package de.crafttogether.tcportals.listener;

import de.crafttogether.tcportals.portals.Passenger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Passenger.remove(event.getPlayer().getUniqueId());
    }
}
