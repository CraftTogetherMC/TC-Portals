package de.crafttogether.tcportals.portals;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;
import com.bergerkiller.bukkit.common.nbt.CommonTagCompound;
import com.bergerkiller.bukkit.common.utils.StringUtil;
import com.bergerkiller.bukkit.tc.TrainCarts;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.components.RailPiece;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.controller.status.TrainStatus;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberRideable;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.properties.standard.type.CollisionOptions;
import com.bergerkiller.bukkit.tc.properties.standard.type.TrainNameFormat;
import com.bergerkiller.bukkit.tc.rails.RailLookup;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.generated.net.minecraft.world.entity.EntityHandle;
import de.crafttogether.TCPortals;
import de.crafttogether.common.NetworkLocation;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.net.TCPClient;
import de.crafttogether.tcportals.net.TCPServer;
import de.crafttogether.tcportals.net.events.EntityReceivedEvent;
import de.crafttogether.tcportals.net.events.PacketReceivedEvent;
import de.crafttogether.tcportals.net.packets.EntityPacket;
import de.crafttogether.tcportals.net.packets.TrainPacket;
import de.crafttogether.tcportals.signactions.SignActionPortal;
import de.crafttogether.tcportals.signactions.SignActionPortalIn;
import de.crafttogether.tcportals.signactions.SignActionPortalOut;
import de.crafttogether.tcportals.util.PollingTask;
import de.crafttogether.tcportals.util.TCHelper;
import de.crafttogether.tcportals.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PortalHandler implements Listener {
    private static final TCPortals plugin = TCPortals.plugin;

    private static final SignActionPortal signActionPortal = new SignActionPortal();
    private static final SignActionPortalIn signActionPortalIn = new SignActionPortalIn();
    private static final SignActionPortalOut signActionPortalOut = new SignActionPortalOut();

    private final TCPServer tcpServer;
    private final ConcurrentHashMap<Location, PortalQueue> portalQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<MinecartGroup, PendingTeleport> pendingTeleports = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<MinecartGroup, ReceivedTrain> receivedTrains = new ConcurrentHashMap<>();

    public PortalHandler(String host, int port) {
        // Create Server Socket
        tcpServer = new TCPServer(host, port);

        // Register as EventHandler
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Register TrainCarts-ActionSigns
        registerActionSigns();
    }

    public void handleTrain(SignActionEvent event) {
        Util.debug("#handleTrain (" + event.getGroup().getProperties().getTrainName() + ") size: " + event.getGroup().size() + " passengers: " + TCHelper.getPassengers(event.getGroup()).size());

        MinecartGroup group = event.getGroup();
        String portalName = event.getLine(2);
        Portal.PortalType targetType = null;

        if (event.getLine(1).equals("portal"))
            targetType = Portal.PortalType.BIDIRECTIONAL;
        else if (event.getLine(1).equals("portal-in"))
            targetType = Portal.PortalType.OUT;

        // Get existing portal-out -signs from database
        List<Portal> portals;
        try {
            Portal.PortalType finalTargetType = targetType;
            portals = plugin.getPortalStorage().get(portalName).stream()
                    .filter(portal -> portal.getType().equals(finalTargetType))
                    .filter(portal -> !portal.getTargetLocation().getServer().equals(plugin.getServerName()))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            TCHelper.sendMessage(group, Localization.ERROR_DATABASE,
                    Placeholder.set("error", e.getMessage()));
            e.printStackTrace();
            return;
        }
        Portal portal = (portals.size() < 1) ? null : portals.get(0);

        // Abort if the triggered sign is the sign where the train was spawned
        if (portal != null && receivedTrains.containsKey(group) && receivedTrains.get(group).getPortal().getName().equals(portal.getName()))
            return;

        if (portal == null || portal.getTargetLocation() == null) {
            TCHelper.sendMessage(group, Localization.PORTAL_ENTER_NOEXIT,
                    Placeholder.set("name", portalName));
            return;
        }

        Util.debug("Train (" + group.getProperties().getTrainName() + ") goes from " + plugin.getServerName() + " (" + TCHelper.signToString(event.getLines()) + ") to " + portal.getTargetLocation().getServer() + ". Portal: " + portal.getName() + " Type: " + portal.getType().name() + ")");

        // Look if flags are used
        boolean clearItems = false;

        if (event.getLine(3).contains("!mobs"))
            TCHelper.killNonPlayerPassengers(group);
        if (event.getLine(3).contains("-mobs"))
            TCHelper.ejectNonPlayerPassengers(group);
        if (event.getLine(3).contains("!items")) {
            TCHelper.clearInventory(group);
            clearItems = true;
        }
        if (event.getLine(3).contains("-items"))  {
            TCHelper.dropInventory(group);
            clearItems = true;
        }

        // Disable item-drops
        group.getProperties().setSpawnItemDrops(false);

        // Try to transfer train to the target server
        boolean success = transferTrain(event, portal, clearItems);

        // Cache teleportation-infos
        Util.debug("clean pendingTeleports");
        cleanCache(pendingTeleports);
        pendingTeleports.put(group, new PendingTeleport(portal, !success));

        if (!success)
            return;

        // Apply blindness-effect
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 40, 1);
        List<Player> playerPassengers = TCHelper.getPlayerPassengers(group);
        for (Player passenger : playerPassengers)
            passenger.addPotionEffect(blindness);

        group.getProperties().setCollision(CollisionOptions.CANCEL);

        if (event.isCartSign())
            handleCart(event);
    }

    public void handleCart(SignActionEvent event) {
        Util.debug("#handleCart group-size: " + event.getGroup().size());

        MinecartGroup group = event.getGroup();
        PendingTeleport pendingTeleport = pendingTeleports.get(group);

        // Abort if no pendingTeleport was created or some error occured
        if (pendingTeleport == null || pendingTeleport.hasError())
            return;

        // Transfer passengers to target server
        MinecartMember<?> member = event.getMember();

        for (Entity passenger : TCHelper.getPassengers(member)) {
            if (passenger instanceof Player)
                sendPlayerToServer((Player) passenger, pendingTeleport.portal());

            else if (passenger instanceof LivingEntity)
                passenger.remove();
        }

        // Remove from cache
        if ((event.isTrainSign() && group.size() <= 1) || event.isCartSign()) {
            Util.debug("isTrainSign: " + event.isTrainSign() + " isCartSign: " + event.isCartSign());
            Util.debug("remove pending teleport");
            pendingTeleports.remove(group);
        }

        // Destroy cart per cart
        if (group.size() <= 1) {
            group.destroy();
            group.remove();
        } else {
            Entity cartEntity = member.getEntity().getEntity();
            group.removeSilent(member);
            cartEntity.remove();
        }
    }

    public boolean transferTrain(SignActionEvent event, Portal portal, boolean clearItems) {
        Util.debug("#transferTrain");

        UUID trainId = UUID.randomUUID();
        MinecartGroup group = event.getGroup();

        // Save train and get properties
        ConfigurationNode trainConfig = group.saveConfig().clone();
        trainConfig.set("name", TrainNameFormat.guess(group.getProperties().getTrainName()).toString());

        Object[] cartList = trainConfig.getNode("carts").getNodes().toArray();
        for (int i = 0; i < cartList.length; i++) {
            ConfigurationNode node = (ConfigurationNode) cartList[i];

            // If cart sign, remove all carts except the first one
            if (i > 0 && event.isCartSign()) {
                Util.debug("stripping cart from " + group.getProperties().getTrainName() + " which had " + group.size() + " minecarts");
                trainConfig.getNode("carts").remove(String.valueOf(i));
            }

            else {
                // Remove lastPathNode from cartProperties
                if (node.contains("lastPathNode"))
                    node.remove("lastPathNode");

                // clear items if necessary
                if (clearItems && node.contains("data.contents"))
                    node.remove("data.contents");
            }
        }

        // Get passengers and seat numbers
        List<Passenger> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group) {
            for (Entity entity : TCHelper.getPassengers(member)) {
                Passenger passenger = new Passenger(trainId, entity.getUniqueId(), entity.getType(), member.getIndex());
                passengers.add(passenger);

                // Register player to have a reference for sending error-messages in chat
                if (entity instanceof Player player) {
                    passenger.setTrainName(group.getProperties().getTrainName());
                    Passenger.register(passenger);
                    Util.debug("Register " + player.getName() + " trainId: " + trainId + " name: " + group.getProperties().getTrainName());
                }
            }
        }

        TrainPacket packet = new TrainPacket(trainId);
        packet.name = group.getProperties().getTrainName();
        packet.speed = group.head().getRealSpeedLimited();
        packet.waitDistance = group.getProperties().getWaitDistance();
        packet.owners = group.getProperties().getOwners();
        packet.config = trainConfig.toString();
        packet.passengers = passengers;
        packet.portal = portal.getName();
        packet.target = portal.getTargetLocation();
        packet.source = plugin.getServerName();

        // Transfer train
        TCPClient client = new TCPClient(portal.getTargetHost(), portal.getTargetPort(), trainId);
        client.sendAuth(plugin.getConfig().getString("Portals.Server.SecretKey"));
        boolean success = client.send(packet);

        if (!success)
            plugin.getLogger().warning("Unable to send train " + packet.name + " to " + packet.target.getServer());

        else {
            Util.debug("Train (" + packet.name + ") was sent to " + packet.target.getServer() + " with " + group.size() + " carts");

            // Send entities to target server
            for (Entity entity : TCHelper.getPassengers(group)) {
                if (entity instanceof LivingEntity && !(entity instanceof Player))
                    sendEntityToServer((LivingEntity) entity, portal);
            }
        }

        return success;
    }

    @EventHandler
    public void receivePacket(PacketReceivedEvent event) {
        if (event.getPacket() instanceof TrainPacket)
            receiveTrain((TrainPacket) event.getPacket());
    }

    public void receiveTrain(TrainPacket packet) {
        if (packet == null)
            return;

        // Register passengers
        for (Passenger passenger : packet.passengers)
            Passenger.register(passenger);

        // Load train from received config
        ConfigurationNode trainConfig = new ConfigurationNode();
        trainConfig.loadFromString(packet.config);
        SpawnableGroup spawnable = SpawnableGroup.fromConfig(TrainCarts.plugin, trainConfig);

        int carts = trainConfig.getNode("carts").getNodes().size();
        Util.debug("Received train (" + packet.name + ") with " + carts + " cart(s) from " + packet.source, false);

        // Check if world exists
        World targetWorld = Bukkit.getWorld(packet.target.getWorld());
        if (targetWorld == null) {
            Util.debug("World '" + packet.target.getWorld() + "' was not found!");
            Passenger.error(packet.id, Localization.PORTAL_EXIT_WORLDNOTFOUND.deserialize(
                    Placeholder.set("world", packet.target.getWorld())));
            return;
        }

        // Portal not found
        NetworkLocation targetLocation = packet.target;
        Portal portal = plugin.getPortalStorage().getPortal(targetLocation.getBukkitLocation());

        if (portal == null || portal.getSign() == null) {
            Util.debug("Could not find a Portal at " + targetLocation.getWorld() + ", " + targetLocation.getX() + ", " + targetLocation.getY() + ", " + targetLocation.getZ());
            Passenger.error(packet.id, Localization.PORTAL_EXIT_SIGNNOTFOUND.deserialize(
                    Placeholder.set("name", packet.portal),
                    Placeholder.set("world", packet.target.getWorld()),
                    Placeholder.set("x", packet.target.getX()),
                    Placeholder.set("y", packet.target.getY()),
                    Placeholder.set("z", packet.target.getZ())));
            return;
        }

        // No Rail!
        RailPiece rail = RailLookup.discoverRailPieceFromSign(portal.getSign().getBlock());
        if (rail == null || rail.block() == null) {
            Util.debug("Could not find a Rail at " + targetLocation.getWorld() + ", " + targetLocation.getX() + ", " + targetLocation.getY() + ", " + targetLocation.getZ());
            Passenger.error(packet.id, Localization.PORTAL_EXIT_NORAILS.deserialize(
                    Placeholder.set("world", targetLocation.getWorld()),
                    Placeholder.set("x", targetLocation.getX()),
                    Placeholder.set("y", targetLocation.getY()),
                    Placeholder.set("z", targetLocation.getZ())));
            return;
        }

        // Couldn't find spawn-location
        SpawnableGroup.SpawnLocationList spawnLocations = TCHelper.getSpawnLocations(spawnable, rail, portal.getSign());
        if (spawnLocations == null) {
            Util.debug("Could not find the right spot to spawn a train at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ());
            Passenger.error(packet.id, Localization.PORTAL_EXIT_NOSPAWNLOCATION.deserialize(
                    Placeholder.set("world", targetLocation.getWorld()),
                    Placeholder.set("x", targetLocation.getX()),
                    Placeholder.set("y", targetLocation.getY()),
                    Placeholder.set("z", targetLocation.getZ())));
            return;
        }

        // Check that the area isn't occupied by another train
        if (spawnLocations.isOccupied()) {
            MinecartMember<?> occupyingMember = TCHelper.getOccupyingMember(spawnLocations);

            if (occupyingMember != null) {
                if (receivedTrains.containsKey(occupyingMember.getGroup()))
                    Util.debug("Track is occupied by a previosly spawned train. Ignore it. isMoving: " + (occupyingMember.getRealSpeed() > 0));

                else {
                    Util.debug("Track is occupied by another train at " + rail.world() + ", " + rail.block().getX() + ", " + rail.block().getY() + ", " + rail.block().getZ());
                    Passenger.error(packet.id, Localization.PORTAL_EXIT_TRACKOCCUPIED.deserialize(
                            Placeholder.set("world", targetLocation.getWorld()),
                            Placeholder.set("x", targetLocation.getX()),
                            Placeholder.set("y", targetLocation.getY()),
                            Placeholder.set("z", targetLocation.getZ())));
                    return;
                }
            }
        }

        // Add to queue
        Location portalLocation = portal.getTargetLocation().getBukkitLocation();
        PortalQueue queue = portalQueues.get(portalLocation);

        if (queue == null) {
            queue =  new PortalQueue(portal, this::handleReceivedTrain);
            portalQueues.put(portalLocation, queue);
        }

        queue.addTrain(portal, packet, spawnable, spawnLocations);
        Util.debug("Added received train (" + packet.name + ") to queue. Size: " + spawnable.getMembers().size());
    }

    public boolean handleReceivedTrain(PortalQueue.QueuedTrain queuedTrain) {
        if (queuedTrain == null)
            return false;

        Portal portal = queuedTrain.getPortal();
        TrainPacket packet = queuedTrain.getTrainPacket();
        SpawnableGroup.SpawnLocationList spawnLocations = queuedTrain.getSpawnLocations();

        if (!queuedTrain.isSpawned()) {
            // Load the chunks first
            Util.loadChunks(spawnLocations, 2, 5);

            // Spawn
            MinecartGroup group = queuedTrain.getSpawnableGroup().spawn(spawnLocations);
            queuedTrain.setSpawnedGroup(group);

            // Tell passengers
            Passenger.setTrainName(packet.id, group.getProperties().getTrainName());

            // Avoid conflicts with other recently spawned trains, restore will be restored later
            if (group.hasFuel())
                queuedTrain.setFuelMap(TCHelper.getFuelMap(group, true));

            queuedTrain.setCollisionOptions(group.getProperties().getCollision());
            queuedTrain.setWaitDistance(packet.waitDistance);

            group.getProperties().setCollision(CollisionOptions.CANCEL);
            group.getProperties().setWaitDistance(0);

            // Remove wait-actions
            for (TrainStatus status : group.getStatusInfo()) {
                if (status instanceof TrainStatus.WaitingForTrain) {
                    for (MinecartMember<?> member : group)
                        member.getActions().getStatusInfo().remove(status);
                }
            }

            // Look if flags are used
            Sign sign = portal.getSign();
            if (sign.getLine(3).contains("!mobs"))
                TCHelper.killNonPlayerPassengers(group);
            if (sign.getLine(3).contains("-mobs"))
                TCHelper.ejectNonPlayerPassengers(group);
            if (sign.getLine(3).contains("!items"))
                TCHelper.clearInventory(group);
            if (sign.getLine(3).contains("-items"))
                TCHelper.dropInventory(group);

            Util.debug("Train spawned! (" + TCHelper.groupToString(group) + ") Name: " + group.getProperties().getTrainName() + " OldName: " + packet.name + " Size: " + group.size());

            // Cache received trains
            cleanCache(receivedTrains);
            receivedTrains.put(group, new ReceivedTrain(portal));

            // Route fix
            boolean routeFixingDisabled = plugin.getConfig().getBoolean("Settings.DisableRouteFixing");
            if (!routeFixingDisabled && group.getProperties().getDestination().equals(TCPortals.plugin.getServerName()) && group.getProperties().getNextDestinationOnRoute() != null)
                group.getProperties().setDestination(group.getProperties().getNextDestinationOnRoute());
        }

        else {
            MinecartGroup group = queuedTrain.getSpawnedGroup();

            // Check that the area isn't occupied by any other train or wait for the next tick
            MinecartMember<?> occupyingMember = TCHelper.getOccupyingMember(spawnLocations);
            if (occupyingMember != null && !group.contains(occupyingMember))
                return false;

            // Launch
            long launchDelayTicks = plugin.getConfig().getLong("Portals.LaunchDelayTicks");
            double launchDistance = plugin.getConfig().getDouble("Portals.LaunchDistanceBlocks");

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Vector headDirection = spawnLocations.locations.get(spawnLocations.locations.size() - 1).forward;
                BlockFace launchDirection = com.bergerkiller.bukkit.tc.Util.vecToFace(headDirection, false);
                group.head().getActions().addActionLaunch(launchDirection, launchDistance, packet.speed);

                if (queuedTrain.hasFuel())
                    TCHelper.applyFuelMap(queuedTrain.getFuelMap());

                Util.debug("Train (" + group.getProperties().getTrainName() + ") launched! Direction: " + launchDirection.name() + " delay: " + launchDelayTicks + " distance: " + launchDistance + " speed: " + packet.speed);

                // Restore altered properties and remove train vom receivedTrains-cache
                new PollingTask(() -> {
                    if (group.isRemoved() || group.isUnloaded() || !receivedTrains.containsKey(group))
                        return true;

                    if (receivedTrains.get(group).getTravelledBlocks() > spawnLocations.locations.size() && TCHelper.isTrackClear(group, 1)) {
                        group.getProperties().setCollision(queuedTrain.getCollisionOptions());
                        group.getProperties().setWaitDistance(queuedTrain.getWaitDistance());
                        Util.debug("Properties restored after " + receivedTrains.get(group).getTravelledBlocks() + " blocks for " + group.getProperties().getTrainName());
                        Util.debug("WaitDistance: " + group.getProperties().getWaitDistance());
                        Util.debug("CollisionOptions: trains->" + group.getProperties().getCollision().trainMode().name());
                        Util.debug("Removed receivedTrain");
                        getReceivedTrains().remove(group);
                        return true;
                    }

                    return false;
                }, 0L, 1L);
            }, launchDelayTicks);

            Util.debug("Train (" + group.getProperties().getTrainName() + ") came from " + packet.source + ". Portal: " + packet.portal + "/" + portal.getName() + " Type: " + portal.getType().name() + " to " + plugin.getServerName() + " (" + TCHelper.signToString(portal.getSign().getLines()) + ")");

            // Remove from queue
            return true;
        }

        return false;
    }

    public void sendEntityToServer(LivingEntity entity, Portal portal) {
        if (entity.getHealth() < 1)
            return;

        EntityHandle entityHandle = EntityHandle.fromBukkit(entity);
        CommonTagCompound tagCompound = new CommonTagCompound();
        entityHandle.saveToNBT(tagCompound);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            TCPClient client = new TCPClient(portal.getTargetHost(), portal.getTargetPort(), null);
            client.sendAuth(plugin.getConfig().getString("Portals.Server.SecretKey"));
            boolean success = client.send(new EntityPacket(entity.getUniqueId(), entity.getType()));

            if (!success)
                plugin.getLogger().warning("Unable to send entity (" + entity.getType() + ") to " + portal.getTargetLocation().getServer());

            else {
                try {
                    tagCompound.writeToStream(client.getOutputStream(), false);
                    Util.debug("Entity (" + entity.getType() + ") was sent to " + portal.getTargetLocation().getServer());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @EventHandler
    public void receiveEntity(EntityReceivedEvent event) {
        Passenger passenger = Passenger.get(event.getUuid());
        if (passenger == null)
            return;

        Util.debug("Received entity (" + event.getType() + ") from " + event.getSource());

        MinecartGroup group = TCHelper.getTrain(passenger.getTrainName());
        if (group == null || group.get(passenger.getCartIndex()) == null) {
            Util.debug("Unable to get spawnLocation for entity (" + event.getType().name() + ")");
            return;
        }

        // Spawn Entity
        Location location = group.get(passenger.getCartIndex()).getEntity().getLocation();
        World world = location.getWorld();
        Class<? extends Entity> entityClass = event.getType().getEntityClass();

        if (entityClass == null || world == null) {
            Util.debug("Failed to spawn entity (" + event.getType().name() + ")");
            return;
        }

        world.spawn(location, entityClass, spawnedEntity -> {
            spawnedEntity.setInvulnerable(true);

            // Load received NBT to spawned Entity
            EntityHandle entityHandle = EntityHandle.fromBukkit(spawnedEntity);
            entityHandle.loadFromNBT(event.getTagCompound());
        });
    }

    public void sendPlayerToServer(Player player, Portal portal) {
        String portalType = portal.getType().equals(Portal.PortalType.BIDIRECTIONAL) ? "bidirectional" : "directional";
        if (!Util.checkPermission(player, "tcportals.portal.use." + portalType))
            return;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
            out.writeUTF("Connect");
            out.writeUTF(portal.getTargetLocation().getServer());
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendPluginMessage(plugin, "BungeeCord", bytes.toByteArray());
    }

    // Handle spawned entity if it was a passenger
    public static void reEnterEntity(Passenger passenger, CreatureSpawnEvent event) {
        event.setCancelled(false);

        if (passenger.hasError()) {
            Passenger.remove(passenger.getUUID());
            return;
        }

        LivingEntity entity = event.getEntity();
        MinecartGroup group = TCHelper.getTrain(passenger.getTrainName());

        if (group == null) {
            Util.debug("Could not find train (" + passenger.getTrainName() + ") for entity " + entity.getType());
            Passenger.remove(passenger.getUUID());
            return;
        }

        MinecartMember<?> member = group.get(passenger.getCartIndex());

        if (member instanceof MinecartMemberRideable) {
            entity.teleport(member.getEntity().getLocation());
            member.getEntity().getEntity().addPassenger(entity);
            entity.setInvulnerable(false);

            Passenger.remove(passenger.getUUID());
            Util.debug("Passenger (" + entity.getType() + ") " + entity.getUniqueId() + " sucessfully reEntered");
        }
        else
            Util.debug("Unable to put entity " + entity.getType() + " back on train (" + group.getProperties().getTrainName() + ") the cart (" + passenger.getCartIndex() + ") is not rideable");

        Passenger.remove(passenger.getUUID());
    }

    // Handle joined player if he was a passenger
    public static void reEnterPlayer(Passenger passenger, PlayerSpawnLocationEvent event) {
        // Check if some error occurred
        if (passenger.hasError()) {
            // TODO: Could be async
            Bukkit.getScheduler().runTaskLater(TCPortals.plugin, () -> {
                PluginUtil.adventure().player(event.getPlayer()).sendMessage(passenger.getError());
                Passenger.remove(passenger.getUUID());
            }, 20L);

            return;
        }

        Player player = event.getPlayer();
        MinecartGroup group = TCHelper.getTrain(passenger.getTrainName());

        if (group == null) {
            Util.debug("Could not find train (" + passenger.getTrainName() + ") for player " + player.getName());
            Localization.PORTAL_EXIT_NOTRAIN.message(player,
                    Placeholder.set("train", passenger.getTrainName()));
            return;
        }

        MinecartMember<?> member = group.get(passenger.getCartIndex());

        if (member instanceof MinecartMemberRideable) {
            event.setSpawnLocation(member.getEntity().getLocation());

            if (player.isFlying())
                player.setFlying(false);

            member.getEntity().getEntity().addPassenger(player);
            Passenger.remove(passenger.getUUID());
        }
        else
            Util.debug("Unable to put player " + player.getName() + " back on train (" + group.getProperties().getTrainName() + ") the cart (" + passenger.getCartIndex() + ") is not rideable");

        Passenger.remove(passenger.getUUID());
        Util.debug("Passenger (" + player.getName() + ") sucessfully reEntered");
    }

    public void cleanCache(ConcurrentHashMap<MinecartGroup, ?> map) {
        for (MinecartGroup group : map.keySet()) {
            if (group.isUnloaded() || group.isRemoved() || group.size() < 1) {
                Util.debug("removed " + group.getProperties().getTrainName() + " from cache");
                map.remove(group);
            }
        }
    }

    public void registerActionSigns() {
        SignAction.register(signActionPortal);
        SignAction.register(signActionPortalIn);
        SignAction.register(signActionPortalOut);
    }

    public void unregisterActionSigns() {
        SignAction.unregister(signActionPortal);
        SignAction.unregister(signActionPortalIn);
        SignAction.unregister(signActionPortalOut);
    }

    public void shutdown() {
        unregisterActionSigns();

        // Cancel all running tasks
        for (PortalQueue queue : portalQueues.values())
            queue.cancel();

        // Close server
        if (tcpServer != null)
            tcpServer.close();

        // Close all active clients
        TCPClient.closeAll();
    }

    public Map<MinecartGroup, PendingTeleport> getPendingTeleports() {
        return pendingTeleports;
    }

    public ConcurrentHashMap<MinecartGroup, ReceivedTrain> getReceivedTrains() {
        return receivedTrains;
    }

    public ConcurrentHashMap<Location, PortalQueue> getPortalQueues() {
        return portalQueues;
    }
}
