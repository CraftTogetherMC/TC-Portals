package de.crafttogether.tcportals.commands;

import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.portals.Portal;
import net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.AudienceUtil;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.portals.Passenger;
import de.crafttogether.tcportals.portals.PortalQueue;
import de.crafttogether.tcportals.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

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
            sendMessage(sender, Localization.CONFIG_RELOADED.deserialize());
        } else if (args.length > 0 && args[0].equals("debug")) {
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
                } else if (args[1].equalsIgnoreCase("receivedTrains"))
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
                        } else {
                            message = Component.text("DebugMode enabled");
                            Util.debugUUIDs.add(player.getUniqueId());
                        }
                    } else {
                        plugin.getLogger().warning("This command can only be ran by players");
                    }
                }
            }

            if (message != null) {
                sendMessage(sender, message);
                return true;
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("tcportals.command.list") && !sender.hasPermission("tcportals.admin")) {
                sender.sendMessage("§cTC-Portals » Keine Berechtigung.");
                return true;
            }

            sendPortalList(sender);
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("ping")) {
            if (!sender.hasPermission("tcportals.command.ping") && !sender.hasPermission("tcportals.admin")) {
                sender.sendMessage("§cTC-Portals » Keine Berechtigung.");
                return true;
            }

            String portalName = args[1];

            Portal portal = TCPortals.plugin.getPortalStorage().getPortals().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(portalName))
                    .filter(p -> p.getType() == Portal.PortalType.OUT
                            || p.getType() == Portal.PortalType.BIDIRECTIONAL)
                    .findFirst()
                    .orElse(null);

            if (portal == null) {
                sender.sendMessage("§6TC-Portals » §cPortal '" + portalName + "' nicht gefunden.");
                return true;
            }

            String host = portal.getTargetHost();
            int port = portal.getTargetPort();

            sender.sendMessage("§6TC-Portals » §7Teste Verbindung zu §e" + portal.getName() + "§7...");

            Bukkit.getScheduler().runTaskAsynchronously(TCPortals.plugin, () -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(host, port), 2000);

                    sender.sendMessage("§6TC-Portals » §aErreichbar: §f" + host + ":" + port);
                } catch (IOException ex) {
                    sender.sendMessage("§6TC-Portals » §cNicht erreichbar: §7" + ex.getMessage());
                }
            });

            return true;
        } else if (args.length == 0) {
            new UpdateChecker(plugin.getPlatformLayer()).checkUpdatesAsync((err, installedVersion, installedBuild, build) -> {
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

                sendMessage(sender, message);
            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        return true;
    }

    public void registerCommand(String cmd, TabExecutor executor) {
        Objects.requireNonNull(plugin.getCommand(cmd)).setExecutor(executor);
        Objects.requireNonNull(plugin.getCommand(cmd)).setTabCompleter(executor);
    }

    private static void sendMessage(CommandSender sender, Component message) {
        if (sender instanceof Player player) {
            AudienceUtil.getPlayer(player.getUniqueId()).sendMessage(message);
            return;
        }

        AudienceUtil.getConsole().sendMessage(message);
    }

    private void sendPortalList(CommandSender sender) {
        Collection<Portal> portals = TCPortals.plugin.getPortalStorage().getPortals();

        if (portals == null || portals.isEmpty()) {
            sender.sendMessage("§6TC-Portals » §eKeine Portale gespeichert.");
            return;
        }

        sender.sendMessage("§6TC-Portals » §aGespeicherte Portale:");

        for (Portal portal : portals) {
            if (portal == null || portal.getTargetLocation() == null)
                continue;

            sender.sendMessage("§8#§7" + portal.getId()
                    + " §e" + portal.getName()
                    + " §8| §b" + portal.getType()
                    + " §8| §7Server: §f" + portal.getTargetLocation().getServer()
                    + " §8| §7World: §f" + portal.getTargetLocation().getWorld()
                    + " §8| §7Host: §f" + portal.getTargetHost() + ":" + portal.getTargetPort()
                    + " §8| §7XYZ: §f"
                    + (int) portal.getTargetLocation().getX() + " "
                    + (int) portal.getTargetLocation().getY() + " "
                    + (int) portal.getTargetLocation().getZ());
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("tcportals.command.debug") || sender.hasPermission("tcportals.admin"))
                suggestions.add("debug");

            if (sender.hasPermission("tcportals.command.reload") || sender.hasPermission("tcportals.admin"))
                suggestions.add("reload");

            if (sender.hasPermission("tcportals.command.list") || sender.hasPermission("tcportals.admin"))
                suggestions.add("list");

            if (sender.hasPermission("tcportals.command.ping") || sender.hasPermission("tcportals.admin"))
                suggestions.add("ping");

            return filterSuggestions(suggestions, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            if (!sender.hasPermission("tcportals.command.debug") && !sender.hasPermission("tcportals.admin"))
                return Collections.emptyList();

            suggestions.add("queuedTrains");
            suggestions.add("receivedTrains");
            suggestions.add("portalQueue");
            suggestions.add("passenger");
            suggestions.add("errors");
            suggestions.add("toggle");

            return filterSuggestions(suggestions, args[1]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("ping")) {
            if (!sender.hasPermission("tcportals.command.ping") && !sender.hasPermission("tcportals.admin"))
                return Collections.emptyList();

            return filterSuggestions(
                    TCPortals.plugin.getPortalStorage().getPortals().stream()
                            .filter(p -> p.getType() == Portal.PortalType.OUT
                                    || p.getType() == Portal.PortalType.BIDIRECTIONAL)
                            .map(Portal::getName)
                            .distinct()
                            .sorted(String.CASE_INSENSITIVE_ORDER)
                            .collect(Collectors.toList()),
                    args[1]
            );
        }

        return Collections.emptyList();
    }

    private List<String> filterSuggestions(List<String> suggestions, String input) {
        String lowerInput = input.toLowerCase(Locale.ROOT);

        return suggestions.stream()
                .filter(suggestion -> suggestion.toLowerCase(Locale.ROOT).startsWith(lowerInput))
                .collect(Collectors.toList());
    }
}
