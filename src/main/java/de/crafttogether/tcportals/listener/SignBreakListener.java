package de.crafttogether.tcportals.listener;

import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.portals.Portal;
import de.crafttogether.tcportals.util.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class SignBreakListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (!(block.getState() instanceof Sign sign)) {
            return;
        }

        if (!Portal.isValid(sign)) {
            return;
        }

        Location brokenLocation = block.getLocation();

        Portal portal = TCPortals.plugin.getPortalStorage().getPortals().stream()
                .filter(p -> p.getTargetLocation() != null)
                .filter(p -> p.getTargetLocation().getServer().equals(TCPortals.plugin.getServerName()))
                .filter(p -> p.getTargetLocation().getWorld().equals(brokenLocation.getWorld().getName()))
                .filter(p -> (int) p.getTargetLocation().getX() == brokenLocation.getBlockX())
                .filter(p -> (int) p.getTargetLocation().getY() == brokenLocation.getBlockY())
                .filter(p -> (int) p.getTargetLocation().getZ() == brokenLocation.getBlockZ())
                .findFirst()
                .orElse(null);

        if (portal == null) {
            Util.debug("SignBreakListener: no portal found at "
                    + brokenLocation.getWorld().getName() + " "
                    + brokenLocation.getBlockX() + " "
                    + brokenLocation.getBlockY() + " "
                    + brokenLocation.getBlockZ());
            return;
        }

        TCPortals.plugin.getPortalStorage().delete(portal.getId(), (err, rows) -> {
            if (err != null) {
                TCPortals.plugin.getLogger().warning("Could not delete portal '" + portal.getName() + "': " + err.getMessage());

                TCPortals.plugin.getServer().getScheduler().runTask(TCPortals.plugin, () ->
                        event.getPlayer().sendMessage("§6TC-Portals » §cPortal could not be removed."));
                return;
            }

            int affectedRows = rows == null ? 0 : rows;
            Util.debug("Deleted portal '" + portal.getName() + "' because sign was broken. Rows: " + affectedRows);

            if (affectedRows > 0) {
                TCPortals.plugin.getServer().getScheduler().runTask(TCPortals.plugin, () ->
                        event.getPlayer().sendMessage("§6TC-Portals » §aPortal §e" + portal.getName() + " §awas removed."));
            }
        });
    }
}
