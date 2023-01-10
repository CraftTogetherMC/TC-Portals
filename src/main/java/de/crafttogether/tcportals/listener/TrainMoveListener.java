package de.crafttogether.tcportals.listener;

import com.bergerkiller.bukkit.tc.events.MemberBlockChangeEvent;
import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.portals.PortalHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TrainMoveListener implements Listener {

    @EventHandler
    public void onMemberMove(MemberBlockChangeEvent event) {
        if (!event.getGroup().head().equals(event.getMember()))
            return;

        PortalHandler portalHandler = TCPortals.plugin.getPortalHandler();
        if (!portalHandler.getReceivedTrains().containsKey(event.getGroup()))
            return;

        portalHandler.getReceivedTrains().get(event.getGroup()).move(1);
    }
}