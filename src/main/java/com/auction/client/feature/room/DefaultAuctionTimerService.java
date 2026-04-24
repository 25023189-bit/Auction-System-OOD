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

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(startTime)) {
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                long left = ChronoUnit.SECONDS.between(LocalDateTime.now(), startTime);
                if (left <= 0) {
                    start(startTime, scheduledEndTime);
                } else {
                    long hh = left / 3600;
                    long mm = (left % 3600) / 60;
                    long ss = left % 60;
                    presenter.setTimerText(String.format("Starts in: %02d:%02d:%02d", hh, mm, ss), Color.BLUE);
                }
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
            return;
        }

        if (!now.isBefore(scheduledEndTime)) {
            presenter.setTimerText("Ended", Color.RED);
            presenter.disableBidUi("Auction has ended.");
            return;
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            long left = ChronoUnit.SECONDS.between(LocalDateTime.now(), scheduledEndTime);
            if (left <= 0) {
                presenter.setTimerText("Ended", Color.RED);
                presenter.disableBidUi("Auction has ended.");
                stop();
            } else {
                long hh = left / 3600;
                long mm = (left % 3600) / 60;
                long ss = left % 60;
                presenter.setTimerText(String.format("%02d:%02d:%02d", hh, mm, ss), Color.DARKGREEN);
            }
        }));
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
