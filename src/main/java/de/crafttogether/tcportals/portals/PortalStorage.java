package de.crafttogether.tcportals.portals;

import de.crafttogether.TCPortals;
import de.crafttogether.common.NetworkLocation;
import de.crafttogether.common.mysql.MySQLAdapter;
import de.crafttogether.common.mysql.MySQLConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

@SuppressWarnings("UnusedReturnValue")
public class PortalStorage {
    private final TCPortals plugin = TCPortals.plugin;

    private MySQLAdapter mySQLAdapter;
    private TreeMap<Integer, Portal> portals;

    public PortalStorage() {
        this.connect();
    }

    public void connect() {
        if (this.isActive())
            return;

        this.portals = new TreeMap<>();

        // Initialize MySQLAdapter
        this.mySQLAdapter = new MySQLAdapter(plugin,
                this.plugin.getConfig().getString("MySQL.Host"),
                this.plugin.getConfig().getInt("MySQL.Port"),
                this.plugin.getConfig().getString("MySQL.Username"),
                this.plugin.getConfig().getString("MySQL.Password"),
                this.plugin.getConfig().getString("MySQL.Database"),
                this.plugin.getConfig().getString("MySQL.TablePrefix"));

        // Create Tables if missing
        MySQLConnection connection = this.mySQLAdapter.getConnection();
        if (connection == null)
            return;

        // Create Tables if missing
        try (ResultSet result = connection.query("SHOW TABLES LIKE '%sportals';", connection.getTablePrefix())) {

            if (result != null && !result.next()) {
                this.plugin.getLogger().info("[MySQL]: Create Table '" + connection.getTablePrefix() + "portals' ...");

                connection.execute("""
                    CREATE TABLE `%sportals` (
                        `id` int(11) NOT NULL,
                        `name` varchar(16) NOT NULL,
                        `type` varchar(16) NOT NULL,
                        `host` varchar(128) DEFAULT NULL,
                        `port` int(11) DEFAULT NULL,
                        `server` varchar(128) DEFAULT NULL,
                        `world` varchar(128) DEFAULT NULL,
                        `x` double DEFAULT NULL,
                        `y` double DEFAULT NULL,
                        `z` double DEFAULT NULL
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
                """, connection.getTablePrefix());

                connection.execute("""
                    ALTER TABLE `%sportals`
                        ADD PRIMARY KEY (`id`),
                        ADD KEY `name` (`name`) USING BTREE;
                """, connection.getTablePrefix());

                connection.execute("""
                    ALTER TABLE `%sportals`
                      MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
                """, connection.getTablePrefix());
            }
        }
        catch (SQLException ex) {
            this.plugin.getLogger().warning("[MySQL]: " + ex.getMessage());
        }
        finally {
            connection.close();
        }

        // Load all portals from database into our cache
        Bukkit.getServer().getScheduler().runTask(this.plugin, () -> loadAll((err, portals) -> {
            if (err != null)
                return;

            this.plugin.getLogger().info("Loaded " + portals.size() + " Portals");

            // Remove not existing signs from database
            Bukkit.getServer().getScheduler().runTask(this.plugin, this::checkSigns);
        }));
    }

    public boolean isActive() {
        if (this.mySQLAdapter == null)
            return false;

        return this.mySQLAdapter.isActive();
    }

    public void disconnect() {
        if (this.mySQLAdapter == null)
            return;

        this.mySQLAdapter.disconnect();
        this.mySQLAdapter = null;
    }

    public List<Portal> get(String portalName) throws SQLException {
        List<Portal> found = new ArrayList<>();

        MySQLConnection connection = mySQLAdapter.getConnection();
        ResultSet result = connection.query("SELECT * FROM `%sportals` WHERE `name` = '%s'", connection.getTablePrefix(), portalName);

        while (result.next()) {
            Portal portal = setupPortal(result);

            if (portal != null) {
                found.add(portal);

                // Update cache
                this.portals.put(portal.getId(), portal);
            }
        }

        connection.close();
        return found;
    }

    public Portal create(String name, Portal.PortalType type, String host, int port, NetworkLocation location) throws SQLException {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        int portalId = connection.insert("INSERT INTO `%sportals` SET " +
                "`name` = '" + name + "', " +
                "`type` = '" + type + "', " +
                "`host` = '" + host + "', " +
                "`port` = " + port + ", " +
                "`server` = '" + location.getServer() + "', " +
                "`world` = '" + location.getWorld() + "', " +
                "`x` = " + location.getX() + ", " +
                "`y` = " + location.getY() + ", " +
                "`z` = " + location.getZ(), connection.getTablePrefix());
        connection.close();

        Portal portal = new Portal(name, type, portalId, host, port, location);

        // Update cache
        this.portals.put(portalId, portal);
        return portal;
    }

