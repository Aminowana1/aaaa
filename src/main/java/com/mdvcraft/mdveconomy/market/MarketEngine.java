package com.mdvcraft.mdveconomy.market;

import java.sql.SQLException;
import java.util.*;
import java.util.function.LongSupplier;
import com.mdvcraft.mdveconomy.market.Models.*;

/** Domain rules and durable escrow. All external transfers have an intent before execution. */
public final class MarketEngine {
    private final MarketStore db;
    private final LongSupplier clock;
    private boolean halted;
    public MarketEngine(MarketStore db) { this(db,System::currentTimeMillis); }
    public MarketEngine(MarketStore db,LongSupplier clock) { this.db=db; this.clock=clock; }
    @FunctionalInterface public interface Transfer { boolean run() throws Exception; }
    private void require(boolean ok,String message) { if(!ok) throw new IllegalArgumentException(message); }
    private void available(String player,long listing) throws SQLException {
        require(!halted,"La economía está pausada por un error de almacenamiento. Contactá al administrador.");
        require(db.number("SELECT count(*) FROM market_ops WHERE status IN ('REVIEW','PENDING') AND (player=? OR (listing=? AND listing<>0))",player,listing)==0,
                "Hay una operación pendiente de revisión. Contactá al administrador.");
    }
    private Listing active(long id) throws SQLException {
        Listing l=db.listing(id); require(l!=null,"Publicación inexistente.");
        require(l.status().equals("ACTIVE") && l.expires()>clock.getAsLong(),"La publicación ya terminó."); return l;
    }
    private void collect(String player,long listing,String kind,long amount,String payload,String note,Transfer transfer,MarketStore.Work<Void> finish) throws SQLException {
        available(player,listing);
        long op=db.insert("INSERT INTO market_ops(player,listing,direction,kind,amount,payload,note,created) VALUES(?,?,'TAKE',?,?,?,?,?)",player,listing,kind,amount,payload,note,clock.getAsLong());
        try {
            if(!transfer.run()) { db.execute("UPDATE market_ops SET status='CANCELLED' WHERE id=?",op); throw new Declined("No se pudo retirar el pago o los artículos. Revisá tu inventario/saldo."); }
            db.transaction(()-> { finish.run(); db.execute("UPDATE market_ops SET status='DONE' WHERE id=?",op); return null; });
        } catch(Declined e) { throw e; }
        catch(Exception e) {
            try { db.execute("UPDATE market_ops SET status='REVIEW' WHERE id=? AND status='PENDING'",op); }
            catch(SQLException failure) { halted=true; e.addSuppressed(failure); }
            throw new IllegalStateException("Operación #"+op+" en revisión; no repitas el pago. El administrador debe verificarla.",e);
        }
    }
    private static final class Declined extends IllegalArgumentException { Declined(String s) { super(s); } }
    public synchronized long create(String player,String name,String item,String category,String tier,String mode,long duration,int limit,Transfer take) throws SQLException {
        available(player,0); require(!item.isBlank(),"Seleccioná un artículo.");
        require(mode.equals("BID")||mode.equals("TRADE"),"Modo inválido.");
        require(duration>0 && duration<=172800000L,"Duración inválida.");
        require(limit>0 && db.number("SELECT count(*) FROM market_listings WHERE seller=? AND status='ACTIVE'",player)<limit,"No tenés espacios disponibles.");
        long now=clock.getAsLong(); long[] id={0};
        collect(player,0,"ITEMS",0,item,"Crear publicación",take,()-> {
            id[0]=db.insert("INSERT INTO market_listings(seller,seller_name,item,category,tier,mode,expires,created) VALUES(?,?,?,?,?,?,?,?)",player,name,item,category,tier,mode,now+duration,now); return null;
        }); return id[0];
    }
    public synchronized long nextBid(long id) throws SQLException { Listing l=active(id); require(l.mode().equals("BID"),"Esta publicación es de trueque."); return Math.addExact(l.price(),1); }
    public synchronized long bid(String player,String name,long id,String kind,long maxPrice,java.util.function.LongFunction<Transfer> debit) throws SQLException {
        available(player,id); Listing l=active(id);
        require(l.mode().equals("BID"),"Esta publicación es de trueque.");
        require(!l.seller().equals(player),"No podés pujar por tu propia publicación.");
        require(kind.equals("BANK")||kind.equals("CASH"),"Método inválido.");
        long next=Math.addExact(l.price(),1); require(next<=maxPrice,"Se alcanzó el máximo de puja configurado.");
        long delta=l.winner().equals(player)?1:next;
        collect(player,id,kind,delta,"","Puja por publicación #"+id,debit.apply(delta),()-> {
            if(!l.winner().isEmpty() && !l.winner().equals(player)) refundHolds(id,"Puja superada");
            db.insert("INSERT INTO market_holds(listing,player,kind,amount) VALUES(?,?,?,?)",id,player,kind,delta);
            db.insert("INSERT INTO market_bids(listing,player,name,amount,created) VALUES(?,?,?,?,?)",id,player,name,next,clock.getAsLong());
            db.execute("UPDATE market_listings SET price=?,winner=?,winner_name=? WHERE id=?",next,player,name,id); return null;
        }); return next;
    }
    public synchronized long offer(String player,String name,long id,List<String> items,int maxOffers,Transfer take) throws SQLException {
        available(player,id); Listing l=active(id);
        require(l.mode().equals("TRADE"),"Esta publicación es de pujas.");
        require(!l.seller().equals(player),"No podés ofertar en tu propia publicación.");
        require(!items.isEmpty() && items.size()<=5 && items.stream().noneMatch(String::isBlank),"Ofrecé entre 1 y 5 pilas.");
        require(db.number("SELECT count(*) FROM market_offers WHERE listing=? AND player=? AND status='ACTIVE'",id,player)<maxOffers,"Llegaste al máximo de ofertas para esta publicación.");
        String payload=String.join("\n",items); long[] offer={0};
        collect(player,id,"ITEMS",0,payload,"Oferta por publicación #"+id,take,()-> {
            offer[0]=db.insert("INSERT INTO market_offers(listing,player,name,items,created) VALUES(?,?,?,?,?)",id,player,name,payload,clock.getAsLong()); return null;
        }); return offer[0];
    }
    private void claim(String key,String player,long listing,String kind,long amount,String payload,String label) throws SQLException {
        db.execute("INSERT INTO market_claims(unique_key,player,listing,kind,amount,payload,label) VALUES(?,?,?,?,?,?,?) ON CONFLICT(unique_key) DO NOTHING",key,player,listing,kind,amount,payload,label);
    }
    private void refundHolds(long id,String label) throws SQLException {
        var holds=db.query("SELECT min(id) id,player,kind,sum(amount) amount FROM market_holds WHERE listing=? AND status='HELD' GROUP BY player,kind",r->new Object[]{r.getLong("id"),r.getString("player"),r.getString("kind"),r.getLong("amount")},id);
        for(var h:holds) claim("hold:"+h[0],(String)h[1],id,(String)h[2],(long)h[3],"",label);
        db.execute("UPDATE market_holds SET status='REFUNDED' WHERE listing=? AND status='HELD'",id);
    }
    private void refundOffers(long id,long except) throws SQLException {
        for(Offer o:db.query("SELECT * FROM market_offers WHERE listing=? AND status='ACTIVE' AND id<>?",MarketStore::offerRow,id,except)) {
            claim("offer:"+o.id(),o.player(),id,"ITEMS",0,o.items(),"Devolución de trueque");
            db.execute("UPDATE market_offers SET status='RETURNED' WHERE id=?",o.id());
        }
    }
    public synchronized void cancel(String seller,long id) throws SQLException {
        available(seller,id); Listing l=active(id); require(l.seller().equals(seller),"No sos el dueño.");
        db.transaction(()-> { finishWithoutSale(l,"CANCELLED"); return null; });
    }
    private void finishWithoutSale(Listing l,String status) throws SQLException {
        refundHolds(l.id(),"Devolución por cancelación/vencimiento"); refundOffers(l.id(),-1);
        claim("listing-return:"+l.id(),l.seller(),l.id(),"ITEMS",0,l.item(),"Recuperar publicación");
        db.execute("UPDATE market_listings SET status=? WHERE id=? AND status='ACTIVE'",status,l.id());
    }
    public synchronized void accept(String seller,long id,long offerId) throws SQLException {
        available(seller,id); Listing l=active(id);
        require(l.seller().equals(seller),"No sos el dueño."); require(l.mode().equals("TRADE"),"No es un trueque.");
        Offer o=db.query("SELECT * FROM market_offers WHERE id=? AND listing=? AND status='ACTIVE'",MarketStore::offerRow,offerId,id).stream().findFirst().orElse(null);
        require(o!=null,"La oferta ya no está disponible.");
        db.transaction(()-> {
            claim("trade-seller:"+id,seller,id,"ITEMS",0,o.items(),"Trueque aceptado");
            claim("trade-buyer:"+id,o.player(),id,"ITEMS",0,l.item(),"Artículo ganado por trueque");
            db.execute("UPDATE market_offers SET status='ACCEPTED' WHERE id=?",offerId);
            refundOffers(id,offerId);
            db.execute("UPDATE market_listings SET status='SOLD',winner=?,winner_name=? WHERE id=?",o.player(),o.name(),id); return null;
        });
    }
    public synchronized void expire(int batch) throws SQLException {
        require(!halted,"Almacenamiento pausado.");
        var ids=db.query("SELECT id FROM market_listings WHERE status='ACTIVE' AND expires<=? AND NOT EXISTS (SELECT 1 FROM market_ops WHERE listing=market_listings.id AND status IN ('PENDING','REVIEW')) ORDER BY id LIMIT ?",r->r.getLong(1),clock.getAsLong(),batch);
        for(long id:ids) db.transaction(()-> {
            Listing l=db.listing(id);
            if(l.mode().equals("TRADE") || l.winner().isEmpty()) finishWithoutSale(l,"EXPIRED");
            else {
                claim("bid-seller:"+id,l.seller(),id,"BANK",l.price(),"","Cobro de subasta");
                claim("bid-winner:"+id,l.winner(),id,"ITEMS",0,l.item(),"Artículo ganado por puja");
                db.execute("UPDATE market_holds SET status='SPENT' WHERE listing=? AND status='HELD'",id);
                db.execute("UPDATE market_listings SET status='SOLD' WHERE id=?",id);
            } return null;
        });
    }
    public synchronized boolean redeem(String player,long id,java.util.function.Function<Claim,Transfer> credit) throws SQLException {
        return redeem(player,id,Long.MAX_VALUE,credit);
    }
    public synchronized boolean redeem(String player,long id,long cashCapacity,java.util.function.Function<Claim,Transfer> credit) throws SQLException {
        Claim original=db.query("SELECT * FROM market_claims WHERE id=? AND player=?",MarketStore::claimRow,id,player).stream().findFirst().orElse(null);
        require(original!=null && original.status().equals("READY"),"Entrega ya reclamada o en revisión."); available(player,original.listing());
        long amount=original.kind().equals("CASH")?Math.min(original.amount(),cashCapacity):original.amount();
        require(!original.kind().equals("CASH")||amount>0,"Liberá espacio para recuperar los denares.");
        Claim c=new Claim(original.id(),original.player(),original.listing(),original.kind(),amount,original.payload(),original.label(),original.status());
        // Preflight may reject full inventory before recording any delivery intent.
        Transfer transfer=credit.apply(c);
        long op=db.transaction(()-> {
            require(db.execute("UPDATE market_claims SET status='DELIVERING' WHERE id=? AND status='READY'",id)==1,"Entrega no disponible.");
            return db.insert("INSERT INTO market_ops(player,listing,direction,kind,amount,payload,note,claim,created) VALUES(?,?,'GIVE',?,?,?,?,?,?)",player,c.listing(),c.kind(),c.amount(),c.payload(),c.label(),id,clock.getAsLong());
        });
        try {
            boolean done=transfer.run();
            db.transaction(()-> { if(done && c.kind().equals("CASH")) finishCashClaim(id,c.amount());
                else db.execute("UPDATE market_claims SET status=? WHERE id=?",done?"CLAIMED":"READY",id);
                db.execute("UPDATE market_ops SET status=? WHERE id=?",done?"DONE":"CANCELLED",op); return null; });
            return done;
        } catch(Exception e) {
            try { db.transaction(()-> {db.execute("UPDATE market_ops SET status='REVIEW' WHERE id=? AND status='PENDING'",op);
                db.execute("UPDATE market_claims SET status='REVIEW' WHERE id=? AND status='DELIVERING'",id); return null;}); }
            catch(SQLException failure) { halted=true; e.addSuppressed(failure); }
            throw new IllegalStateException("Entrega #"+op+" en revisión; no se reintentará automáticamente.",e);
        }
    }
    private void finishCashClaim(long id,long amount) throws SQLException {
        db.execute("UPDATE market_claims SET status=CASE WHEN amount>? THEN 'READY' ELSE 'CLAIMED' END, amount=amount-? WHERE id=?",amount,amount,id);
    }
    public synchronized List<Operation> reviews() throws SQLException { return db.query("SELECT * FROM market_ops WHERE status='REVIEW' ORDER BY id LIMIT 100",MarketStore::opRow); }
    /** Explicit admin reconciliation after consulting inventory/provider logs; never automatic. */
    public synchronized void resolve(long id,String decision) throws SQLException {
        Operation o=db.query("SELECT * FROM market_ops WHERE id=? AND status='REVIEW'",MarketStore::opRow,id).stream().findFirst().orElse(null);
        require(o!=null,"Operación inexistente o resuelta.");
        boolean take=o.direction().equals("TAKE");
        require(take ? Set.of("retirado","no-retirado").contains(decision) : Set.of("entregado","no-entregado").contains(decision),"Decisión incompatible con la operación.");
        db.transaction(()-> {
            if(take && decision.equals("retirado")) claim("recovery:"+id,o.player(),o.listing(),o.kind(),o.amount(),o.payload(),"Recuperación administrativa #"+id);
            if(!take) {
                if(decision.equals("entregado")&&o.kind().equals("CASH"))finishCashClaim(o.claim(),o.amount());
                else db.execute("UPDATE market_claims SET status=? WHERE id=?",decision.equals("entregado")?"CLAIMED":"READY",o.claim());
            }
            db.execute("UPDATE market_ops SET status='RESOLVED',note=note||? WHERE id=?"," ["+decision+"]",id); return null;
        });
    }
    public synchronized Listing get(long id) throws SQLException { return db.listing(id); }
    public synchronized List<Listing> listings(Filter f,int page,int size) throws SQLException {
        require(page>=0 && size>0 && size<=54,"Página inválida.");
        var args=new ArrayList<Object>(); var where=new StringBuilder(" WHERE 1=1");
        if(!f.owner().isEmpty()) { where.append(" AND l.seller=?"); args.add(f.owner()); }
        else if(!f.participant().isEmpty()) { where.append(" AND (EXISTS(SELECT 1 FROM market_bids b WHERE b.listing=l.id AND b.player=?) OR EXISTS(SELECT 1 FROM market_offers o WHERE o.listing=l.id AND o.player=?))");args.add(f.participant());args.add(f.participant()); }
        else {where.append(" AND l.status='ACTIVE' AND l.expires>?"); args.add(clock.getAsLong());}
        if(!f.category().isEmpty()) {where.append(" AND l.category=?");args.add(f.category());}
        if(!f.tier().isEmpty()) {where.append(" AND l.tier=?");args.add(f.tier());}
        String order=switch(f.sort()) {case "PRICE_HIGH"->"l.price DESC,l.id DESC";case "PRICE_LOW"->"l.price ASC,l.id DESC";
            case "SELLER_COUNT"->"(SELECT count(*) FROM market_listings x WHERE x.seller=l.seller AND x.status='ACTIVE') DESC,l.id DESC";
            case "POPULAR"->"offers DESC,l.id DESC";default->"l.id DESC";};
        if(!f.owner().isEmpty()||!f.participant().isEmpty()) order="(l.status='ACTIVE') DESC,l.id DESC";
        args.add(size+1); args.add((long)page*size);
        return db.query("SELECT l.*,(SELECT count(*) FROM market_bids b WHERE b.listing=l.id)+(SELECT count(*) FROM market_offers o WHERE o.listing=l.id) offers FROM market_listings l"+where+" ORDER BY "+order+" LIMIT ? OFFSET ?",MarketStore::listingRow,args.toArray());
    }
    public synchronized List<Offer> offers(long listing,int page,int size) throws SQLException {
        return db.query("SELECT * FROM market_offers WHERE listing=? ORDER BY id DESC LIMIT ? OFFSET ?",MarketStore::offerRow,listing,size+1,(long)page*size);
    }
    public synchronized List<Bid> bids(long listing) throws SQLException { return db.query("SELECT id,name,amount,created FROM market_bids WHERE listing=? ORDER BY id DESC LIMIT 3",r->new Bid(r.getLong(1),r.getString(2),r.getLong(3),r.getLong(4)),listing); }
    public synchronized List<Claim> claims(String player,long listing,int page,int size) throws SQLException {
        String extra=listing>0?" AND listing=?":"";
        Object[] args=listing>0?new Object[]{player,listing,size+1,(long)page*size}:new Object[]{player,size+1,(long)page*size};
        return db.query("SELECT * FROM market_claims WHERE player=? AND status IN ('READY','REVIEW','DELIVERING')"+extra+" ORDER BY id LIMIT ? OFFSET ?",MarketStore::claimRow,args);
    }
    public synchronized long ready(String player,long listing) throws SQLException { return db.number("SELECT count(*) FROM market_claims WHERE player=? AND listing=? AND status='READY'",player,listing); }
    public synchronized int activeCount(String player) throws SQLException { return (int)db.number("SELECT count(*) FROM market_listings WHERE seller=? AND status='ACTIVE'",player); }
    /** Legacy fixed-price listings are returned, not silently converted to bids. Runs once, atomically. */
    public synchronized void migrateLegacy() throws SQLException {
        if(db.number("SELECT count(*) FROM market_meta WHERE key='legacy-import'")>0) return;
        db.transaction(()-> {
            if(db.number("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='auctions'")>0) {
                var old=db.query("SELECT a.id,p.uuid,a.item_data FROM auctions a JOIN players p ON p.id=a.seller_id WHERE a.status='ACTIVE'",r->new String[]{r.getString(1),r.getString(2),r.getString(3)});
                for(var row:old) claim("legacy-listing:"+row[0],row[1],0,"ITEMS",0,row[2],"Publicación anterior: recuperar artículo");
            }
            if(db.number("SELECT count(*) FROM sqlite_master WHERE type='table' AND name='pending_items'")>0) {
                var old=db.query("SELECT i.id,p.uuid,i.item_data FROM pending_items i JOIN players p ON p.id=i.player_id WHERE i.status='PENDING'",r->new String[]{r.getString(1),r.getString(2),r.getString(3)});
                for(var row:old) claim("legacy-pending:"+row[0],row[1],0,"ITEMS",0,row[2],"Pendiente de la versión anterior");
            }
            db.execute("INSERT INTO market_meta(key,value) VALUES('legacy-import','2')"); return null;
        });
    }
}
