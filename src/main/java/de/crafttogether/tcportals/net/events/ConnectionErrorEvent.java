package de.crafttogether.tcportals.net.events;

import de.crafttogether.tcportals.net.TCPClient;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ConnectionErrorEvent extends Event {
    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private final TCPClient.Error error;
    private final UUID trainId;
    private final String targetHost;
    private final int targetPort;

    public ConnectionErrorEvent(TCPClient.Error error, UUID trainId, String targetHost, int targetPort) {
        this.error = error;
        this.trainId = trainId;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public TCPClient.Error getError() {
        return error;
    }

    public UUID getTrainId() {
        return trainId;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
