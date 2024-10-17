package net.tfgames.hide.game;

import lombok.Getter;

@Getter
public enum HideState {

    HIDING_TIME(60, "⌛ Esconda-se"),
    PLAYING(600, "⌚ Tempo");

    private final int duration;
    private final String scoreboardDisplay;

    HideState(int duration, String scoreboardDisplay) {
        this.duration = duration;
        this.scoreboardDisplay = scoreboardDisplay;
    }

}
