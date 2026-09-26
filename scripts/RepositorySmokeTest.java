import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import com.mdvcraft.mdveconomy.auction.AuctionRepository;
import com.mdvcraft.mdveconomy.pending.PendingItemRepository;
import com.mdvcraft.mdveconomy.players.PaymentMethod;
import com.mdvcraft.mdveconomy.players.PlayerRepository;

/** Real JDBC/repository checks on an in-memory fixture, not a Paper integration test. */
class RepositorySmokeTest {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        try (Connection db = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            try (var sql = db.createStatement()) {
                sql.execute("PRAGMA foreign_keys = ON");
                sql.execute("CREATE TABLE players (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid TEXT NOT NULL UNIQUE, username TEXT, default_payment_method TEXT NOT NULL DEFAULT 'inventory')");
                sql.execute("CREATE TABLE auctions (id INTEGER PRIMARY KEY AUTOINCREMENT, seller_id INTEGER NOT NULL REFERENCES players(id), item_data TEXT NOT NULL, quantity INTEGER NOT NULL, price REAL NOT NULL, status TEXT NOT NULL, created_at INTEGER NOT NULL, expires_at INTEGER NOT NULL)");
                sql.execute("CREATE TABLE pending_items (id INTEGER PRIMARY KEY AUTOINCREMENT, player_id INTEGER NOT NULL REFERENCES players(id), item_data TEXT NOT NULL, quantity INTEGER NOT NULL, source_type TEXT NOT NULL, source_id INTEGER, status TEXT NOT NULL, created_at INTEGER NOT NULL, claimed_at INTEGER)");
            }
            UUID uuid = UUID.randomUUID();
            long player = PlayerRepository.getOrCreateId(db, uuid, "Seller");
            check(player == PlayerRepository.getOrCreateId(db, uuid, "O'Brien"), "stable player ID / quoted name");
            check(PlayerRepository.getDefaultPaymentMethod(db, player) == PaymentMethod.INVENTORY, "default payment preference");
            PlayerRepository.setDefaultPaymentMethod(db, player, PaymentMethod.BANK);
            check(PlayerRepository.getDefaultPaymentMethod(db, player) == PaymentMethod.BANK, "saved preference");
            long active = AuctionRepository.insert(db, player, "fixture", 3, 10, 100, 1000);
            long expired = AuctionRepository.insert(db, player, "fixture", 1, 5, 100, 200);
            check(AuctionRepository.countActive(db, 500) == 1, "expired listings excluded from browsing");
            check(AuctionRepository.findActivePage(db, 500, 1, 0).getFirst().id() == active, "page retrieval");
            check(AuctionRepository.markSold(db, active, 500) && !AuctionRepository.markSold(db, active, 500), "single successful sale transition");
            check(!AuctionRepository.markSold(db, expired, 500), "expired listing cannot be sold");
            check(AuctionRepository.markExpired(db, expired) && !AuctionRepository.markExpired(db, expired), "idempotent expiration");
            long pending = PendingItemRepository.insert(db, player, "fixture", 1, "TEST", null, 500);
            check(PendingItemRepository.findPendingForPlayer(db, player).getFirst().sourceId() == null, "nullable source ID");
            check(PendingItemRepository.claim(db, pending, 600) && !PendingItemRepository.claim(db, pending, 600), "single successful claim");
            check(PendingItemRepository.findPendingForPlayer(db, player).isEmpty(), "claimed item excluded from queue");
            boolean foreignKeyRejected = false;
            try {
                AuctionRepository.insert(db, player + 999, "fixture", 1, 1, 100, 1000);
            } catch (SQLException expected) {
                foreignKeyRejected = true;
            }
            check(foreignKeyRejected, "foreign key enforcement");
        }
        System.out.println("PASS: 12 SQLite/repository checks. Inventory, Vault and crash recovery require Paper tests.");
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        System.out.println("PASS: " + label);
    }
}
