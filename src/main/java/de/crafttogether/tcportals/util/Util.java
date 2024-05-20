package de.crafttogether.tcportals.util;

import com.bergerkiller.bukkit.common.chunk.ForcedChunk;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableMember;

import de.crafttogether.TCPortals;
import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.util.AudienceUtil;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcportals.Localization;

import de.crafttogether.common.shaded.net.kyori.adventure.text.Component;
import de.crafttogether.common.shaded.net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import de.crafttogether.common.shaded.net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {
    public static final TCPortals plugin = TCPortals.plugin;

    public static List<UUID> debugUUIDs = new ArrayList<>();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkPermission(Player player, String permission) {
        return checkPermission((CommandSender) player, permission);
    }

    public static boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            Localization.ERROR_NOPERMISSION.message(sender,
                    Placeholder.set("permission", permission));
            return false;
        }
        return true;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static List<ForcedChunk> loadChunks(SpawnableGroup.SpawnLocationList spawnLocations, int surrounding, int unloadDelaySeconds) {
        List<ForcedChunk> forcedChunks = new ArrayList<>();
        spawnLocations.loadChunks();

        for (SpawnableMember.SpawnLocation spawnLocation : spawnLocations.locations)
            forcedChunks.addAll(loadChunks(spawnLocation.location.getChunk(), surrounding, unloadDelaySeconds));

        return forcedChunks;
    }

    public static List<ForcedChunk> loadChunks(Chunk chunk, int surrounding, int unloadDelaySeconds) {
        List<ForcedChunk> forcedChunks = new ArrayList<>();
        World world = chunk.getWorld();
        int chunk_x = chunk.getX();
        int chunk_z = chunk.getZ();

        for (int x = chunk_x - surrounding; x < chunk_x + surrounding; x++) {
            for (int z = chunk_z - surrounding; z < chunk_z + surrounding; z++) {
                ForcedChunk forcedChunk = ForcedChunk.load(world.getChunkAt(x, z));
                forcedChunk.getChunk();
                forcedChunks.add(forcedChunk);
            }
        }

        if (unloadDelaySeconds > 0) {
            Bukkit.getScheduler().runTaskLater(TCPortals.plugin, () -> {
                for (ForcedChunk forcedChunk : forcedChunks)
                    forcedChunk.close();
            }, 20L * unloadDelaySeconds);
        }

        return forcedChunks;
    }

    public static void debug(String message, boolean broadcast) {
        if (!TCPortals.plugin.getConfig().getBoolean("Settings.Debug"))
            return;

        Component messageComponent = LegacyComponentSerializer.legacyAmpersand().deserialize("&7&l[Debug]: &r" + message);

        // Broadcast to online players with permission
        if (broadcast) {
            for (Player player : Bukkit.getOnlinePlayers())
                if (debugUUIDs.contains(player.getUniqueId()))
                    AudienceUtil.getPlayer(player.getUniqueId()).sendMessage(messageComponent);
        }

        plugin.getLogger().info(PlainTextComponentSerializer.plainText().serialize(messageComponent));
    }

    public static void debug(Component message, boolean broadcast) {
        debug(LegacyComponentSerializer.legacyAmpersand().serialize(message), broadcast);
    }

    public static void debug(String message) {
        debug(Component.text(message), true);
    }
    
    public static void debug(Component message) {
        debug(message, true);
    }
}
