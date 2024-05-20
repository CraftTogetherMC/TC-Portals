package de.crafttogether.tcportals.listener;

import de.crafttogether.TCPortals;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.shaded.net.kyori.adventure.text.Component;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.AudienceUtil;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.portals.Passenger;
import de.crafttogether.tcportals.portals.PortalHandler;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerJoinListener implements Listener {
    private static final TCPortals plugin = TCPortals.plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Look if player should be a passenger
        Passenger passenger = Passenger.get(event.getPlayer().getUniqueId());
        if (passenger != null)
            PortalHandler.reEnterPlayer(passenger, event);

        if (!event.getPlayer().hasPermission("tcportals.notify.updates"))
            return;

        Configuration config = plugin.getConfig();

        if (config.getBoolean("Settings.Updates.Notify.DisableNotifications")
                || !config.getBoolean("Settings.Updates.Notify.InGame"))
            return;

        new UpdateChecker(TCPortals.platformLayer).checkUpdatesAsync((err, installedVersion, installedBuild, build) -> {
            if (err != null) {
                plugin.getLogger().warning("An error occurred while receiving update information.");
                plugin.getLogger().warning("Error: " + err.getMessage());
            }

            if (build == null)
                return;

            List<Placeholder> resolvers = new ArrayList<>();
            Component message;

            resolvers.add(Placeholder.set("version", build.getVersion()));
            resolvers.add(Placeholder.set("build", build.getNumber()));
            resolvers.add(Placeholder.set("fileName", build.getFileName()));
            resolvers.add(Placeholder.set("fileSize", build.getHumanReadableFileSize()));
            resolvers.add(Placeholder.set("url", build.getUrl()));
            resolvers.add(Placeholder.set("currentVersion", installedVersion));
            resolvers.add(Placeholder.set("currentBuild", installedBuild));

            if (build.getType().equals(BuildType.RELEASE))
                message = Localization.UPDATE_RELEASE.deserialize(resolvers);
            else
                message = Localization.UPDATE_DEVBUILD.deserialize(resolvers);

            AudienceUtil.getPlayer(event.getPlayer().getUniqueId()).sendMessage(message);
        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"), 40L);
    }
}