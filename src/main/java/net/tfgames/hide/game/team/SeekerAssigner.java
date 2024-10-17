package net.tfgames.hide.game.team;

import net.kyori.adventure.text.format.NamedTextColor;
import net.tfgames.engine.arena.Arena;
import net.tfgames.engine.game.Game;
import net.tfgames.engine.team.ArenaTeam;
import net.tfgames.engine.team.TeamAssigner;
import net.tfgames.engine.team.TeamColor;
import net.tfgames.hide.game.HideAndSeek;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.Random;

public class SeekerAssigner implements TeamAssigner {

    @Override
    public void setupTeams(Game game) {
        ArenaTeam hiders = new ArenaTeam(game.getArena(), "Escondedor", TeamColor.BLUE);
        ArenaTeam seekers = new ArenaTeam(game.getArena(), "Procurador", TeamColor.RED);

        seekers.getBukkitTeam().color(NamedTextColor.RED);
        seekers.getBukkitTeam().setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);
        seekers.getBukkitTeam().setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        seekers.getBukkitTeam().setAllowFriendlyFire(false);

        hiders.getBukkitTeam().color(NamedTextColor.BLUE);
        hiders.getBukkitTeam().setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);
        hiders.getBukkitTeam().setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
        hiders.getBukkitTeam().setAllowFriendlyFire(false);

        game.getTeams().add(hiders);
        game.getTeams().add(seekers);
    }

    @Override
    public void assignTeams(Game game) {
        Arena arena = game.getArena();
        HideAndSeek hideAndSeek = (HideAndSeek) game;

        //Set Seeker
        Player seeker = Bukkit.getPlayer(arena.getPlayers().get(new Random().nextInt(arena.getPlayers().size())));

        if (seeker != null) {
            hideAndSeek.updateRole(seeker, true);

            //Set the others as Hiders
            arena.getPlayers().forEach(uuid -> {
                Player hider = Bukkit.getPlayer(uuid);
                if (hider != null && hider != seeker) {
                    hideAndSeek.updateRole(hider, false);
                }
            });
        }
    }
}
