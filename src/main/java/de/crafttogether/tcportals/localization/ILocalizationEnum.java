package de.crafttogether.tcportals.localization;

/*
  Copyright (C) 2013-2022 bergerkiller
 */

import com.bergerkiller.bukkit.common.utils.LogicUtil;
import de.crafttogether.TCPortals;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Interface for a LocalizationEnum. Can be implemented by
 * an actual enum to provide localization constants and defaults.
 */
public interface ILocalizationEnum extends ILocalizationDefault {
    /**
     * Sends this Localization message to the sender specified
     *
     * @param sender to send to
     * @param arguments for the node
     */
    default void message(CommandSender sender, PlaceholderResolver... arguments) {
        TCPortals.plugin.adventure().sender(sender).sendMessage(deserialize(arguments));
    }

    /**
     * Returns the deserialized Localization message to the sender specified
     *
     * @param arguments for the node
     */
    default Component deserialize(PlaceholderResolver... arguments) {
        String text = get();

        if (LogicUtil.nullOrEmpty(text))
            return null;

        List<PlaceholderResolver> resolvers = new ArrayList<>(Arrays.stream(arguments).toList());

        for (PlaceholderResolver resolver : resolvers)
            text = resolver.resolve(text);

        return TCPortals.plugin.getMiniMessageParser().deserialize(text);
    }

    /**
     * Gets the locale value for this Localization node
     *
     * @return Locale value
     */
    String get();
}
