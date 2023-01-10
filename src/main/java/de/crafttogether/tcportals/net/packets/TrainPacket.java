package de.crafttogether.tcportals.net.packets;

import de.crafttogether.tcportals.portals.Passenger;
import de.crafttogether.tcportals.util.CTLocation;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TrainPacket implements Packet {
    public final UUID id;

    public String name;
    public double speed;
    public String portal;
    public Set<String> owners;
    public String config;
    public List<Passenger> passengers;
    public CTLocation target;
    public String source;

    public TrainPacket(UUID trainId) {
        this.id = trainId;
    }
}