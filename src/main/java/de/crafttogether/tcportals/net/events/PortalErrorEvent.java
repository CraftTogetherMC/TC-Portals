package de.crafttogether.tcportals.net.events;

import de.crafttogether.common.messaging.ConnectionState;
import de.crafttogether.common.messaging.events.ConnectionErrorEvent;

import java.util.UUID;

public class PortalErrorEvent extends ConnectionErrorEvent {
    private final UUID trainId;

    public PortalErrorEvent(ConnectionState state, UUID trainId, String targetHost, int targetPort) {
        super(state, targetHost, targetPort);
        this.trainId = trainId;
    }

    public UUID getTrainId() {
        return trainId;
    }
}
