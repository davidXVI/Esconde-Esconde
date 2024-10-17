package net.tfgames.hide.game;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.tfgames.common.api.game.PackedGame;
import net.tfgames.common.api.game.settings.GameType;
import net.tfgames.common.api.game.settings.gamerule.GameRule;
import net.tfgames.engine.arena.ArenaState;
import net.tfgames.engine.game.Game;
import net.tfgames.engine.team.ArenaTeam;
import net.tfgames.engine.utils.item.ItemBuilder;
import net.tfgames.hide.config.HideAndSeekConfig;
import net.tfgames.hide.game.components.HideSidebar;
import net.tfgames.hide.game.tasks.PlayingTask;
import net.tfgames.hide.game.team.SeekerAssigner;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

@Getter
@Setter
public class HideAndSeek extends Game {

    private HideAndSeek gameInstance;
    private Random random;

    private HideAndSeekConfig config;

    private PlayingTask playingTask;
    private HideState currentState;

    public HideAndSeek(Plugin plugin, PackedGame packedGame) {
        super(plugin, packedGame);

        this.gameInstance = this;
        this.random = new Random();

        this.currentState = HideState.HIDING_TIME;
        this.playingTask = new PlayingTask(plugin, this);

        this.teamAssigner = new SeekerAssigner();
    }

    @Override
    public void init(String s, String s1) {
        this.config = new HideAndSeekConfig(s, s1);
    }

    @Override
    public void start() {
        teamAssigner.setupTeams(this);
        changeState(HideState.HIDING_TIME);
        playingTask.start();

        setupComponents();
        teleportPlayers();
    }

