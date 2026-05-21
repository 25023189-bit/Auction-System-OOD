package com.auction.common.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageTest {

    @Test
    @DisplayName("Test Constructor 1: Truyền action, id, data")
    void testConstructor_ActionIdData() {
        Message msg = new Message("LOGIN", "U001", "PayloadData");

        assertEquals("LOGIN", msg.getAction());
        assertEquals("U001", msg.getId());
        assertEquals("PayloadData", msg.getData());
        assertNull(msg.getUsername());
        assertNull(msg.getRole());
    }

    @Test
    @DisplayName("Test Constructor 2: Chỉ truyền action và data")
    void testConstructor_ActionData() {
        Message msg = new Message("LOGOUT", "ByeBye");

        assertEquals("LOGOUT", msg.getAction());
        assertEquals("ByeBye", msg.getData());
        assertNull(msg.getId());
        assertNull(msg.getUsername());
        assertNull(msg.getRole());
    }

    @Test
    @DisplayName("Test Constructor 3: Truyền action, id, username, data")
    void testConstructor_ActionIdUsernameData() {
        Message msg = new Message("CHAT", "ROOM_1", "hanto", "Hello Server");

        assertEquals("CHAT", msg.getAction());
        assertEquals("ROOM_1", msg.getId());
        assertEquals("hanto", msg.getUsername());
        assertEquals("Hello Server", msg.getData());
        assertNull(msg.getRole());
    }

    @Test
    @DisplayName("Test Constructor 4: Truyền đầy đủ tất cả tham số (Full)")
    void testConstructor_FullParameters() {
        Message msg = new Message("CREATE_AUCTION", "REQ_1", "hanto", "SELLER", "AuctionData");

        assertEquals("CREATE_AUCTION", msg.getAction());
        assertEquals("REQ_1", msg.getId());
        assertEquals("hanto", msg.getUsername());
        assertEquals("SELLER", msg.getRole());
        assertEquals("AuctionData", msg.getData());
    }
}