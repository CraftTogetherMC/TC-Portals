package de.crafttogether.tcportals.listener;

import de.crafttogether.CTCommons;
import de.crafttogether.TCPortals;
import de.crafttogether.common.shaded.net.kyori.adventure.text.ComponentLike;
import de.crafttogether.ctcommons.CTCommonsBukkit;
import de.crafttogether.ctcommons.commands.UpdateCommand;
import de.crafttogether.tcportals.Update;
import de.crafttogether.tcportals.portals.Passenger;
import de.crafttogether.tcportals.portals.PortalHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private static final TCPortals plugin = TCPortals.plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Look if player should be a passenger
        Passenger passenger = Passenger.get(event.getPlayer().getUniqueId());
        if (passenger != null)
            PortalHandler.reEnterPlayer(passenger, event);

        if (!event.getPlayer().hasPermission("tcportals.notify.updates"))
            return;

        Configuration config = plugin.getConfig();

        if (config.getBoolean("Settings.Updates.Notify.DisableNotifications")
                || !config.getBoolean("Settings.Updates.Notify.InGame"))
            return;

        Update.check((err, feedback) -> CTCommonsBukkit.audiences.player(event.getPlayer()).sendMessage(err == null ? feedback : (ComponentLike) Component.text(err.getMessage())), 0L);
    }
}