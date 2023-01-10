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
import de.crafttogether.tcportals.util.CTLocation;
import de.crafttogether.tcportals.util.TCHelper;
import de.crafttogether.tcportals.util.Util;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SignActionPortalOut extends SignAction {
    private final TCPortals plugin = TCPortals.plugin;

    @Override
    public boolean match(SignActionEvent event) {
        return event.getLine(1).equalsIgnoreCase("portal-out");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.isAction(SignActionType.MEMBER_LEAVE))
            plugin.getPortalHandler().getReceivedTrains().remove(event.getGroup());
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!Util.checkPermission(event.getPlayer(), "tcportals.create.exit")) {
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

        // Get existing portal-out -signs from database
        List<Portal> portals;
        try {
            portals = plugin.getPortalStorage().get(portalName).stream()
                    .filter(portal -> portal.getType().equals(Portal.PortalType.OUT))
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            Localization.ERROR_DATABASE.message(event.getPlayer(),
                    PlaceholderResolver.resolver("error", e.getMessage()));

            e.printStackTrace();
            TCHelper.displayError(event);
            return false;
        }

        // Save to database
        if (portals.size() == 0) {
            try {
                plugin.getPortalStorage().create(
                        portalName,
                        Portal.PortalType.OUT,
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

            Localization.PORTAL_CREATE_OUT_SUCCESS.message(event.getPlayer(),
                    PlaceholderResolver.resolver("name", portalName));
        }

        // There are already two signs
        else {
            Portal portal = portals.get(0);

            Localization.PORTAL_CREATE_OUT_EXIST.message(event.getPlayer(),
                    PlaceholderResolver.resolver("name", portal.getName()),
                    PlaceholderResolver.resolver("server", portal.getTargetLocation().getServer()),
                    PlaceholderResolver.resolver("world", portal.getTargetLocation().getWorld()),
                    PlaceholderResolver.resolver("x", String.valueOf(portal.getTargetLocation().getX())),
                    PlaceholderResolver.resolver("y", String.valueOf(portal.getTargetLocation().getY())),
                    PlaceholderResolver.resolver("z", String.valueOf(portal.getTargetLocation().getZ())));
            TCHelper.displayError(event);
            return false;
        }

        SignBuildOptions.create()
                .setName("ServerPortal-Exit").setDescription("allow trains to travel between servers" + ((event.getLine(3).equalsIgnoreCase("clear")) ? ".\n§eChest-Minecarts will be cleared" : ""))
                .handle(event.getPlayer());

        return true;
    }
}