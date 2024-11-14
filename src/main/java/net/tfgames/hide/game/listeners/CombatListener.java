package net.tfgames.hide.game.listeners;

import net.tfgames.engine.api.PlayerKillEvent;
import net.tfgames.engine.arena.Arena;
import net.tfgames.engine.arena.ArenaManager;
import net.tfgames.hide.game.HideAndSeek;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class CombatListener implements Listener {

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;

        if (e.getEntity() instanceof Player victim && e.getDamager() instanceof Player attacker) {
            Arena arena = ArenaManager.getArena(victim);
            Arena attackerArena = ArenaManager.getArena(attacker);

            if (attackerArena == null) return;
            if (arena == null) return;
            if (attackerArena != arena) return;

            if (arena.getGame() instanceof HideAndSeek game) {
                if (victim.getHealth() - e.getFinalDamage() <= 0) {
                    e.setCancelled(true);
                    victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 1.0F, 1.0F);

                    if (game.isSeeker(attacker)) {
                        PlayerKillEvent playerKillEvent = new PlayerKillEvent(arena, attacker, victim, "%v% foi encontrado por %k%.");
                        Bukkit.getPluginManager().callEvent(playerKillEvent);
                    }
                    else {
                        PlayerKillEvent playerKillEvent = new PlayerKillEvent(arena, attacker, victim, PlayerKillEvent.KillCause.fromDamageCause(e.getCause()));
                        Bukkit.getPluginManager().callEvent(playerKillEvent);
                    }

                    if (game.isHider(victim)) {
                        game.updateRole(victim, true);
                        game.setTimeLived(victim, game.getCurrentState().getDuration() - game.getPlayingTask().getSeconds());
                    }

                    // Handle teleportation and winner check
                    victim.teleportAsync(game.getConfig().getWaitingSpawn()).thenRun(game::checkWinner);
                    arena.resetPlayer(victim);

                    game.giveInventory(victim);

                    game.addKill(attacker);
                }
            }
        }
    }

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

                    PlayerKillEvent playerKillEvent = new PlayerKillEvent(arena, null, player, PlayerKillEvent.KillCause.fromDamageCause(e.getCause()));
                    Bukkit.getPluginManager().callEvent(playerKillEvent);

                    if (game.isHider(player)) {
                        game.updateRole(player, true);
                        game.setTimeLived(player, game.getCurrentState().getDuration() - game.getPlayingTask().getSeconds());
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
