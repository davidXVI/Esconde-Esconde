package net.tfgames.hide.game.components;

import me.catcoder.sidebar.ProtocolSidebar;
import me.catcoder.sidebar.Sidebar;
import net.tfgames.engine.game.modules.GameScoreboard;
import net.tfgames.hide.HideAndSeekTF;
import net.tfgames.hide.game.HideAndSeek;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class HideSidebar implements GameScoreboard {

    private final String serverId = HideAndSeekTF.getInstance().getServerId();
    protected final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    private HideAndSeek game;
    private Map<UUID, Sidebar<?>> sidebars;

    public HideSidebar(HideAndSeek game) {
        this.game = game;
        this.sidebars = new HashMap<>();
    }

    @Override
    public void addSidebar(Player player, String spectated) {
        Sidebar<String> sidebar = ProtocolSidebar.newMiniMessageSidebar("<yellow><bold>ESCONDE-ESCONDE", game.getPlugin());
        sidebar.addLine("  <gray>" + df.format(new Date(System.currentTimeMillis())) + "  <dark_gray>" + serverId + "  ");
        sidebar.addBlankLine();

        if (spectated != null) {
            sidebar.addLine(" ∞ Assistindo: " + spectated);
            sidebar.addBlankLine();
        }

        sidebar.addUpdatableLine(p -> " " + game.getCurrentState().getScoreboardDisplay() + ": <green>" + String.format("%02d:%02d", game.getPlayingTask().getSeconds() / 60, game.getPlayingTask().getSeconds() % 60));
        sidebar.addBlankLine();

        sidebar.addUpdatableLine(p -> " \uD83C\uDFF9 Escondendo: <green>" + game.getHiderTeam().size());
        sidebar.addUpdatableLine(p -> " \uD83D\uDDE1 Procurando: <green>" + game.getSeekerTeam().size());
        sidebar.addBlankLine();

        sidebar.addConditionalLine(p -> " \uD83C\uDFA3 Encontrados: <green>" + game.getKills(p), game::isSeeker);
        sidebar.addConditionalLine(p -> " ", game::isSeeker);

        sidebar.addUpdatableLine(p -> " ⛨ Função: " + (game.getTeam(p) != null ? game.getTeam(p).getRichName() : "<gray>Nenhuma"));
        sidebar.addLine(" ⛏ Mapa: <green>" + game.getMap());

        sidebar.addBlankLine();

        sidebar.addLine(" <yellow>jogar.tfgames.com.br ");

        sidebar.getObjective().scoreNumberFormatBlank();
        sidebar.updateLinesPeriodically(0, 20, true);

        sidebar.addViewer(player);
        sidebars.put(player.getUniqueId(), sidebar);
    }

    @Override
    public void removeSidebar(Player player) {
        if (sidebars.containsKey(player.getUniqueId())) {
            sidebars.get(player.getUniqueId()).destroy();
            sidebars.remove(player.getUniqueId());
        }
    }

    @Override
    public void destroy() {
        if (sidebars != null && !sidebars.isEmpty()) {
            for (Sidebar<?> sidebar : sidebars.values()) {
                sidebar.destroy();
            }
            sidebars.clear();
            this.sidebars = null;
        }
        this.game = null;
    }

}
