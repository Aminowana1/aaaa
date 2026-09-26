package com.mdvcraft.mdveconomy.market;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
import com.mdvcraft.mdveconomy.market.Models.*;

class MarketEngineTest {
    MarketStore db; MarketEngine engine; AtomicLong now;
    @TempDir Path temp;
    @BeforeEach void setup() throws Exception {db=new MarketStore("jdbc:sqlite::memory:");now=new AtomicLong(100000);engine=new MarketEngine(db,now::get);}
    @AfterEach void close() throws Exception {db.close();}
    long listing(String mode) throws Exception {return engine.create("seller","Seller","encoded-item","equipment","RARE",mode,36000000,5,()->true);}
    void bid(String who,long id,String kind,List<Long> debits) throws Exception {engine.bid(who,who,id,kind,999999,amount->()->{debits.add(amount);return true;});}
    @Test void sameWinnerOnlyPaysIncrementAndSellerReceivesTotal() throws Exception {
        long id=listing("BID");var debits=new ArrayList<Long>();bid("a",id,"BANK",debits);bid("a",id,"CASH",debits);
        assertEquals(List.of(1L,1L),debits);assertEquals(2,engine.get(id).price());
        now.addAndGet(36000001);engine.expire(100);
        assertEquals("SOLD",engine.get(id).status());assertEquals(2,engine.claims("seller",id,0,20).getFirst().amount());
        assertEquals("BANK",engine.claims("seller",id,0,20).getFirst().kind());assertEquals("encoded-item",engine.claims("a",id,0,20).getFirst().payload());
        assertEquals(0,db.number("SELECT count(*) FROM market_holds WHERE status='HELD'"));
    }
    @Test void outbidRefundsOriginalSourcesExactlyOnce() throws Exception {
        long id=listing("BID");var debits=new ArrayList<Long>();bid("a",id,"CASH",debits);bid("a",id,"BANK",debits);bid("b",id,"BANK",debits);
        assertEquals(List.of(1L,1L,3L),debits);
        var claims=engine.claims("a",id,0,20);assertEquals(2,claims.size());assertEquals(Set.of("CASH","BANK"),new HashSet<>(claims.stream().map(Claim::kind).toList()));
        engine.cancel("seller",id);assertEquals(2,engine.claims("a",id,0,20).size());assertEquals(3,engine.claims("b",id,0,20).getFirst().amount());
    }
    @Test void rebidAfterBeingOutbidPaysFullNewPrice() throws Exception {
        long id=listing("BID");var d=new ArrayList<Long>();bid("a",id,"BANK",d);bid("b",id,"BANK",d);bid("a",id,"BANK",d);
        assertEquals(List.of(1L,2L,3L),d);assertEquals(1,engine.claims("a",id,0,10).getFirst().amount());
    }
    @Test void insufficientFundsDoNotChangeAuction() throws Exception {
        long id=listing("BID");assertThrows(IllegalArgumentException.class,()->engine.bid("a","A",id,"BANK",100,x->()->false));
        assertEquals(0,engine.get(id).price());assertTrue(engine.reviews().isEmpty());assertEquals(0,db.number("SELECT count(*) FROM market_holds"));
    }
    @Test void createRefusedInventoryNeverPublishes() {
        assertThrows(IllegalArgumentException.class,()->engine.create("seller","S","item","other","","BID",36000000,5,()->false));
        assertDoesNotThrow(()->assertEquals(0,engine.activeCount("seller")));
    }
    @Test void listingLimitIsCheckedBeforeTakingItems() throws Exception {
        for(int i=0;i<5;i++)listing("BID");var removed=new AtomicLong();
        assertThrows(IllegalArgumentException.class,()->engine.create("seller","S","item","other","","BID",36000000,5,()->{removed.incrementAndGet();return true;}));assertEquals(0,removed.get());
    }
    @Test void cannotBuyOwnListingOrBidOnTrade() throws Exception {
        long id=listing("BID"),trade=listing("TRADE");assertThrows(IllegalArgumentException.class,()->bid("seller",id,"BANK",new ArrayList<>()));
        assertThrows(IllegalArgumentException.class,()->bid("a",trade,"BANK",new ArrayList<>()));
    }
    @Test void maxBidStopsDebit() throws Exception {
        long id=listing("BID");bid("a",id,"BANK",new ArrayList<>());assertThrows(IllegalArgumentException.class,()->engine.bid("b","B",id,"BANK",1,n->()->{fail("must not debit");return true;}));
    }
    @Test void expiredListingCannotBeBidOrCancelledOrOffered() throws Exception {
        long id=listing("BID"),trade=listing("TRADE");now.addAndGet(36000001);
        assertThrows(IllegalArgumentException.class,()->bid("a",id,"BANK",new ArrayList<>()));assertThrows(IllegalArgumentException.class,()->engine.cancel("seller",id));
        assertThrows(IllegalArgumentException.class,()->engine.offer("a","A",trade,List.of("offer"),5,()->true));
    }
    @Test void expirationWithoutBidsReturnsItemAndIsIdempotent() throws Exception {
        long id=listing("BID");now.addAndGet(36000001);engine.expire(100);engine.expire(100);
        assertEquals("EXPIRED",engine.get(id).status());assertEquals(1,engine.claims("seller",id,0,10).size());assertEquals(0,engine.activeCount("seller"));
    }
    @Test void tradeAcceptanceTransfersWinnerAndRefundsLosers() throws Exception {
        long id=listing("TRADE");long offer=engine.offer("a","A",id,List.of("sword","cash"),5,()->true);
        engine.offer("b","B",id,List.of("shield"),5,()->true);engine.accept("seller",id,offer);
        assertEquals("SOLD",engine.get(id).status());assertEquals("a",engine.get(id).winner());
        assertEquals("sword\ncash",engine.claims("seller",id,0,10).getFirst().payload());
        assertEquals("encoded-item",engine.claims("a",id,0,10).getFirst().payload());assertEquals("shield",engine.claims("b",id,0,10).getFirst().payload());
        assertThrows(IllegalArgumentException.class,()->engine.accept("seller",id,offer));
    }
    @Test void tradeExpiresWithoutAutoAccepting() throws Exception {
        long id=listing("TRADE");engine.offer("a","A",id,List.of("offer"),5,()->true);now.addAndGet(36000001);engine.expire(100);
        assertEquals("EXPIRED",engine.get(id).status());assertEquals("",engine.get(id).winner());assertEquals("offer",engine.claims("a",id,0,10).getFirst().payload());
    }
    @Test void cancelledTradeReturnsEveryoneAndNoOtherPlayerCanCancel() throws Exception {
        long id=listing("TRADE");engine.offer("a","A",id,List.of("a"),5,()->true);engine.offer("b","B",id,List.of("b"),5,()->true);
        assertThrows(IllegalArgumentException.class,()->engine.cancel("a",id));engine.cancel("seller",id);
        assertEquals(1,engine.claims("a",id,0,10).size());assertEquals(1,engine.claims("b",id,0,10).size());assertEquals(1,engine.claims("seller",id,0,10).size());
    }
    @Test void onlyOwnerCanAcceptAndOfferMustBelongToListing() throws Exception {
        long id=listing("TRADE"),other=listing("TRADE");long offer=engine.offer("a","A",id,List.of("a"),5,()->true);
        assertThrows(IllegalArgumentException.class,()->engine.accept("b",id,offer));assertThrows(IllegalArgumentException.class,()->engine.accept("seller",other,offer));
    }
    @Test void offersValidateSizeLimitAndOwnListing() throws Exception {
        long id=listing("TRADE");assertThrows(IllegalArgumentException.class,()->engine.offer("a","A",id,List.of(),5,()->true));
        assertThrows(IllegalArgumentException.class,()->engine.offer("a","A",id,Collections.nCopies(6,"x"),5,()->true));
        assertThrows(IllegalArgumentException.class,()->engine.offer("seller","S",id,List.of("x"),5,()->true));
        engine.offer("a","A",id,List.of("x"),1,()->true);assertThrows(IllegalArgumentException.class,()->engine.offer("a","A",id,List.of("y"),1,()->true));
    }
    @Test void latestOffersFirstAndOnlyThreeRecentBids() throws Exception {
        long trade=listing("TRADE");for(int i=0;i<5;i++)engine.offer("a","A",trade,List.of("offer"+i),5,()->true);
        assertEquals("offer4",engine.offers(trade,0,4).getFirst().items());assertEquals(5,engine.offers(trade,0,4).size());
        long id=listing("BID");for(int i=0;i<4;i++)bid("a",id,"BANK",new ArrayList<>());
        assertEquals(List.of(4L,3L,2L),engine.bids(id).stream().map(Bid::amount).toList());
    }
    @Test void claimIsOwnedAndDeliveredOnce() throws Exception {
        long id=listing("BID");engine.cancel("seller",id);long claim=engine.claims("seller",id,0,10).getFirst().id();var deliveries=new AtomicLong();
        assertThrows(IllegalArgumentException.class,()->engine.redeem("other",claim,c->()->true));
        assertTrue(engine.redeem("seller",claim,c->()->{deliveries.incrementAndGet();return true;}));
        assertThrows(IllegalArgumentException.class,()->engine.redeem("seller",claim,c->()->true));assertEquals(1,deliveries.get());
    }
    @Test void rejectedVaultDepositLeavesClaimReady() throws Exception {
        long id=listing("BID");bid("a",id,"BANK",new ArrayList<>());now.addAndGet(36000001);engine.expire(100);
        long claim=engine.claims("seller",id,0,10).getFirst().id();assertFalse(engine.redeem("seller",claim,c->()->false));
        assertEquals("READY",engine.claims("seller",id,0,10).getFirst().status());assertTrue(engine.reviews().isEmpty());
    }
    @Test void fullInventoryPreflightDoesNotClaim() throws Exception {
        long id=listing("BID");engine.cancel("seller",id);long claim=engine.claims("seller",id,0,10).getFirst().id();
        assertThrows(IllegalArgumentException.class,()->engine.redeem("seller",claim,c->{throw new IllegalArgumentException("full");}));
        assertEquals("READY",engine.claims("seller",id,0,10).getFirst().status());assertEquals(0,db.number("SELECT count(*) FROM market_ops WHERE direction='GIVE'"));
    }
    @Test void ambiguousWithdrawalBlocksReplayAndCanBeRefundedByReconciliation() throws Exception {
        long id=listing("BID");assertThrows(IllegalStateException.class,()->engine.bid("a","A",id,"BANK",100,n->()->{throw new Exception("provider timeout");}));
        var op=engine.reviews().getFirst();assertEquals(0,engine.get(id).price());
        assertThrows(IllegalArgumentException.class,()->bid("a",id,"BANK",new ArrayList<>()));
        assertThrows(IllegalArgumentException.class,()->bid("b",id,"BANK",new ArrayList<>()));
        now.addAndGet(36000001);engine.expire(100);assertEquals("ACTIVE",engine.get(id).status());
        engine.resolve(op.id(),"retirado");assertEquals(1,engine.claims("a",id,0,10).getFirst().amount());engine.expire(100);assertEquals("EXPIRED",engine.get(id).status());
        assertThrows(IllegalArgumentException.class,()->engine.resolve(op.id(),"retirado"));
    }
    @Test void sqlFailureAfterDebitLeavesIntentInsteadOfLosingPayment() throws Exception {
        long id=listing("BID");db.execute("CREATE TRIGGER reject_bid BEFORE INSERT ON market_bids BEGIN SELECT RAISE(ABORT,'simulated disk failure'); END");
        var debit=new AtomicLong();assertThrows(IllegalStateException.class,()->engine.bid("a","A",id,"CASH",100,n->()->{debit.addAndGet(n);return true;}));
        assertEquals(1,debit.get());assertEquals(0,db.number("SELECT count(*) FROM market_holds"));assertEquals(0,engine.get(id).price());assertEquals(1,engine.reviews().size());
        engine.resolve(engine.reviews().getFirst().id(),"retirado");assertEquals("CASH",engine.claims("a",id,0,10).getFirst().kind());
    }
    @Test void ambiguousDeliveryCannotBeRepeatedAndCanBeAcknowledged() throws Exception {
        long id=listing("BID");engine.cancel("seller",id);long claim=engine.claims("seller",id,0,10).getFirst().id();
        assertThrows(IllegalStateException.class,()->engine.redeem("seller",claim,c->()->{throw new Exception("crash after side effect");}));
        assertEquals("REVIEW",engine.claims("seller",id,0,10).getFirst().status());assertThrows(IllegalArgumentException.class,()->engine.redeem("seller",claim,c->()->true));
        engine.resolve(engine.reviews().getFirst().id(),"entregado");assertEquals(0,engine.claims("seller",id,0,10).size());
    }
    @Test void notDeliveredResolutionAllowsOneRetry() throws Exception {
        long id=listing("BID");engine.cancel("seller",id);long claim=engine.claims("seller",id,0,10).getFirst().id();
        assertThrows(IllegalStateException.class,()->engine.redeem("seller",claim,c->()->{throw new Exception("error");}));
        engine.resolve(engine.reviews().getFirst().id(),"no-entregado");assertTrue(engine.redeem("seller",claim,c->()->true));
    }
    @Test void restartMarksIncompleteIntentsForReview() throws Exception {
        Path file=temp.resolve("restart.db");try(var s=new MarketStore(file)) {s.insert("INSERT INTO market_ops(player,listing,direction,kind,amount,payload,note,created) VALUES('a',0,'TAKE','BANK',5,'','test',0)");}
        try(var s=new MarketStore(file)) {assertEquals(1,new MarketEngine(s).reviews().size());}
    }
    @Test void transactionRollsBackExpirationAndCanRetryWithoutDuplicates() throws Exception {
        long id=listing("BID");now.addAndGet(36000001);db.execute("CREATE TRIGGER reject_claim BEFORE INSERT ON market_claims BEGIN SELECT RAISE(ABORT,'failure'); END");
        assertThrows(java.sql.SQLException.class,()->engine.expire(100));assertEquals("ACTIVE",engine.get(id).status());
        db.execute("DROP TRIGGER reject_claim");engine.expire(100);engine.expire(100);assertEquals(1,engine.claims("seller",id,0,10).size());
    }
    @Test void filterCategoryTierOwnerAndParticipantAndStablePaging() throws Exception {
        long id=listing("BID");engine.create("other","Other","other","armor","EPIC","BID",36000000,5,()->true);
        bid("a",id,"BANK",new ArrayList<>());
        assertEquals(id,engine.listings(new Filter("equipment","RARE","RECENT","",""),0,5).getFirst().id());
        assertEquals(1,engine.listings(new Filter("","","RECENT","seller",""),0,5).size());
        assertEquals(id,engine.listings(new Filter("","","RECENT","","a"),0,5).getFirst().id());
        assertEquals(2,engine.listings(Filter.all(),0,1).size());assertEquals(1,engine.listings(Filter.all(),1,1).size());
    }
    @Test void sortHighLowPopularAndSellerCount() throws Exception {
        long a=listing("BID"),b=listing("BID");bid("buyer",a,"BANK",new ArrayList<>());
        assertEquals(a,engine.listings(new Filter("","","PRICE_HIGH","",""),0,5).getFirst().id());
        assertEquals(b,engine.listings(new Filter("","","PRICE_LOW","",""),0,5).getFirst().id());
        assertEquals(a,engine.listings(new Filter("","","POPULAR","",""),0,5).getFirst().id());
        engine.create("other","Other","i","other","","BID",36000000,5,()->true);
        assertEquals("seller",engine.listings(new Filter("","","SELLER_COUNT","",""),0,5).getFirst().seller());
    }
    @Test void legacyMigrationCreatesReturnsOnceWithoutDeletingOldTables() throws Exception {
        db.execute("CREATE TABLE players(id INTEGER,uuid TEXT)");db.execute("CREATE TABLE auctions(id INTEGER,seller_id INTEGER,item_data TEXT,status TEXT)");
        db.execute("CREATE TABLE pending_items(id INTEGER,player_id INTEGER,item_data TEXT,status TEXT)");
        db.execute("INSERT INTO players VALUES(1,'seller')");db.execute("INSERT INTO auctions VALUES(1,1,'old-item','ACTIVE')");db.execute("INSERT INTO auctions VALUES(2,1,'already-sold','SOLD')");
        db.execute("INSERT INTO pending_items VALUES(1,1,'pending','PENDING')");engine.migrateLegacy();engine.migrateLegacy();
        assertEquals(2,engine.claims("seller",0,0,10).size());assertEquals(2,db.number("SELECT count(*) FROM auctions"));
    }
    @Test void largeCashRefundCanBeClaimedInPartsWithoutLosingRemainder() throws Exception {
        long id=listing("BID");for(int i=0;i<10;i++)bid("a",id,"CASH",new ArrayList<>());engine.cancel("seller",id);
        var refund=engine.claims("a",id,0,10);assertEquals(1,refund.size());long claim=refund.getFirst().id();var delivered=new AtomicLong();
        assertTrue(engine.redeem("a",claim,3,c->()->{delivered.addAndGet(c.amount());return true;}));
        assertEquals(7,engine.claims("a",id,0,10).getFirst().amount());
        assertTrue(engine.redeem("a",claim,99,c->()->{delivered.addAndGet(c.amount());return true;}));
        assertEquals(10,delivered.get());assertTrue(engine.claims("a",id,0,10).isEmpty());
    }
    @Test void partialCashDeliveryInReviewOnlySubtractsConfirmedPortion() throws Exception {
        long id=listing("BID");for(int i=0;i<5;i++)bid("a",id,"CASH",new ArrayList<>());engine.cancel("seller",id);
        long claim=engine.claims("a",id,0,10).getFirst().id();assertThrows(IllegalStateException.class,()->engine.redeem("a",claim,2,c->()->{throw new Exception("uncertain");}));
        engine.resolve(engine.reviews().getFirst().id(),"entregado");assertEquals(3,engine.claims("a",id,0,10).getFirst().amount());assertEquals("READY",engine.claims("a",id,0,10).getFirst().status());
    }
    @Test void emptyCashCapacityDoesNotStartDelivery() throws Exception {
        long id=listing("BID");bid("a",id,"CASH",new ArrayList<>());engine.cancel("seller",id);long claim=engine.claims("a",id,0,10).getFirst().id();
        assertThrows(IllegalArgumentException.class,()->engine.redeem("a",claim,0,c->()->true));assertEquals("READY",engine.claims("a",id,0,10).getFirst().status());
    }
    @Test void sqliteBackupIncludesCommittedRows() throws Exception {
        listing("BID");Path backup=temp.resolve("backup.db");db.execute("VACUUM INTO ?",backup.toString());
        try(var copied=new MarketStore(backup)){assertEquals(1,copied.number("SELECT count(*) FROM market_listings"));}
    }
}
