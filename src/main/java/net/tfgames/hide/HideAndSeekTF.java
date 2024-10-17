package net.tfgames.hide;

import lombok.Getter;
import net.tfgames.common.api.game.settings.GameType;
import net.tfgames.engine.EngineTF;
import net.tfgames.hide.game.listeners.CombatListener;
import net.tfgames.hide.messaging.GameMessagingService;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class HideAndSeekTF extends JavaPlugin {

    @Getter
    private static HideAndSeekTF instance;
    private String serverId;

    @Override
    public void onEnable() {
        instance = this;

        GameMessagingService.init();
        EngineTF.registerGame(GameType.HIDE_AND_SEEK);

        registerListeners(
                new CombatListener()
        );
    }

    @Override
    public void onDisable() {
        EngineTF.unRegisterGame(GameType.HIDE_AND_SEEK);
    }

    public void registerListeners(Listener... listeners) {
        for (Listener l : listeners) {
            Bukkit.getPluginManager().registerEvents(l, this);
        }
    }

    public String getServerId() {
        return EngineTF.getServerId();
    }
}
