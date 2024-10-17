package net.tfgames.hide.game.tasks;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.tfgames.hide.game.HideAndSeek;
import net.tfgames.hide.game.HideState;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@Setter
@Getter
public class PlayingTask extends BukkitRunnable {

    private final Plugin plugin;
    private final HideAndSeek game;
    private int seconds;

    private MiniMessage mm = MiniMessage.miniMessage();

    public PlayingTask(Plugin plugin, HideAndSeek game) {
        this.plugin = plugin;
        this.game = game;
        this.seconds = game.getCurrentState().getDuration();
    }

    public void start() {
        runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void run() {
        switch (game.getCurrentState()) {
            case HIDING_TIME -> {
                if (seconds == 0) {
                    game.changeState(HideState.PLAYING);
                    game.getTeamAssigner().assignTeams(game);
                    break;
                }

                if (getSeconds() <= 5) {
                    game.getArena().sendMessage("<yellow>[⌚] <gray>Os papeís serão revelados em <green>" + getSeconds() + " <gray>segundos!");
                    game.getArena().sendTitle(mm.deserialize("<green>" + seconds), mm.deserialize("<gray>Prepare-se!"), 0, 20, 0);
                    game.getArena().sendSound(p -> Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                }

                if (getSeconds() % 10 == 0) {
                    game.getArena().sendMessage("<yellow>[⌚] <gray>Os papeís serão revelados em <green>" + getSeconds() + " <gray>segundos!");
                    game.getArena().sendSound(p -> Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                }

                seconds--;
            }
            case PLAYING -> {
                if (seconds == 0) {
                    game.endGame(game.getHiderTeam());
                }

                seconds--;
            }
        }
    }

    @Override
    public void cancel() {
        Bukkit.getScheduler().cancelTask(this.getTaskId());
    }
}
