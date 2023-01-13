package de.crafttogether;

import de.crafttogether.common.dep.net.kyori.adventure.platform.bukkit.BukkitAudiences;
import de.crafttogether.common.localization.LocalizationManager;
import de.crafttogether.common.mysql.MySQLAdapter;
import de.crafttogether.common.mysql.MySQLConfig;
import de.crafttogether.common.update.BuildType;
import de.crafttogether.common.update.UpdateChecker;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.commands.Commands;
import de.crafttogether.tcportals.listener.*;
import de.crafttogether.tcportals.portals.PortalHandler;
import de.crafttogether.tcportals.portals.PortalStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class TCPortals extends JavaPlugin {
    public static TCPortals plugin;

    private String serverName;
    private MySQLAdapter mySQLAdapter;
    private LocalizationManager localizationManager;
    private PortalStorage portalStorage;
    private PortalHandler portalHandler;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        plugin = this;

        /* Check dependencies */
        if (!getServer().getPluginManager().isPluginEnabled("BKCommonLib")) {
            plugin.getLogger().warning("Couldn't find BKCommonLib");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        if (!getServer().getPluginManager().isPluginEnabled("Train_Carts")) {
            plugin.getLogger().warning("Couldn't find TrainCarts");
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        // Create default config
        saveDefaultConfig();

        // Register Listener
        getServer().getPluginManager().registerEvents(new SignBreakListener(), this);
        getServer().getPluginManager().registerEvents(new TrainExitListener(), this);
        getServer().getPluginManager().registerEvents(new TrainMoveListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new ConnectionErrorListener(), this);

        // Register Commands
        new Commands();

        // Register PluginChannel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Setup MySQLConfig
        MySQLConfig myCfg = new MySQLConfig();
        myCfg.setHost(getConfig().getString("MySQL.Host"));
        myCfg.setPort(getConfig().getInt("MySQL.Port"));
        myCfg.setUsername(getConfig().getString("MySQL.Username"));
        myCfg.setPassword(getConfig().getString("MySQL.Password"));
        myCfg.setDatabase(getConfig().getString("MySQL.Database"));
        myCfg.setTablePrefix(getConfig().getString("MySQL.TablePrefix"));

        // Validate configuration
        if (!myCfg.checkInputs() || myCfg.getDatabase() == null) {
            getLogger().warning("[MySQL]: Invalid configuration! Please check your config.yml");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        serverName = getConfig().getString("Settings.ServerName");

        // Initialize MySQLAdapter
        mySQLAdapter = new MySQLAdapter(this, myCfg);

        // Initialize LocalizationManager
        localizationManager = new LocalizationManager(this, Localization.class,
                getConfig().getString("Settings.Language"), "en_EN", "locales");

        localizationManager.addTagResolver("prefix", Localization.PREFIX.deserialize());

        // Initialize Storage
        portalStorage = new PortalStorage();

        // Initialize PortalHandler
        portalHandler = new PortalHandler(getConfig().getString("Portals.Server.BindAddress"), getConfig().getInt("Portals.Server.Port"));
        portalHandler.registerActionSigns();

        // Check for updates
        if (!getConfig().getBoolean("Settings.Updates.Notify.DisableNotifications")
                && getConfig().getBoolean("Settings.Updates.Notify.Console"))
        {
            new UpdateChecker(this).checkUpdatesAsync((String version, String build, String fileName, Integer fileSize, String url, String currentVersion, String currentBuild, BuildType buildType) -> {
                switch (buildType) {
                    case RELEASE -> plugin.getLogger().warning("A new full version of this plugin was released!");
                    case SNAPSHOT -> plugin.getLogger().warning("A new snapshot version of this plugin is available!");
                }

                plugin.getLogger().warning("You can download it here: " + url);
                plugin.getLogger().warning("Version: " + version + " #" + build);
                plugin.getLogger().warning("FileName: " + fileName + " FileSize: " + UpdateChecker.humanReadableFileSize(fileSize));
                plugin.getLogger().warning("You are on version: " + currentVersion + " #" + currentBuild);

            }, plugin.getConfig().getBoolean("Settings.Updates.CheckForDevBuilds"));
        }

        getLogger().info(getDescription().getName() + " v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }

        // Shutdown MySQL-Adapter
        if(mySQLAdapter != null) {
            mySQLAdapter.disconnect();
            mySQLAdapter = null;
        }

        // Close TCPServer/TCPClients & Unregister ActionSigns
        if (portalHandler != null) {
            portalHandler.shutdown();
            portalHandler = null;
        }
    }

    public String getServerName() {
        return serverName;
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
}
