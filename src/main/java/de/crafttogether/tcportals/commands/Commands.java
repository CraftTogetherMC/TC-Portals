package de.crafttogether.tcportals.commands;

import de.crafttogether.TCPortals;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Commands implements CommandExecutor {
    private static final TCPortals plugin = TCPortals.plugin;

    public Commands() {
        registerCommand("tcportals", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        plugin.getLogger().info("Run command");

        new UpdateChecker(plugin).checkUpdatesAsync((String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, BuildType buildType) -> {
            List<Placeholder> resolvers = new ArrayList<>();
            Component message = null;

            switch (buildType) {
                case RELEASE, SNAPSHOT -> {
                    resolvers.add(Placeholder.set("version", version));
                    resolvers.add(Placeholder.set("build", build));
                    resolvers.add(Placeholder.set("fileName", fileName));
                    resolvers.add(Placeholder.set("fileSize", UpdateChecker.humanReadableFileSize(fileSize)));
                    resolvers.add(Placeholder.set("url", url));
                    resolvers.add(Placeholder.set("currentVersion", currentVersion));
                    resolvers.add(Placeholder.set("currentBuild", currentBuild));

                    switch (buildType) {
                        case RELEASE -> Localization.UPDATE_RELEASE.deserialize(resolvers);
                        case SNAPSHOT -> Localization.UPDATE_DEVBUILD.deserialize(resolvers);
                    }
                }

                case UP2DATE -> {
                    resolvers.add(Placeholder.set("currentVersion", currentVersion));
                    resolvers.add(Placeholder.set("currentBuild", currentBuild));

                    Configuration pluginDescription = PluginUtil.getPluginFile(plugin);
                    String buildNumber = pluginDescription.getString("build");
                    sender.sendMessage(ChatColor.GREEN + "TCDestinations version: " + plugin.getDescription().getVersion() + " #" + buildNumber);

                    message = plugin.getLocalizationManager().miniMessage()
                            .deserialize("<prefix/><gold>TCPortals version: </gold><yellow>" + currentVersion + " #" + currentBuild + "</yellow><newLine/>")
                            .append(Localization.UPDATE_LASTBUILD.deserialize(resolvers));
                }
            }

            if (message != null)
                PluginUtil.adventure().sender(sender).sendMessage(message);
            else
                Util.debug("message is null");

        }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));

        return true;
    }

    public void registerCommand(String cmd, CommandExecutor executor) {
        Objects.requireNonNull(plugin.getCommand(cmd)).setExecutor(executor);
    }
}
