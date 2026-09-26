package com.mdvcraft.mdveconomy.market;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import com.mdvcraft.mdveconomy.market.Models.*;

/** A single connection confined to the server thread. No Bukkit objects enter persistence. */
public final class MarketStore implements AutoCloseable {
    private final Connection db;
    public MarketStore(Path file) throws SQLException { this("jdbc:sqlite:" + file.toAbsolutePath()); }
    public MarketStore(String url) throws SQLException {
        try { Class.forName("org.sqlite.JDBC",true,MarketStore.class.getClassLoader()); }
        catch(ClassNotFoundException e) { throw new SQLException("SQLite JDBC no está incluido en el plugin",e); }
        db = DriverManager.getConnection(url);
        execute("PRAGMA foreign_keys=ON"); execute("PRAGMA journal_mode=WAL");
        execute("PRAGMA synchronous=FULL"); execute("PRAGMA busy_timeout=1500");
        execute("CREATE TABLE IF NOT EXISTS market_meta (key TEXT PRIMARY KEY, value TEXT NOT NULL)");
        execute("CREATE TABLE IF NOT EXISTS market_listings (id INTEGER PRIMARY KEY AUTOINCREMENT, seller TEXT NOT NULL, seller_name TEXT NOT NULL, item TEXT NOT NULL, category TEXT NOT NULL, tier TEXT NOT NULL, mode TEXT NOT NULL CHECK(mode IN ('BID','TRADE')), status TEXT NOT NULL DEFAULT 'ACTIVE', expires INTEGER NOT NULL, price INTEGER NOT NULL DEFAULT 0, winner TEXT NOT NULL DEFAULT '', winner_name TEXT NOT NULL DEFAULT '', created INTEGER NOT NULL)");
        execute("CREATE TABLE IF NOT EXISTS market_bids (id INTEGER PRIMARY KEY AUTOINCREMENT, listing INTEGER NOT NULL REFERENCES market_listings(id), player TEXT NOT NULL, name TEXT NOT NULL, amount INTEGER NOT NULL, created INTEGER NOT NULL)");
        execute("CREATE TABLE IF NOT EXISTS market_holds (id INTEGER PRIMARY KEY AUTOINCREMENT, listing INTEGER NOT NULL REFERENCES market_listings(id), player TEXT NOT NULL, kind TEXT NOT NULL, amount INTEGER NOT NULL CHECK(amount>0), status TEXT NOT NULL DEFAULT 'HELD')");
        execute("CREATE TABLE IF NOT EXISTS market_offers (id INTEGER PRIMARY KEY AUTOINCREMENT, listing INTEGER NOT NULL REFERENCES market_listings(id), player TEXT NOT NULL, name TEXT NOT NULL, items TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'ACTIVE', created INTEGER NOT NULL)");
        execute("CREATE TABLE IF NOT EXISTS market_claims (id INTEGER PRIMARY KEY AUTOINCREMENT, player TEXT NOT NULL, listing INTEGER NOT NULL DEFAULT 0, kind TEXT NOT NULL, amount INTEGER NOT NULL DEFAULT 0, payload TEXT NOT NULL DEFAULT '', label TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'READY', unique_key TEXT NOT NULL UNIQUE)");
        execute("CREATE TABLE IF NOT EXISTS market_ops (id INTEGER PRIMARY KEY AUTOINCREMENT, player TEXT NOT NULL, listing INTEGER NOT NULL, direction TEXT NOT NULL, kind TEXT NOT NULL, amount INTEGER NOT NULL, payload TEXT NOT NULL, note TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'PENDING', claim INTEGER NOT NULL DEFAULT 0, created INTEGER NOT NULL)");
        execute("CREATE INDEX IF NOT EXISTS market_active ON market_listings(status, expires)");
        execute("CREATE INDEX IF NOT EXISTS market_owner ON market_listings(seller, status)");
        execute("CREATE INDEX IF NOT EXISTS market_claim_owner ON market_claims(player, status)");
        execute("CREATE INDEX IF NOT EXISTS market_bid_listing ON market_bids(listing, id)");
        execute("CREATE INDEX IF NOT EXISTS market_offer_listing ON market_offers(listing, id)");
        execute("CREATE INDEX IF NOT EXISTS market_hold_listing ON market_holds(listing, player, status)");
        execute("CREATE INDEX IF NOT EXISTS market_op_status ON market_ops(status, listing, player)");
        transaction(() -> { execute("UPDATE market_ops SET status='REVIEW' WHERE status='PENDING'");
            execute("UPDATE market_claims SET status='REVIEW' WHERE status='DELIVERING'"); return null; });
    }
    public interface Work<T> { T run() throws Exception; }
    public <T> T transaction(Work<T> work) throws SQLException {
        if (!db.getAutoCommit()) throw new SQLException("Nested transaction");
        db.setAutoCommit(false);
        try { T value = work.run(); db.commit(); return value; }
        catch (Exception e) { db.rollback(); if(e instanceof RuntimeException r) throw r; if(e instanceof SQLException s) throw s; throw new SQLException(e); }
        finally { db.setAutoCommit(true); }
    }
    public int execute(String sql, Object... values) throws SQLException {
        try(var s=db.prepareStatement(sql)) { bind(s, values); return s.execute()?0:s.getUpdateCount(); }
    }
    public long insert(String sql, Object... values) throws SQLException {
        try(var s=db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(s,values); s.executeUpdate(); try(var r=s.getGeneratedKeys()) { if(r.next()) return r.getLong(1); }
        } throw new SQLException("Missing generated key");
    }
    public interface Mapper<T> { T map(ResultSet r) throws SQLException; }
    public <T> List<T> query(String sql, Mapper<T> mapper, Object... values) throws SQLException {
        try(var s=db.prepareStatement(sql)) { bind(s,values); try(var r=s.executeQuery()) {
            var rows=new ArrayList<T>(); while(r.next()) rows.add(mapper.map(r)); return rows;
        }}
    }
    public long number(String sql,Object... values) throws SQLException { var r=query(sql,x->x.getLong(1),values); return r.isEmpty()?0:r.getFirst(); }
    private void bind(PreparedStatement s,Object[] values) throws SQLException { for(int i=0;i<values.length;i++) s.setObject(i+1,values[i]); }
    public Listing listing(long id) throws SQLException {
        return query("SELECT l.*, (SELECT count(*) FROM market_bids b WHERE b.listing=l.id)+(SELECT count(*) FROM market_offers o WHERE o.listing=l.id) offers FROM market_listings l WHERE l.id=?", MarketStore::listingRow,id).stream().findFirst().orElse(null);
    }
    public static Listing listingRow(ResultSet r) throws SQLException { return new Listing(r.getLong("id"),r.getString("seller"),r.getString("seller_name"),r.getString("item"),r.getString("category"),r.getString("tier"),r.getString("mode"),r.getString("status"),r.getLong("expires"),r.getLong("price"),r.getString("winner"),r.getString("winner_name"),r.getLong("created"),r.getLong("offers")); }
    public static Offer offerRow(ResultSet r) throws SQLException { return new Offer(r.getLong("id"),r.getLong("listing"),r.getString("player"),r.getString("name"),r.getString("items"),r.getString("status"),r.getLong("created")); }
    public static Claim claimRow(ResultSet r) throws SQLException { return new Claim(r.getLong("id"),r.getString("player"),r.getLong("listing"),r.getString("kind"),r.getLong("amount"),r.getString("payload"),r.getString("label"),r.getString("status")); }
    public static Operation opRow(ResultSet r) throws SQLException { return new Operation(r.getLong("id"),r.getString("player"),r.getLong("listing"),r.getString("direction"),r.getString("kind"),r.getLong("amount"),r.getString("payload"),r.getString("note"),r.getString("status"),r.getLong("claim")); }
    @Override public void close() throws SQLException { db.close(); }
}
