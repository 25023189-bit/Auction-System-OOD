package com.auction.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;

public class Art extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(Art.class);

    @Serial
    private static final long serialVersionUID = 1L;

    private String artistName;

    public Art(String id, String name, String description, double startingPrice, String artistName) {
        super(id, name, description, startingPrice);
        this.artistName = artistName;
    }

    public void printInfo() {
        LOGGER.info("Art {} - Artist: {} - Starting price: ${}", getProductName(), artistName, getStartingPrice());
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}
