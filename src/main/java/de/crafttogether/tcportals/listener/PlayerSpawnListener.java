package de.crafttogether.tcportals.listener;

import de.crafttogether.tcportals.portals.Passenger;
import de.crafttogether.tcportals.portals.PortalHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class PlayerSpawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSpawn(PlayerSpawnLocationEvent event) {
        /*
        // Look if player should be a passenger
        Passenger passenger = Passenger.get(event.getPlayer().getUniqueId());

        if (passenger != null)
            PortalHandler.reEnterPlayer(passenger, event);
        */
    }
}