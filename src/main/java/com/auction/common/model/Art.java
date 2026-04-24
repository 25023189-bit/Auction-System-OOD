package com.auction.common.model;

import java.io.Serial;

public class Art extends Item {
    @Serial
    private static final long serialVersionUID = 1L;

    private String artistName;

    public Art(String id, String name, String description, double startingPrice, String artistName) {
        super(id, name, description, startingPrice);
        this.artistName = artistName;
    }

    public void printInfo() {
        System.out.println("[Art] " + getProductName() + " - Artist: " + artistName
                + " - Starting price: $" + getStartingPrice());
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}
