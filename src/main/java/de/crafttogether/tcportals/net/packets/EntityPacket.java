package de.crafttogether.tcportals.net.packets;

import de.crafttogether.common.messaging.packets.Packet;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class EntityPacket extends Packet {
    public final UUID uuid;
    public final EntityType type;

    public EntityPacket(UUID uuid, EntityType entityType) {
        this.uuid = uuid;
        this.type = entityType;
    }
}
