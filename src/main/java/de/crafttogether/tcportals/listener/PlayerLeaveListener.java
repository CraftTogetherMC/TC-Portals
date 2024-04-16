package de.crafttogether.tcportals.listener;

import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.portals.Passenger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerLeaveListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerJoinEvent event) {
        Passenger.remove(event.getPlayer().getUniqueId());
    }
}
