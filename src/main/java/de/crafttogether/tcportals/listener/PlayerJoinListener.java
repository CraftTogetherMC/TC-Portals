package de.crafttogether.tcportals.listener;

import de.crafttogether.TCPortals;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcportals.Localization;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerJoinListener implements Listener {
    private static final TCPortals plugin = TCPortals.plugin;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission("tcdestinations.notify.updates"))
            return;

        Configuration config = plugin.getConfig();

        if (config.getBoolean("Settings.Updates.Notify.DisableNotifications")
                || !config.getBoolean("Settings.Updates.Notify.InGame"))
            return;

        new UpdateChecker(plugin).checkUpdatesAsync((String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, BuildType buildType) -> {
            List<Placeholder> resolvers = new ArrayList<>();
            Component message;

            resolvers.add(Placeholder.set("version", version));
            resolvers.add(Placeholder.set("build", build));
            resolvers.add(Placeholder.set("fileName", fileName));
            resolvers.add(Placeholder.set("fileSize", UpdateChecker.humanReadableFileSize(fileSize)));
            resolvers.add(Placeholder.set("url", url));
            resolvers.add(Placeholder.set("currentVersion", currentVersion));
            resolvers.add(Placeholder.set("currentBuild", currentBuild));

            if (buildType.equals(BuildType.RELEASE))
                message = Localization.UPDATE_RELEASE.deserialize(resolvers);
            else
                message = Localization.UPDATE_DEVBUILD.deserialize(resolvers);

            PluginUtil.adventure().player(event.getPlayer()).sendMessage(message);
        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"), 40L);
    }
}