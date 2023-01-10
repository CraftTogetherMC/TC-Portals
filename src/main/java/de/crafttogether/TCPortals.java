package de.crafttogether;

import de.crafttogether.mysql.MySQLAdapter;
import de.crafttogether.mysql.MySQLConfig;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.listener.*;
import de.crafttogether.tcportals.localization.LocalizationManager;
import de.crafttogether.tcportals.portals.PortalHandler;
import de.crafttogether.tcportals.portals.PortalStorage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TCPortals extends JavaPlugin {
    public static TCPortals plugin;

    private String serverName;
    private MySQLAdapter mySQLAdapter;
    private LocalizationManager localizationManager;
    private PortalStorage portalStorage;
    private PortalHandler portalHandler;
    private BukkitAudiences adventure;
    private MiniMessage miniMessageParser;

    @Override
    public void onEnable() {
        plugin = this;
        adventure = BukkitAudiences.create(this);

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
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new ConnectionErrorListener(), this);

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
        localizationManager = new LocalizationManager();

        // Set Tags/Placeholder for MiniMessage
        miniMessageParser = MiniMessage.builder()
                .editTags(t -> t.resolver(TagResolver.resolver("prefix", Tag.selfClosingInserting(Localization.PREFIX.deserialize()))))
                .build();

        // Initialize Storage
        portalStorage = new PortalStorage();

        // Initialize PortalHandler
        portalHandler = new PortalHandler(getConfig().getString("Portals.Server.BindAddress"), getConfig().getInt("Portals.Server.Port"));
        portalHandler.registerActionSigns();

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

    public MiniMessage getMiniMessageParser() {
        return Objects.requireNonNullElseGet(miniMessageParser, MiniMessage::miniMessage);
    }

    public BukkitAudiences adventure() {
        if (adventure == null)
            throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
        return adventure;
    }
}
