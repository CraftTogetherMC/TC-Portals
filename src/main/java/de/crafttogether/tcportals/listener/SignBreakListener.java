package de.crafttogether.tcportals.listener;

import com.bergerkiller.bukkit.common.utils.BlockUtil;
import com.bergerkiller.bukkit.common.utils.FaceUtil;
import com.bergerkiller.bukkit.tc.SignActionHeader;
import com.bergerkiller.bukkit.tc.controller.components.RailPiece;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.portals.Portal;
import de.crafttogether.tcportals.util.TCHelper;
import de.crafttogether.tcportals.util.Util;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SignBreakListener implements Listener {

    @EventHandler
    public void onSignAction(BlockBreakEvent event) {
        Sign sign = BlockUtil.getSign(event.getBlock());

        Util.debug("BlockBreakEvent");
        if (Portal.isValid(sign)) {
            SignActionEvent signAction = new SignActionEvent(event.getBlock());
            RailPiece railPiece = signAction.getRailPiece();

            if (railPiece.isNone())
                return;

            Portal portal = TCPortals.plugin.getPortalStorage().getPortal(railPiece.block().getLocation());

            if (portal != null) {
                TCPortals.plugin.getPortalStorage().delete(portal.getId(), (err, rows) -> {
                    Util.debug("BlockBreakEvent: removed sign " + TCHelper.signToString(sign.getLines()));
                });
            }
        }
    }
}
