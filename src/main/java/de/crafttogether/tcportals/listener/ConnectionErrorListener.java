package de.crafttogether.tcportals.listener;

import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.localization.PlaceholderResolver;
import de.crafttogether.tcportals.net.events.ConnectionErrorEvent;
import de.crafttogether.tcportals.portals.Passenger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ConnectionErrorListener implements Listener {
    private static final TCPortals plugin = TCPortals.plugin;

    @EventHandler
    public void handleError(ConnectionErrorEvent event) {
        String trainName = Passenger.getTrainName(event.getTrainId());

        switch (event.getError()) {
            case CONNECTION_REFUSED -> {
                Passenger.sendMessage(event.getTrainId(), Localization.PORTAL_ENTER_CONNECTIONREFUSED.deserialize(
                        PlaceholderResolver.resolver("host", event.getTargetHost()),
                        PlaceholderResolver.resolver("port", event.getTargetPort())));
                plugin.getLogger().warning("(" + trainName + ") A connection to " + event.getTargetHost() + ":" + event.getTargetPort() +" could not be established");
            }

            case NO_REMOTE_CONNECTIONS ->  {
                Passenger.sendMessage(event.getTrainId(), Localization.PORTAL_ENTER_NOREMOTECONNECTIONS.deserialize(
                        PlaceholderResolver.resolver("host", event.getTargetHost()),
                        PlaceholderResolver.resolver("port", event.getTargetPort())));
                plugin.getLogger().warning("(" + trainName + ") A connection to " + event.getTargetHost() + ":" + event.getTargetPort() + " was refused, because remote connections are not allowed!");
            }

            case NOT_AUTHENTICATED -> {
                Passenger.sendMessage(event.getTrainId(), Localization.PORTAL_ENTER_NOTAUTHENTICATED.deserialize(
                        PlaceholderResolver.resolver("host", event.getTargetHost()),
                        PlaceholderResolver.resolver("port", event.getTargetPort())));
                plugin.getLogger().warning("(" + trainName + ") Data was sent to " + event.getTargetHost() + ":" + event.getTargetPort() + " without authentication taking place.");
            }

            case INVALID_AUTHENTICATION ->  {
                Passenger.sendMessage(event.getTrainId(), Localization.PORTAL_ENTER_INVALIDAUTH.deserialize(
                        PlaceholderResolver.resolver("host", event.getTargetHost()),
                        PlaceholderResolver.resolver("port", event.getTargetPort())));
                plugin.getLogger().warning("(" + trainName + ") A connection to " + event.getTargetHost() + ":" + event.getTargetPort() + " was refused because an invalid 'SecretKey' was sent.");
                plugin.getLogger().warning("Please check your config.yml!");
            }
        }

        Passenger.removeTrain(event.getTrainId());
    }
}
