package de.crafttogether.tcportals.portals;

import de.crafttogether.TCPortals;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Passenger implements Serializable {
    private static final ConcurrentHashMap<UUID, Passenger> passengers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Component> errors = new ConcurrentHashMap<>();

    private final UUID trainId;
    private final UUID uuid;
    private final EntityType type;
    private final int cartIndex;

    private String trainName = null;

    public Passenger(UUID trainId, UUID uuid, EntityType type, int cartIndex) {
        this.trainId = trainId;
        this.uuid = uuid;
        this.type = type;
        this.cartIndex = cartIndex;
    }

    public boolean hasError() {
        return errors.get(trainId) != null;
    }

    public Component getError() {
        return errors.get(trainId);
    }

    public UUID getTrainId() {
        return trainId;
    }

    public void setTrainName(String trainName) {
        this.trainName = trainName;
    }

    public String getTrainName() { return trainName; }

    public UUID getUUID() { return this.uuid; }

    public EntityType getType() { return type; }

    public int getCartIndex() { return this.cartIndex; }

    public static void register(Passenger passenger) {
        passengers.put(passenger.getUUID(), passenger);
    }

    public static void error(UUID trainId, Component error) {
        errors.put(trainId, error);
        sendMessage(trainId, error);
    }

    public static void setTrainName(UUID trainId, String trainName) {
        for (Passenger passenger : passengers.values()) {
            if (passenger.trainId.equals(trainId))
                passenger.trainName = trainName;
        }
    }

    public static String getTrainName(UUID trainId) {
        for (Passenger passenger : passengers.values()) {
            if (passenger.trainId.equals(trainId))
                return passenger.trainName;
        }

        return null;
    }

    public static void sendMessage(UUID trainId, Component message) {
        List<Passenger> passengerList = passengers.values().stream()
                .filter(passenger -> passenger.type.equals(EntityType.PLAYER))
                .filter(passenger -> passenger.trainId.equals(trainId))
                .toList();

        for (Passenger passenger : passengerList) {
            Player player = Bukkit.getPlayer(passenger.getUUID());
            if (player != null && player.isOnline())
                TCPortals.plugin.adventure().player(player).sendMessage(message);
        }
    }

    public static void remove(UUID uuid) {
        passengers.remove(uuid);
    }

    public static void removeTrain(UUID trainId) {
        for (Passenger passenger : passengers.values()) {
            if (passenger.trainId.equals(trainId)) {
                passengers.remove(trainId);
                errors.remove(trainId);
            }
        }
    }

    public static Passenger get(UUID uuid) {
        if (passengers.containsKey(uuid))
            return passengers.get(uuid);
        return null;
    }
}