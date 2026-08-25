package de.crafttogether;

import de.crafttogether.common.plugin.BukkitPlatformLayer;
import de.crafttogether.common.plugin.PlatformAbstractionLayer;
import de.crafttogether.common.plugin.PluginInformation;
import de.crafttogether.common.shaded.org.bstats.bukkit.Metrics;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.commands.Commands;
import de.crafttogether.tcportals.listener.*;
import de.crafttogether.tcportals.portals.PortalHandler;
import de.crafttogether.tcportals.portals.PortalStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TCPortals extends JavaPlugin {
    public static TCPortals plugin;

    private String serverName;
    private LocalizationManager localizationManager;
    public static PlatformAbstractionLayer platformLayer;
    private PortalStorage portalStorage;
    private PortalHandler portalHandler;

    @Override
    public void onEnable() {
        plugin = this;

        PluginManager pluginManager = getServer().getPluginManager();

        /* Check dependencies before using any of their classes */
        if (!pluginManager.isPluginEnabled("CTCommonsBukkit")) {
            plugin.getLogger().severe("[TCPortals] Required dependency 'CTCommonsBukkit' was not found or is not enabled! Disabling plugin.");
            pluginManager.disablePlugin(plugin);
            return;
        }

        if (!pluginManager.isPluginEnabled("BKCommonLib")) {
            plugin.getLogger().severe("[TCPortals] Required dependency 'BKCommonLib' was not found or is not enabled! Disabling plugin.");
            pluginManager.disablePlugin(plugin);
            return;
        }

        if (!pluginManager.isPluginEnabled("Train_Carts")) {
            plugin.getLogger().severe("[TCPortals] Required dependency 'Train_Carts' was not found or is not enabled! Disabling plugin.");
            pluginManager.disablePlugin(plugin);
            return;
        }

        platformLayer = new BukkitPlatformLayer(this);

        // Create default config
        saveDefaultConfig();

        // Set serverName
        serverName = getConfig().getString("Settings.ServerName");

        // Register Listener
        pluginManager.registerEvents(new SignBreakListener(), this);
        pluginManager.registerEvents(new TrainExitListener(), this);
        pluginManager.registerEvents(new TrainMoveListener(), this);
        pluginManager.registerEvents(new PlayerJoinListener(), this);
        pluginManager.registerEvents(new PlayerQuitListener(), this);
        // CreatureSpawnListener is intentionally not registered: entity passengers are re-mounted directly
        // in PortalHandler.receiveEntity() with retry timing.
        pluginManager.registerEvents(new ConnectionErrorListener(), this);

        // Register PluginChannel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(platformLayer, Localization.class, "en_EN", "locales");
        localizationManager.loadLocalization(getConfig().getString("Settings.Language"));
        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        // Initialize Storage
        portalStorage = new PortalStorage();
        if (!portalStorage.isActive()) {
            pluginManager.disablePlugin(plugin);
            return;
        }

        // Initialize PortalHandler
        portalHandler = new PortalHandler(getConfig().getString("Portals.Server.BindAddress"), getConfig().getInt("Portals.Server.Port"));

        // Register Commands
        new Commands();

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
                && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(platformLayer).checkUpdatesAsync((err, installedVersion, installedBuild, build) -> {
                if (err != null) {
                    plugin.getLogger().warning("An error occurred while receiving update information.");
                    plugin.getLogger().warning("Error: " + err.getMessage());
                }

                if (build == null)
                    return;

                // Go sync again to avoid mixing output with other plugins
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (build.getType().equals(BuildType.RELEASE))
                        plugin.getLogger().warning("A new full version of this plugin was released!");
                    else
                        plugin.getLogger().warning("A new development version of this plugin is available!");

                    plugin.getLogger().warning("You can download it here: " + build.getUrl());
                    plugin.getLogger().warning("Version: " + build.getVersion() + " (build: " + build.getNumber() + ")");
                    plugin.getLogger().warning("FileName: " + build.getFileName() + " FileSize: " + build.getHumanReadableFileSize());
                    plugin.getLogger().warning("You are on version: " + installedVersion + " (build: " + installedBuild + ")");
                });
            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        // bStats
        new Metrics(this, 17418);

        PluginInformation pluginInformation = platformLayer.getPluginInformation();
        getLogger().info(pluginInformation.getName() + " v" + pluginInformation.getVersion() + " (build: " + pluginInformation.getBuild() + ") enabled.");
    }

    @Override
    public void onDisable() {
        // Shutdown MySQL-Adapter
        if (portalStorage != null) {
            portalStorage.disconnect();
            portalStorage = null;
        }

        // Shutdown TCPServer/TCPClients, Unregister ActionSigns
        if (portalHandler != null) {
            portalHandler.shutdown();
            portalHandler = null;
        }
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }

    public PortalStorage getPortalStorage() {
        return portalStorage;
    }

    public PortalHandler getPortalHandler() {
        return portalHandler;
    }

    public String getServerName() {
        return serverName;
    }

    public PlatformAbstractionLayer getPlatformLayer() {
        return platformLayer;
    }
}
