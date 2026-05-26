package com.auction.client.feature.lobby;

import com.auction.client.feature.viewmodel.LobbyRoomDisplayModel;
import com.auction.client.service.AuctionService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LobbyUiTest {
    @BeforeAll
    static void initFx() {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    void defaultCardShowsModelDataAndJoinsSelectedRoom() {
        AuctionService service = mock(AuctionService.class);
        DefaultAuctionCardFactory factory = new DefaultAuctionCardFactory(service);
        LobbyRoomDisplayModel model = new LobbyRoomDisplayModel("R1", "Painting", 1500.0, null);

        VBox card = factory.createDefault(model);

        assertEquals(6, card.getChildren().size());
        assertEquals("Painting", ((Label) card.getChildren().get(1)).getText());
        assertEquals("ID: R1", ((Label) card.getChildren().get(2)).getText());
        assertEquals("Price: 1,500 $", ((Label) card.getChildren().get(3)).getText());
        ((Button) card.getChildren().get(4)).fire();
        verify(service).joinRoom("R1");
    }

    @Test
    void rendererClearsRendersSizesAndUpdatesMatchingPriceLabel() {
        FlowPane pane = new FlowPane();
        pane.getStyleClass().add("bidder-auction-grid");
        DefaultAuctionCardFactory factory = mock(DefaultAuctionCardFactory.class);
        LobbyRoomListRenderer renderer = new LobbyRoomListRenderer(pane, factory);
        LobbyRoomDisplayModel first = new LobbyRoomDisplayModel("R1", "One", 10.0);
        LobbyRoomDisplayModel second = new LobbyRoomDisplayModel("R2", "Two", 20.0);
        VBox firstCard = cardWithPrice("Price: 10");
        VBox secondCard = cardWithPrice("Current Price: 20");
        when(factory.createDefault(first)).thenReturn(firstCard);
        when(factory.createDefault(second)).thenReturn(secondCard);

        renderer.render(List.of(first, second));

        assertEquals(2, pane.getChildren().size());
        assertEquals("R1", firstCard.getUserData());
        assertEquals(286.0, firstCard.getPrefHeight());
        renderer.updatePrice("R2", 85.5);
        assertEquals("Price: 85.5", ((Label) secondCard.getChildren().get(0)).getText());
        renderer.updatePrice("missing", 99.0);
        assertEquals("Price: 10", ((Label) firstCard.getChildren().get(0)).getText());

        renderer.render(null);
        assertEquals(0, pane.getChildren().size());
    }

    @Test
    void rendererWithMissingPaneIgnoresRenderAndUpdates() {
        LobbyRoomListRenderer renderer = new LobbyRoomListRenderer(null, mock(DefaultAuctionCardFactory.class));
        assertDoesNotThrow(() -> renderer.render(List.of(new LobbyRoomDisplayModel("R", "Room", 1.0))));
        assertDoesNotThrow(() -> renderer.updatePrice(null, 1.0));
    }

    private VBox cardWithPrice(String text) {
        VBox card = new VBox();
        card.getChildren().add(new Label(text));
        card.getChildren().add(new Label("Other"));
        return card;
    }
}
