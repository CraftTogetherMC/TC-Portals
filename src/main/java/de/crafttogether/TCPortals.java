package de.crafttogether;

import de.crafttogether.common.dep.org.bstats.bukkit.Metrics;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.common.util.PluginUtil;
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
    private PortalStorage portalStorage;
    private PortalHandler portalHandler;

    @Override
    public void onEnable() {
        plugin = this;

        PluginManager pluginManager = getServer().getPluginManager();
        
        /* Check dependencies */
        if (!pluginManager.isPluginEnabled("CTCommons")) {
            plugin.getLogger().warning("Couldn't find plugin: CTCommons");
            pluginManager.disablePlugin(plugin);
            return;
        }

        /* Check dependencies */
        if (!pluginManager.isPluginEnabled("BKCommonLib")) {
            plugin.getLogger().warning("Couldn't find BKCommonLib");
            pluginManager.disablePlugin(plugin);
            return;
        }

        if (!pluginManager.isPluginEnabled("Train_Carts")) {
            plugin.getLogger().warning("Couldn't find TrainCarts");
            pluginManager.disablePlugin(plugin);
            return;
        }

        // Create default config
        saveDefaultConfig();

        // Set serverName
        serverName = getConfig().getString("Settings.ServerName");

        // Register Listener
        pluginManager.registerEvents(new SignBreakListener(), this);
        pluginManager.registerEvents(new TrainExitListener(), this);
        pluginManager.registerEvents(new TrainMoveListener(), this);
        pluginManager.registerEvents(new PlayerJoinListener(), this);
        pluginManager.registerEvents(new PlayerLeaveListener(), this);
        pluginManager.registerEvents(new PlayerSpawnListener(), this);
        pluginManager.registerEvents(new CreatureSpawnListener(), this);
        pluginManager.registerEvents(new ConnectionErrorListener(), this);

        // Register PluginChannel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(this, Localization.class, "en_EN", "locales");
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
            new UpdateChecker(this).checkUpdatesAsync((err, build, currentVersion, currentBuild) -> {
                if (err != null)
                    err.printStackTrace();

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
                    plugin.getLogger().warning("You are on version: " + currentVersion + " (build: " + currentBuild + ")");
                });
            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        // bStats
        new Metrics(this, 17418);

        String build = PluginUtil.getPluginFile(this).getString("build");
        getLogger().info(plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + " (build: " + build + ") enabled.");
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
}
