package com.mdvcraft.mdveconomy.market;

public final class Models {
    private Models() {}
    public record Listing(long id, String seller, String sellerName, String item, String category, String tier,
                          String mode, String status, long expires, long price, String winner, String winnerName,
                          long created, long offers) {}
    public record Offer(long id, long listing, String player, String name, String items, String status, long created) {}
    public record Bid(long id, String name, long amount, long created) {}
    public record Claim(long id, String player, long listing, String kind, long amount, String payload, String label, String status) {}
    public record Operation(long id, String player, long listing, String direction, String kind, long amount,
                            String payload, String note, String status, long claim) {}
    public record Filter(String category, String tier, String sort, String owner, String participant) {
        public static Filter all() { return new Filter("", "", "RECENT", "", ""); }
    }
}
