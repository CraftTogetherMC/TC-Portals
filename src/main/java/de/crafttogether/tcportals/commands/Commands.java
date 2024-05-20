package de.crafttogether.tcportals.commands;

import de.crafttogether.TCPortals;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.shaded.net.kyori.adventure.text.Component;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.AudienceUtil;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.portals.Passenger;
import de.crafttogether.tcportals.portals.PortalQueue;
import de.crafttogether.tcportals.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Commands implements TabExecutor {
    private static final TCPortals plugin = TCPortals.plugin;

    public Commands() {
        registerCommand("tcportals", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0 && args[0].equals("reload")) {
            plugin.getLogger().info("Disconnecting portal-storage...");
            plugin.getPortalStorage().disconnect();

            plugin.getLogger().info("Reloading config.yml...");
            plugin.reloadConfig();

            plugin.getLogger().info("Reloading localization...");
            plugin.getLocalizationManager().loadLocalization(plugin.getConfig().getString("Settings.Language"));

            plugin.getLogger().info("Reconnecting portal-storage...");
            plugin.getPortalStorage().connect();

            plugin.getLogger().info("Reload completed...");
            AudienceUtil.Bukkit.audiences.sender(sender).sendMessage(Localization.CONFIG_RELOADED.deserialize());
        }

        else if (args.length > 0 && args[0].equals("debug")) {
            Component message = Component.empty();

            if (args.length == 1)
                message = null;

            else {
                if (args[1].equalsIgnoreCase("queuedTrains")) {
                    int queuedTrains = 0;

                    for (PortalQueue portalQueue : plugin.getPortalHandler().getPortalQueues().values())
                        queuedTrains += portalQueue.getQueuedTrains().size();

                    message = Component.text("There are " + plugin.getPortalHandler().getPortalQueues().values().size() + " open PortalQueues");
                    message = message.append(Component.text("With a total of " + queuedTrains + " queued."));
                }
                else if (args[1].equalsIgnoreCase("receivedTrains"))
                    message = Component.text("There are " + plugin.getPortalHandler().getReceivedTrains().values().size() + " receivedTrains cached");

                else if (args[1].equalsIgnoreCase("pendingTeleports"))
                    message = Component.text("There are " + plugin.getPortalHandler().getPendingTeleports().values().size() + " pendingTeleports cached");

                else if (args[1].equalsIgnoreCase("passengers"))
                    message = Component.text("There are " + Passenger.passengers().values().size() + " passengers cached");

                else if (args[1].equalsIgnoreCase("errors"))
                    message = Component.text("There are " + Passenger.errors().values().size() + " errors cached");

                else if (args[1].equalsIgnoreCase("toggle")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (Util.debugUUIDs.contains(player.getUniqueId())) {
                            message = Component.text("DebugMode disabled");
                            Util.debugUUIDs.remove(player.getUniqueId());
                        }
                        else {
                            message = Component.text("DebugMode enabled");
                            Util.debugUUIDs.add(player.getUniqueId());
                        }
                    }
                    else {
                        plugin.getLogger().warning("This command can only be ran by players");
                    }
                }
            }

            if (message != null) {
                AudienceUtil.Bukkit.audiences.sender(sender).sendMessage(message);
                return true;
            }
        }

        else if(args.length == 0) {
            new UpdateChecker(TCPortals.platformLayer).checkUpdatesAsync((err, installedVersion, installedBuild, build) -> {
                if (err != null) {
                    plugin.getLogger().warning("An error occurred while receiving update information.");
                    plugin.getLogger().warning("Error: " + err.getMessage());
                }

                List<Placeholder> resolvers = new ArrayList<>();
                Component message;

                if (build == null) {
                    resolvers.add(Placeholder.set("currentVersion", installedVersion));
                    resolvers.add(Placeholder.set("currentBuild", installedBuild));

                    message = plugin.getLocalizationManager().miniMessage()
                            .deserialize("<prefix/><gold>" + plugin.getName() + " version: </gold><yellow>" + installedVersion + " #" + installedBuild + "</yellow><newLine/>");

                    if (err == null)
                        message = message.append(Localization.UPDATE_LASTBUILD.deserialize(resolvers));
                    else
                        message = message.append(Localization.UPDATE_ERROR.deserialize(
                                Placeholder.set("error", err.getMessage())));
                } else {
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
                }

                AudienceUtil.Bukkit.audiences.sender(sender).sendMessage(message);
            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        return true;
    }

    public void registerCommand(String cmd, TabExecutor executor) {
        Objects.requireNonNull(plugin.getCommand(cmd)).setExecutor(executor);
        Objects.requireNonNull(plugin.getCommand(cmd)).setTabCompleter(executor);
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("debug");
            suggestions.add("reload");
        }

        if (args.length == 2) {
            suggestions.add("queuedTrains");
            suggestions.add("receivedTrains");
            suggestions.add("portalQueue");
            suggestions.add("passenger");
            suggestions.add("errors");
            suggestions.add("toggle");
        }

        return suggestions;
    }
}
