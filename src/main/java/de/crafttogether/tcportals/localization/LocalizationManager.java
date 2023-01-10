package de.crafttogether.tcportals.localization;

/*
  Copyright (C) 2013-2022 bergerkiller
 */

import com.bergerkiller.bukkit.common.config.FileConfiguration;
import com.bergerkiller.bukkit.common.utils.CommonUtil;
import de.crafttogether.TCPortals;
import de.crafttogether.tcportals.Localization;

import java.util.Locale;

public class LocalizationManager {
    private static final TCPortals plugin = TCPortals.plugin;

    private final FileConfiguration localizationconfig;

    public LocalizationManager() {
        // Load localization configuration
        this.localizationconfig = new FileConfiguration(TCPortals.plugin, "localization.yml");

        // load
        if (this.localizationconfig.exists()) {
            this.loadLocalization();
        }

        // header
        this.localizationconfig.setHeader("Below are the localization nodes set for plugin '" + plugin.getName() + "'.");
        this.localizationconfig.addHeader("For colors, text-formatting, click-events and more, you can use the MiniMessage format.");
        this.localizationconfig.addHeader("https://docs.adventure.kyori.net/minimessage/format.html");

        // load
        this.loadLocales(Localization.class);

        if (!this.localizationconfig.isEmpty()) {
            this.saveLocalization();
        }
    }

    /**
     * Loads all the localization defaults from a Localization container
     * class<br>
     * If the class is not an enumeration, the static constants in the class are
     * used instead
     *
     * @param localizationDefaults class
     */
    public void loadLocales(Class<? extends ILocalizationDefault> localizationDefaults) {
        for (ILocalizationDefault def : CommonUtil.getClassConstants(localizationDefaults)) {
            this.loadLocale(def);
        }
    }

    /**
     * Loads a localization using a localization default
     *
     * @param localizationDefault to load from
     */
    public void loadLocale(ILocalizationDefault localizationDefault) {
        localizationDefault.initDefaults(this.localizationconfig);
    }

    /**
     * Loads a single Localization value<br>
     * Adds this node to the localization configuration if it wasn't added
     *
     * @param path to the value (case-insensitive, can not be null)
     * @param defaultValue for the value
     */
    public void loadLocale(String path, String defaultValue) {
        path = path.toLowerCase(Locale.ENGLISH);
        if (!this.localizationconfig.contains(path)) {
            this.localizationconfig.set(path, defaultValue);
        }
    }

    /**
     * Gets a localization value
     *
     * @param path to the localization value (case-insensitive, can not be null)
     * @return Localization value
     */
    public String getLocale(String path) {
        path = path.toLowerCase(Locale.ENGLISH);
        // First check if the path leads to a node
        if (this.localizationconfig.isNode(path)) {
            // Redirect to the proper sub-node
            // Check recursively if the arguments are contained
            // Update path to lead to the new path
            path = path + ".default";
        }

        return this.localizationconfig.get(path, String.class, "");
    }

    public final void loadLocalization() {
        this.localizationconfig.load();
    }
    public final void saveLocalization() {
        this.localizationconfig.save();
    }
}