    //Component Management
    public void setupComponents() {
        this.scoreboard = new HideSidebar(this);

        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                addComponents(player, null);
                player.clearTitle();
            }
        }
    }

    //Teleport Players
    public void teleportPlayers() {
        arena.getPlayers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                player.clearTitle();
                player.teleportAsync(config.getWaitingSpawn());
            }
        });
    }

    @Override
    public void addComponents(Player player, String s) {
        if (scoreboard != null) {
            scoreboard.removeSidebar(player);
            scoreboard.addSidebar(player, s);
        }

        Component lobbyHeader = mm.deserialize("\n<gradient:#57e86b:#fefe34><bold>ᴛꜰɢᴀᴍᴇѕ</gradient> <yellow>— <white><bold>ᴇѕᴄᴏɴᴅᴇ-ᴇѕᴄᴏɴᴅᴇ\n");
        Component footerIP = mm.deserialize(" \n<reset>ɪᴘ: <gold>ᴊᴏɢᴀʀ.ᴛꜰɢᴀᴍᴇѕ.ᴄᴏᴍ.ʙʀ \n");
        Component footerSV = mm.deserialize(" <reset>ᴅɪѕᴄᴏʀᴅ: <blue>ᴅᴄ.ᴛꜰɢᴀᴍᴇѕ.ᴄᴏᴍ.ʙʀ \n");
        Component lobbyFooter = footerIP.append(footerSV);
        player.sendPlayerListHeaderAndFooter(lobbyHeader, lobbyFooter);
    }

    //Game Management
    public void checkWinner() {
        //Arena is Empty, end the game with a winner
        if (arena.getPlayers().isEmpty() || arena.getPlayers().size() == 1) {
            endGame(null);
            return;
        }

        List<UUID> hiders = getHiderTeam().getMembers();
        List<UUID> seekers = getSeekerTeam().getMembers();

        if (hiders.isEmpty()) {
            endGame(getSeekerTeam());
            return;
        }

        if (seekers.isEmpty()) {
            endGame(getHiderTeam());
        }
    }

    public void endGame(ArenaTeam winner) {
        if (winner != null) {
            endMessage(winner);
            arena.changeState(ArenaState.RESTARTING);
        }
        else {
            arena.changeState(ArenaState.RESTARTING);
        }
    }

    public void endMessage(ArenaTeam winner) {
        arena.sendMessage("<gold><bold><st>                                                                 ");
        arena.sendCenteredMessage("<yellow><bold>ᴇѕᴄᴏɴᴅᴇ-ᴇѕᴄᴏɴᴅᴇ");
        arena.sendMessage(" ");
        arena.sendCenteredMessage("<yellow>Vencedor - " + winner.getRichName());
        arena.sendMessage(" ");
        arena.sendMessage("<gold><bold><st>                                                                 ");

        for (UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);

            if(player != null) {
                if (getTeam(player) == winner) {
                    player.showTitle(Title.title(mm.deserialize("<green><bold>VITÓRIA!"), mm.deserialize("Você venceu a Partida!")));
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0F , 1.0F);
                }
                else {
                    player.showTitle(Title.title(mm.deserialize("<red><bold>DERROTA!"), mm.deserialize("Não foi dessa vez :(")));
                    player.playSound(player, Sound.ENTITY_WITHER_DEATH, 1.0F , 1.0F);
                }
            }
        }
    }

    // State Management
    public void changeState(HideState state) {
        playingTask.setSeconds(state.getDuration());
        this.currentState = state;

        if (state == HideState.HIDING_TIME) {
            getGameRules().setValue(GameRule.PLAYER_DAMAGE, false);
        }
        else {
            getGameRules().setValue(GameRule.PLAYER_DAMAGE, true);
        }
    }

    // Inventory Management
    public void giveInventory(Player player) {
        player.getInventory().clear();

        Color armorColor = null;

        switch (getTeam(player).getTeamName()) {
            case "Escondedor" -> {
                armorColor = Color.BLUE;
                player.getInventory().addItem(new ItemBuilder(Material.STICK).setName("<green>Pau").addEnchantment(Enchantment.KNOCKBACK, 2).build());
            }
            case "Procurador" -> {
                armorColor = Color.RED;
                player.getInventory().addItem(new ItemBuilder(Material.BLAZE_ROD).setName("<red>Pau Dourado").addEnchantment(Enchantment.SHARPNESS, 5).build());
            }
        }

        ItemStack chestPlate = new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(armorColor).setName("<yellow>Peitoral").setUnbreakable(true).build();
        ItemStack boots = new ItemBuilder(Material.LEATHER_BOOTS).setColor(armorColor).setName("<yellow>Bota").addEnchantment(Enchantment.PROTECTION, 4).setUnbreakable(true).build();

        player.getInventory().setBoots(boots);
        player.getInventory().setChestplate(chestPlate);
    }

    public void updateRole(Player player, boolean isSeeker) {
        // Update player roles in teams
        if (isSeeker) {
            getHiderTeam().removePlayers(player);
            getSeekerTeam().addPlayers(player);

            player.showTitle(Title.title(mm.deserialize("<gray>Você está: <red>PROCURANDO"), mm.deserialize("<gray>ᴇɴᴄᴏɴᴛʀᴇ ᴏѕ ᴏᴜᴛʀᴏѕ ᴊᴏɢᴀᴅᴏʀᴇѕ")));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 2.0F, 2.0F);
        }
        else {
            getSeekerTeam().removePlayers(player);
            getHiderTeam().addPlayers(player);

            player.showTitle(Title.title(mm.deserialize("<gray>Você está: <green>ESCONDENDO"), mm.deserialize("<gray>ᴇѕᴄᴏɴᴅᴀ-ѕᴇ ᴅᴏ ᴘʀᴏᴄᴜʀᴀᴅᴏʀ")));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 2.0F, 2.0F);
        }

        giveInventory(player);
    }

    // Team Utils
    public ArenaTeam getHiderTeam() {
        return getTeam("Escondedor");
    }

    public ArenaTeam getSeekerTeam() {
        return getTeam("Procurador");
    }

    public boolean isSeeker(Player player) {
        return getSeekerTeam().containsPlayer(player);
    }

    public boolean isHider(Player player) {
        return getHiderTeam().containsPlayer(player);
    }

    @Override
    public void destroyData() {
        gameInstance = null;
        random = null;
        config = null;
        currentState = null;

        if (playingTask != null) {
            playingTask.cancel();
        }
        playingTask = null;
    }


    @Override
    public GameType getType() {
        return GameType.HIDE_AND_SEEK;
    }
}
