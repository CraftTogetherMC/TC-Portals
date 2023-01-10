package de.crafttogether.tcportals.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.tcportals.portals.Passenger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class TrainExitListener implements Listener {

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (MinecartMemberStore.getFromEntity(event.getExited()) != null)
            Passenger.remove(event.getExited().getUniqueId());
    }
}