package de.crafttogether.tcportals.net.packets;

import de.crafttogether.common.NetworkLocation;
import de.crafttogether.common.messaging.packets.Packet;
import de.crafttogether.tcportals.portals.Passenger;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrainPacket extends Packet {
    public final UUID id;

    public String name;
    public double speed;
    public double waitDistance;
    public String portal;
    public Set<String> owners;
    public String config;
    public List<Passenger> passengers;
    public NetworkLocation target;
    public String source;

    public TrainPacket(UUID trainId) {
        this.id = trainId;
    }
}