    public void update(Portal portal, MySQLConnection.Consumer<SQLException, Integer> consumer) {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        connection.updateAsync("UPDATE `%sportals` SET " +
                "`name`             = '" + portal.getName() + "', " +
                "`type`             = '" + portal.getType().name() + "', " +
                "`host`      = '" + portal.getTargetHost() + "', " +
                "`port`      =  " + portal.getTargetPort() + ", " +
                "`server`    = '" + portal.getTargetLocation().getServer() + "', " +
                "`world`     = '" + portal.getTargetLocation().getWorld() + "', " +
                "`x`         =  " + portal.getTargetLocation().getX() + ", " +
                "`y`         =  " + portal.getTargetLocation().getY() + ", " +
                "`z`         =  " + portal.getTargetLocation().getZ() +
                "WHERE `%sportals`.`id` = %s;",

        (err, affectedRows) -> {
            if (err != null) {
                this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }
            else {
                // Update cache
                this.portals.put(portal.getId(), portal);
                consumer.operation(null, 0);
            }

            connection.close();
        }, connection.getTablePrefix(), connection.getTablePrefix(), portal.getId());
    }

    public void delete(int portalId, MySQLConnection.Consumer<SQLException, Integer> consumer) {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        connection.updateAsync("DELETE FROM `%sportals` WHERE `id` = %s", (err, affectedRows) -> {
            if (err != null) {
                this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }
            else {
                // Update cache
                this.portals.remove(portalId);
                consumer.operation(null, affectedRows);
            }

            connection.close();
        }, connection.getTablePrefix(), portalId);
    }

    public void loadAll(MySQLConnection.Consumer<SQLException, Collection<Portal>> consumer) {
        MySQLConnection connection = this.mySQLAdapter.getConnection();

        this.portals = new TreeMap<>();

        connection.queryAsync("SELECT * FROM `%sportals`", (err, result) -> {
            if (err != null) {
                this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
                consumer.operation(err, null);
            }

            else {
                try {
                    while (result.next()) {
                        Portal portal = setupPortal(result);

                        // Update cache
                        if (portal != null)
                            this.portals.put(portal.getId(), portal);
                    }
                } catch (SQLException ex) {
                    err = ex;
                    this.plugin.getLogger().warning("[MySQL]: Error: " + ex.getMessage());
                }
                finally {
                    connection.close();
                }

                consumer.operation(err, this.portals.values());
            }
        }, connection.getTablePrefix());
    }

    private Portal setupPortal(ResultSet result) {
        Portal portal = null;

        try {
            NetworkLocation targetLocation = new NetworkLocation(
                    result.getString("server"),
                    result.getString("world"),
                    result.getDouble("x"),
                    result.getDouble("y"),
                    result.getDouble("z"));

            portal = new Portal(
                    result.getString("name"),
                    Portal.PortalType.valueOf(result.getString("type")),
                    result.getInt("id"),
                    result.getString("host"),
                    result.getInt("port"),
                    targetLocation);
        }
        catch (Exception err) {
            this.plugin.getLogger().warning("[MySQL]: Error: " + err.getMessage());
        }

        return portal;
    }

    public void checkSigns() {
        List<Portal> localPortals = this.portals.values().stream()
                .filter(portal -> portal.getTargetLocation().getServer().equals(this.plugin.getServerName()))
                .toList();

        for (Portal portal : localPortals) {
            if (portal.getSign() != null) continue;
            delete(portal.getId(), (err, rows) ->
                    this.plugin.getLogger().info("Deleted portal '" + portal.getName() + "' because action-sign doesn't exist anymore."));
        }
    }

    public Collection<Portal> getPortals() {
        return this.portals.values();
    }

    public Portal getPortal(String name) {
        for (Portal portal : portals.values()) {
            if (portal.getName().equalsIgnoreCase(name))
                return portal;
        }
        return null;
    }

    public Portal getPortal(int id) {
        for (Portal portal : this.portals.values())
            if (portal.getId().equals(id)) return portal;
        return null;
    }

    public Portal getPortal(Location location) {
        for (Portal portal : this.portals.values()) {
            if (!portal.getTargetLocation().getServer().equals(this.plugin.getServerName())) continue;
            if (portal.getTargetLocation().getBukkitLocation().equals(location)) return portal;
        }
        return null;
    }
}