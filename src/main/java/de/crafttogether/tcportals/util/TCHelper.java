package de.crafttogether.tcportals.util;

import com.bergerkiller.bukkit.common.entity.type.CommonMinecartFurnace;
import com.bergerkiller.bukkit.common.resources.ParticleType;
import com.bergerkiller.bukkit.common.resources.SoundEffect;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.common.utils.WorldUtil;
import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import com.bergerkiller.bukkit.tc.controller.components.RailPiece;
import com.bergerkiller.bukkit.tc.controller.components.RailState;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableGroup;
import com.bergerkiller.bukkit.tc.controller.spawnable.SpawnableMember;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberChest;
import com.bergerkiller.bukkit.tc.controller.type.MinecartMemberFurnace;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.properties.TrainProperties;
import com.bergerkiller.bukkit.tc.properties.TrainPropertiesStore;
import com.bergerkiller.bukkit.tc.utils.TrackMovingPoint;
import de.crafttogether.common.dep.net.kyori.adventure.text.Component;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.common.util.PluginUtil;
import de.crafttogether.tcportals.Localization;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCHelper {
    public static void displayError(SignActionEvent info) {
        BlockFace facingInv = info.getFacing().getOppositeFace();
        Location effectLocation = info.getSign().getLocation()
                .add(0.5, 0.5, 0.5)
                .add(0.3 * facingInv.getModX(), 0.0, 0.3 * facingInv.getModZ());

        com.bergerkiller.bukkit.tc.Util.spawnDustParticle(effectLocation, 255.0, 255.0, 0.0);
        WorldUtil.playSound(effectLocation, SoundEffect.EXTINGUISH, 1.0f, 2.0f);
    }

    public static MinecartGroup getTrain(Player p) {
        Entity entity = p.getVehicle();
        MinecartMember<?> member = null;

        if (entity == null)
            return null;

        if (entity instanceof Minecart)
            member = MinecartMemberStore.getFromEntity(entity);

        if (member != null)
            return member.getGroup();

        return null;
    }

    public static MinecartGroup getTrain(String trainName) {
        TrainProperties properties = TrainPropertiesStore.get(trainName);
        return (properties != null && properties.hasHolder()) ? properties.getHolder() : null;
    }

    public static Map<CommonMinecartFurnace, Integer> getFuelMap(MinecartGroup group, boolean clear) {
        Map<CommonMinecartFurnace, Integer> fuelMap = new HashMap<>();

        for (MinecartMember<?> member : group) {
            if (member instanceof MinecartMemberFurnace) {
                CommonMinecartFurnace minecartFurnace = (CommonMinecartFurnace) member.getEntity();

                if (minecartFurnace.hasFuel()) {
                    fuelMap.put(minecartFurnace, minecartFurnace.getFuelTicks());
                    if (clear) minecartFurnace.setFuelTicks(0);
                }
            }
        }

        return fuelMap;
    }

    public static void applyFuelMap(Map<CommonMinecartFurnace, Integer> fuelMap) {
        for (CommonMinecartFurnace minecartFurnace : fuelMap.keySet())
            minecartFurnace.setFuelTicks(fuelMap.get(minecartFurnace));
    }

    public static List<Player> getPlayerPassengers(MinecartMember<?> member) {
        List<Player> passengers = new ArrayList<>();
        for (Entity passenger : member.getEntity().getEntity().getPassengers())
            if (passenger instanceof Player) passengers.add((Player) passenger);

        return passengers;
    }

    public static List<Player> getPlayerPassengers(MinecartGroup group) {
        List<Player> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group)
            passengers.addAll(getPlayerPassengers(member));

        return passengers;
    }

    public static List<Entity> getPassengers(MinecartMember<?> member) {
        return member.getEntity().getEntity().getPassengers();
    }

    public static List<Entity> getPassengers(MinecartGroup group) {
        List<Entity> passengers = new ArrayList<>();
        for (MinecartMember<?> member : group)
            passengers.addAll(getPassengers(member));

        return passengers;
    }

    public static void killNonPlayerPassengers(MinecartGroup group) {
        List<Entity> passengers = getPassengers(group);

        for (Entity entity : passengers) {
            if (entity instanceof Player || !(entity instanceof LivingEntity)) continue;
            ((LivingEntity) entity).setHealth(0);
        }
    }

    public static void ejectNonPlayerPassengers(MinecartGroup group) {
        for (MinecartMember<?> member : group) {
            if (!member.getEntity().hasPassenger()) continue;

            for (Entity entity : member.getEntity().getEntity().getPassengers()) {
                if (entity instanceof Player || !(entity instanceof LivingEntity)) continue;
                member.getEntity().getEntity().removePassenger(entity);
            }
        }
    }

    public static void clearInventory(MinecartMember<?> member) {
        if (member instanceof MinecartMemberChest chestCart)
            chestCart.getEntity().getInventory().clear();

        for (Entity entity : member.getEntity().getEntity().getPassengers()) {
            if (!(entity instanceof ChestedHorse)) continue;
            ((ChestedHorse) entity).getInventory().clear();
        }
    }

    public static void clearInventory(MinecartGroup group) {
        for (MinecartMember<?> member : group)
            clearInventory(member);
    }

    public static void dropInventory(MinecartMember<?> member) {
        if (member instanceof MinecartMemberChest memberChest)
            memberChest.getEntity().getInventory().clear();

        dropChestedHorseInventory(member);
    }

    public static void dropInventory(MinecartGroup group) {
        for (MinecartMember<?> member : group)
            dropInventory(member);
    }

    public static void dropChestedHorseInventory(MinecartMember<?> member) {
        for (Entity entity : member.getEntity().getEntity().getPassengers()) {
            if (!(entity instanceof ChestedHorse)) continue;
            Inventory inventory = ((ChestedHorse) entity).getInventory();
            Location location = member.getEntity().getLocation();

            for (ItemStack item : inventory.getContents()) {
                if (item != null && location.getWorld() != null)
                    location.getWorld().dropItem(location, item.clone()).setPickupDelay(20);
            }

            inventory.clear();
        }
    }

    public static void dropChestedHorseInventory(MinecartGroup group) {
        for (MinecartMember<?> member : group)
            dropChestedHorseInventory(member);
    }

    public static void sendMessage(MinecartMember<?> member, Localization localization, Placeholder... arguments) {
        for (Player passenger : getPlayerPassengers(member))
            localization.message(passenger, arguments);
    }

    public static void sendMessage(MinecartGroup group, Localization localization, Placeholder... arguments) {
        for (Player passenger : getPlayerPassengers(group))
            localization.message(passenger, arguments);
    }

    public static void sendMessage(MinecartMember<?> member, Component message) {
        for (Player passenger : getPlayerPassengers(member))
            PluginUtil.adventure().player(passenger).sendMessage(message);
    }

    public static void sendMessage(MinecartGroup group, Component message) {
        for (Player passenger : getPlayerPassengers(group))
            PluginUtil.adventure().player(passenger).sendMessage(message);
    }

    public static MinecartMember<?> getOccupyingMember(SpawnableGroup.SpawnLocationList spawnLocations) {
        for (SpawnableMember.SpawnLocation loc : spawnLocations.locations) {
            MinecartMember<?> member = MinecartMemberStore.getAt(loc.location);
            if (member != null && !member.isUnloaded() && !member.getEntity().isRemoved())
                return member;
        }
        return null;
    }

    public static boolean isTrackClear(MinecartGroup group, int lookAhead) {
        if (group == null || group.size() < 1)
            return true;

        List<MinecartMember<?>> collidingMembers = new ArrayList<>();
        List<RailPiece> rails = new ArrayList<>();

        for (MinecartMember<?> member : group) {
            RailPiece lastRail = RailPiece.create(member.getRailTracker().getLastBlock());
            if (!rails.contains(lastRail))
                rails.add(lastRail);

            RailPiece rail = RailPiece.create(member.getRailTracker().getLastBlock());
            if (!rails.contains(rail))
                rails.add(rail);
        }

        if (lookAhead > 0) {
            Vector motionVector = group.head().getRailTracker().getState().motionVector();
            Location startLocation = group.head().getRailTracker().getState().positionLocation();
            TrackMovingPoint walker = new TrackMovingPoint(startLocation, motionVector);

            int walked_distance = 0;
            while (walker.hasNext() && walked_distance - 1 < lookAhead) {
                if (!rails.contains(walker.currentRailPiece))
                    rails.add(walker.currentRailPiece);
                walker.next();
                walked_distance++;
            }
        }

        for (RailPiece rail : rails) {
            for (Player player : Bukkit.getOnlinePlayers())
                player.spawnParticle(Particle.BLOCK_MARKER, rail.block().getLocation().add(0, 1, 0), 1);

            for (MinecartMember<?> collidingMember : rail.members()) {
                if (!group.contains(collidingMember) && !collidingMembers.contains(collidingMember))
                    collidingMembers.add(collidingMember);
            }
        }

        return collidingMembers.size() < 1;
    }

    public static String groupToString(MinecartGroup group) {
        StringBuilder groupString = new StringBuilder();

        for (MinecartMember<?> member : group)
            groupString.append(memberToString(member));

        return groupString.toString();
    }

    public static String memberToString(MinecartMember<?> member) {
        return  switch (member.getEntity().getType()) {
            default -> "?";
            case MINECART -> "m";
            case MINECART_CHEST -> "s";
            case MINECART_FURNACE -> "p";
            case MINECART_TNT -> "t";
            case MINECART_HOPPER -> "h";
        };
    }

    public static String signToString(String[] lines) {
        return lines[0] + " | " + lines[1] + " | " + lines[2] + " | " + lines[3];
    }

    public static SpawnableGroup.SpawnLocationList getSpawnLocations(SpawnableGroup spawnable, RailPiece rail, Sign sign) {
        SignActionEvent info = new SignActionEvent(sign.getBlock());

        /*
          Copyright (C) 2013-2022 bergerkiller
        */

        if (spawnable.getMembers().isEmpty())
            return null;

        // Find the movement direction vector on the rails
        // This, and the inverted vector, are the two directions in which can be spawned
        Vector railDirection;
        {
            RailState state = RailState.getSpawnState(rail);
            railDirection = state.motionVector();
        }

        // Figure out a preferred direction to spawn into, and whether to allow centering or not
        // This is defined by:
        // - Watched directions ([train:right]), which disables centering
        // - Which block face of the sign is powered, which disables centering
        // - Facing of the sign if no direction is set, which enables centering
        boolean isBothDirections;
        boolean useCentering;

        Vector spawnDirection;
        {
            boolean spawnA = info.isWatchedDirection(railDirection.clone().multiply(-1.0));
            boolean spawnB = info.isWatchedDirection(railDirection);

            isBothDirections = (spawnA && spawnB);
            if (isBothDirections) {
                // Decide using redstone power if both directions are watched
                BlockFace face = com.bergerkiller.bukkit.tc.Util.vecToFace(railDirection, false);
                spawnA = info.isPowered(face);
                spawnB = info.isPowered(face.getOppositeFace());
            }

            if (spawnA && !spawnB) {
                // Definitively into spawn direction A
                spawnDirection = railDirection;
                useCentering = false;
            }

            else if (!spawnA && spawnB) {
                // Definitively into spawn direction B
                spawnDirection = railDirection.clone().multiply(-1.0);
                useCentering = false;
            }

            else {
                // No particular direction is decided
                // Center the train and spawn relative right of the sign
                if (FaceUtil.isVertical(com.bergerkiller.bukkit.tc.Util.vecToFace(railDirection, false))) {
                    // Vertical rails, launch downwards
                    if (railDirection.getY() < 0.0)
                        spawnDirection = railDirection;
                    else
                        spawnDirection = railDirection.clone().multiply(-1.0);
                }

                else {
                    // Horizontal rails, launch most relative right of the sign facing
                    Vector facingDir = FaceUtil.faceToVector(FaceUtil.rotate(info.getFacing(), -2));
                    if (railDirection.dot(facingDir) >= 0.0)
                        spawnDirection = railDirection;
                    else
                        spawnDirection = railDirection.clone().multiply(-1.0);
                }
                useCentering = true;
            }
        }

        // If a center mode is defined in the declared spawned train, then adjust the
        // centering rule accordingly.
        if (spawnable.getCenterMode() == SpawnableGroup.CenterMode.MIDDLE)
            useCentering = false; // No centering

        else if (spawnable.getCenterMode() == SpawnableGroup.CenterMode.LEFT || spawnable.getCenterMode() == SpawnableGroup.CenterMode.RIGHT)
            useCentering = false;

        // If CenterMode is LEFT, then we use the REVERSE spawn mode instead of DEFAULT
        // This places the head close to the sign, rather than the tail
        SpawnableGroup.SpawnMode directionalSpawnMode = SpawnableGroup.SpawnMode.DEFAULT;
        /*
        if (spawnable.getCenterMode() == SpawnableGroup.CenterMode.LEFT) {
            directionalSpawnMode = SpawnableGroup.SpawnMode.REVERSE;
        }*/

        // Attempt spawning the train in priority of operations
        SpawnableGroup.SpawnLocationList spawnLocations = null;
        if (useCentering) {
            // First try spawning it centered, facing in the suggested spawn direction
            spawnLocations = spawnable.findSpawnLocations(info.getRailPiece(), spawnDirection, SpawnableGroup.SpawnMode.CENTER);

            // If this hits a dead-end, in particular with single-cart spawns, try the opposite direction
            if (spawnLocations != null && !spawnLocations.can_move) {
                Vector opposite = spawnDirection.clone().multiply(-1.0);
                SpawnableGroup.SpawnLocationList spawnOpposite = spawnable.findSpawnLocations(
                        info.getRailPiece(), opposite, SpawnableGroup.SpawnMode.CENTER);

                if (spawnOpposite != null && spawnOpposite.can_move) {
                    spawnDirection = opposite;
                    spawnLocations = spawnOpposite;
                }
            }
        }

        // First try the suggested direction
        if (spawnLocations == null)
            spawnLocations = spawnable.findSpawnLocations(info.getRailPiece(), spawnDirection, directionalSpawnMode);

        // Try opposite direction if not possible
        // Is movement into this direction is not possible, and both directions
        // can be spawned (watched directions), also try other direction.
        // If that direction can be moved into, then use that one instead.
        if (spawnLocations == null || (!spawnLocations.can_move && isBothDirections)) {
            Vector opposite = spawnDirection.clone().multiply(-1.0);
            SpawnableGroup.SpawnLocationList spawnOpposite = spawnable.findSpawnLocations(
                    info.getRailPiece(), opposite, directionalSpawnMode);

            if (spawnOpposite != null && (spawnLocations == null || spawnOpposite.can_move)) {
                spawnDirection = opposite;
                spawnLocations = spawnOpposite;
            }
        }

        // If still not possible, try centered if we had not tried yet, just in case
        if (spawnLocations == null && !useCentering)
            spawnLocations = spawnable.findSpawnLocations(info.getRailPiece(), spawnDirection, SpawnableGroup.SpawnMode.CENTER);

        return spawnLocations;
    }
}