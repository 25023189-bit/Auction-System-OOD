package com.auction.client.feature.room;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class DefaultAuctionTimerService implements AuctionTimer {
    private final AuctionRoomPresenter presenter;
    private Timeline timeline;

    public DefaultAuctionTimerService(AuctionRoomPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void start(LocalDateTime startTime, LocalDateTime scheduledEndTime) {
        stop();

        if (startTime == null || scheduledEndTime == null) {
            presenter.setTimerText("No time limit", Color.ORANGE);
            return;
        }

        if (LocalDateTime.now().isBefore(startTime)) {
            renderBeforeStart(startTime, scheduledEndTime);
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> renderBeforeStart(startTime, scheduledEndTime)));
            playIndefinitely();
            return;
        }

        if (!LocalDateTime.now().isBefore(scheduledEndTime)) {
            presenter.setTimerText("Ended", Color.RED);
            presenter.disableBidUi("Auction has ended.");
            return;
        }

        renderRemaining(scheduledEndTime);
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> renderRemaining(scheduledEndTime)));
        playIndefinitely();
    }

    private void renderBeforeStart(LocalDateTime startTime, LocalDateTime scheduledEndTime) {
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(startTime)) {
            start(startTime, scheduledEndTime);
            return;
        }

        long millisLeft = ChronoUnit.MILLIS.between(now, startTime);
        long displaySeconds = Math.max(1, (millisLeft + 999) / 1000);
        presenter.setTimerText("Starts in: " + formatDuration(displaySeconds), Color.BLUE);
    }

    private void renderRemaining(LocalDateTime scheduledEndTime) {
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(scheduledEndTime)) {
            presenter.setTimerText("Ended", Color.RED);
            presenter.disableBidUi("Auction has ended.");
            stop();
            return;
        }

        long millisLeft = ChronoUnit.MILLIS.between(now, scheduledEndTime);
        long displaySeconds = Math.max(1, (millisLeft + 999) / 1000);
        presenter.setTimerText(formatDuration(displaySeconds), Color.DARKGREEN);
    }

    private String formatDuration(long totalSeconds) {
        long hh = totalSeconds / 3600;
        long mm = (totalSeconds % 3600) / 60;
        long ss = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
    }

    private void playIndefinitely() {
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    @Override
    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }
}
