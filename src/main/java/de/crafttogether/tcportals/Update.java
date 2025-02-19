package de.crafttogether.tcportals;

import de.crafttogether.TCPortals;
import de.crafttogether.common.Consumer;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.shaded.net.kyori.adventure.text.Component;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;


import java.util.ArrayList;
import java.util.List;

public class Update {
    public static void check(Consumer consumer, Long delay) {
        TCPortals.platformLayer.getPluginLogger().warn("CHECK()");
        new UpdateChecker(TCPortals.platformLayer).checkUpdatesAsync("TCPortals", (err, installedVersion, installedBuild, build) -> {
            if (err != null) {
                TCPortals.platformLayer.getPluginLogger().warn("An error occurred while receiving update information.");
                TCPortals.platformLayer.getPluginLogger().warn("Error: " + err.getMessage());
                consumer.operation(err, Component.text(err.getMessage()));
            }

            if (build == null) {
                TCPortals.platformLayer.getPluginLogger().warn("BUILD NULL");
                return;
            }

            List<Placeholder> resolvers = new ArrayList<>();
            resolvers.add(Placeholder.set("installedVersion", installedVersion));
            resolvers.add(Placeholder.set("installedBuild", installedBuild));
            resolvers.add(Placeholder.set("currentVersion", build.getVersion()));
            resolvers.add(Placeholder.set("currentBuild", build.getNumber()));
            resolvers.add(Placeholder.set("fileName", build.getFileName()));
            resolvers.add(Placeholder.set("fileSize", build.getHumanReadableFileSize()));
            resolvers.add(Placeholder.set("url", build.getUrl()));

            if (build.getType().equals(BuildType.RELEASE))
                consumer.operation(err, Localization.UPDATE_RELEASE.deserialize(resolvers));
            else
                consumer.operation(err, Localization.UPDATE_DEVBUILD.deserialize(resolvers));

        }, TCPortals.plugin.getConfig().getBoolean("Updates.CheckForDevBuilds"), delay);
    }
}

