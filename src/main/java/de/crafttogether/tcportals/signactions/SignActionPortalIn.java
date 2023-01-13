package de.crafttogether.tcportals.signactions;

import com.bergerkiller.bukkit.common.utils.LogicUtil;
import com.bergerkiller.bukkit.tc.events.SignActionEvent;
import com.bergerkiller.bukkit.tc.events.SignChangeActionEvent;
import com.bergerkiller.bukkit.tc.signactions.SignAction;
import com.bergerkiller.bukkit.tc.signactions.SignActionType;
import com.bergerkiller.bukkit.tc.utils.SignBuildOptions;
import de.crafttogether.TCPortals;
import de.crafttogether.common.localization.Placeholder;
import de.crafttogether.tcportals.Localization;
import de.crafttogether.tcportals.portals.Portal;
import de.crafttogether.tcportals.portals.PortalHandler;
import de.crafttogether.tcportals.util.TCHelper;
import de.crafttogether.tcportals.util.Util;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class SignActionPortalIn extends SignAction {
    private final TCPortals plugin = TCPortals.plugin;

    @Override
    public boolean match(SignActionEvent event) {
        return event.getLine(1).equalsIgnoreCase("portal-in");
    }

    @Override
    public void execute(SignActionEvent event) {
        if (event.getGroup() == null
                || !event.hasMember()
                || !event.isPowered()
                || !event.isAction(SignActionType.MEMBER_ENTER, SignActionType.REDSTONE_ON))
            return;

        PortalHandler portalHandler = plugin.getPortalHandler();

        if (!portalHandler.getPendingTeleports().containsKey(event.getGroup()))
            portalHandler.handleTrain(event);

        if (event.isTrainSign())
            portalHandler.handleCart(event);
    }

    @Override
    public boolean build(SignChangeActionEvent event) {
        if (!Util.checkPermission(event.getPlayer(), "tcportals.create.entrance")) {
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
                    Placeholder.set("error", e.getMessage()));

            e.printStackTrace();
            TCHelper.displayError(event);
            return false;
        }

        if (portals.size() < 1)
            Localization.PORTAL_CREATE_IN_NOTEXIST.message(event.getPlayer(),
                    Placeholder.set("name", portalName));

        Localization.PORTAL_CREATE_IN_SUCCESS.message(event.getPlayer(),
                Placeholder.set("name", portalName));

        SignBuildOptions.create()
                .setName("ServerPortal-Entrance").setDescription("allow trains to travel between servers" + ((event.getLine(3).equalsIgnoreCase("clear")) ? ".\n§eChest-Minecarts will be cleared" : ""))
                .handle(event.getPlayer());

        return true;
    }
}