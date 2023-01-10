package de.crafttogether.tcportals.signactions;

import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.localization.PlaceholderResolver;
import de.crafttogether.tcportals.portals.Portal;
import de.crafttogether.tcportals.portals.PortalHandler;
import de.crafttogether.tcportals.util.CTLocation;
import de.crafttogether.tcportals.util.TCHelper;
import de.crafttogether.tcportals.util.Util;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SignActionPortal extends SignAction {
    private final TCPortals plugin = TCPortals.plugin;

    @Override
    public boolean match(SignActionEvent event) {
        return event.getLine(1).equalsIgnoreCase("portal");
    }

    @Override
    public void execute(SignActionEvent event) {
       if (event.getGroup() == null
                || !event.hasMember()
                || !event.isPowered()
                || !event.isAction(SignActionType.MEMBER_ENTER, SignActionType.REDSTONE_ON))
            return;

        //if (event.isAction(SignActionType.MEMBER_LEAVE)) {
            //plugin.getPortalHandler().getReceivedTrains().remove(event.getGroup());
            //return;
        //}

        PortalHandler portalHandler = plugin.getPortalHandler();

        if (!portalHandler.getPendingTeleports().containsKey(event.getGroup()))
            portalHandler.handleTrain(event);

        if (event.isTrainSign())
            portalHandler.handleCart(event);
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!Util.checkPermission(event.getPlayer(), "tcportals.create.bidirectional")) {
            TCHelper.displayError(event);
            return false;
        }

        String[] lines = event.getLines();
        String portalName = lines[2];

        // Validate third line
        if (LogicUtil.nullOrEmpty(portalName)) {
            Localization.PORTAL_CREATE_NONAME.message(event.getPlayer());
            TCHelper.displayError(event);
            return false;
        }

        // Get existing portals from database
        List<Portal> portals;
        try {
            portals = plugin.getPortalStorage().get(portalName).stream()
                    .filter(portal -> portal.getType().equals(Portal.PortalType.BIDIRECTIONAL))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            Localization.ERROR_DATABASE.message(event.getPlayer(),
                    PlaceholderResolver.resolver("error", e.getMessage()));

            e.printStackTrace();
            TCHelper.displayError(event);
            return false;
        }

        // Create sign
        if (portals.size() == 0 || portals.size() == 1) {

            // First sign created
            if (portals.size() == 0) {
                Localization.PORTAL_CREATE_BIDIRECTIONAL_INFO_FIRST.message(event.getPlayer(),
                        PlaceholderResolver.resolver("name", portalName));
            }

            // Sign updated
            else if (portals.get(0).getTargetLocation().equals(CTLocation.fromBukkitLocation(event.getLocation()))) {
                // TODO: Handle sign-updates
                Util.debug("Sign update");
                return true;
            }

            // Second sign created
            else {
                Portal portal = portals.get(0);

                // One sign on this server already exists
                if (portal.getTargetLocation().getServer().equals(plugin.getServerName())) {
                    Localization.PORTAL_CREATE_BIDIRECTIONAL_SAMESERVER.message(event.getPlayer());
                    TCHelper.displayError(event);
                    return false;
                }

                Localization.PORTAL_CREATE_BIDIRECTIONAL_INFO_SECOND.message(event.getPlayer(),
                        PlaceholderResolver.resolver("name", portal.getName()),
                        PlaceholderResolver.resolver("server", portal.getTargetLocation().getServer()),
                        PlaceholderResolver.resolver("world", portal.getTargetLocation().getWorld()),
                        PlaceholderResolver.resolver("x", String.valueOf(portal.getTargetLocation().getX())),
                        PlaceholderResolver.resolver("y", String.valueOf(portal.getTargetLocation().getY())),
                        PlaceholderResolver.resolver("z", String.valueOf(portal.getTargetLocation().getZ())));
            }

            // Save to database
            try {
                plugin.getPortalStorage().create(
                        portalName,
                        Portal.PortalType.BIDIRECTIONAL,
                        plugin.getConfig().getString("Portals.Server.PublicAddress"),
                        plugin.getConfig().getInt("Portals.Server.Port"),
                        CTLocation.fromBukkitLocation(event.getLocation()));
            } catch (SQLException e) {
                Localization.ERROR_DATABASE.message(event.getPlayer(),
                        PlaceholderResolver.resolver("error", e.getMessage()));

                e.printStackTrace();
                TCHelper.displayError(event);
                return false;
            }

            Localization.PORTAL_CREATE_BIDIRECTIONAL_SUCCESS.message(event.getPlayer(),
                    PlaceholderResolver.resolver("name", portalName));
        }

        // There are already two signs
        else {
            Localization.PORTAL_CREATE_BIDIRECTIONAL_EXISTS.message(event.getPlayer(),
                    PlaceholderResolver.resolver("name", portalName));
            TCHelper.displayError(event);
            return false;
        }

        SignBuildOptions.create()
                .setName("ServerPortal").setDescription("allow trains to travel between servers" + ((event.getLine(3).equalsIgnoreCase("clear")) ? ".\n§eChest-Minecarts will be cleared" : ""))
                .handle(event.getPlayer());

        return true;
    }
}