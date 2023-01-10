package de.crafttogether.tcportals.localization;

public class PlaceholderResolver {
    private final String name;
    private final String value;

    private PlaceholderResolver(String key, String value) {
        this.name = key;
        this.value = value;
    }

    public static PlaceholderResolver resolver(String key, String value) {
        return new PlaceholderResolver(key, value);
    }

    public static PlaceholderResolver resolver(String key, int value) {
        return new PlaceholderResolver(key, String.valueOf(value));
    }

    public static PlaceholderResolver resolver(String key, double value) {
        return new PlaceholderResolver(key, String.valueOf(value));
    }

    public String resolve(String text) {
        return text.replace("{" + this.name + "}", this.value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
