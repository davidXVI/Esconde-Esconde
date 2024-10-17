package net.tfgames.hide.config;

import net.tfgames.engine.config.GameConfig;
import net.tfgames.hide.HideAndSeekTF;

public class HideAndSeekConfig extends GameConfig {

    public HideAndSeekConfig(String mapName, String world) {
        super(HideAndSeekTF.getInstance(), mapName, world);
    }

}
