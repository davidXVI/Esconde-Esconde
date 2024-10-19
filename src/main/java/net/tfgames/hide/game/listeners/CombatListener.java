package net.tfgames.hide.game.listeners;

import net.tfgames.engine.arena.Arena;
import net.tfgames.engine.arena.ArenaManager;
import net.tfgames.hide.game.HideAndSeek;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class CombatListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;

        if (e.getEntity() instanceof Player player) {
            Arena arena = ArenaManager.getArena(player);

            if (arena == null) return;
            if (arena.getGame() instanceof HideAndSeek game) {
                if (player.getHealth() - e.getFinalDamage() <= 0) {
                    e.setCancelled(true);
                    player.playSound(player, Sound.ENTITY_PLAYER_HURT, 1.0F, 1.0F);

                    if (game.isHider(player)) {
                        game.updateRole(player, true);
                    }

                    // Handle teleportation and winner check
                    player.teleportAsync(game.getConfig().getWaitingSpawn()).thenRun(game::checkWinner);
                    arena.resetPlayer(player);

                    game.giveInventory(player);
                }
            }
        }
    }
}
