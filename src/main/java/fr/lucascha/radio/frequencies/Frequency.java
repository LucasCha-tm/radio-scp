package fr.lucascha.radio.frequencies;

import org.bukkit.Material;

public enum Frequency {
    SECU(Material.BLUE_CONCRETE,    "Securite",           "radio.frequency.secu",    "§9", "🔵", false),
    SCIENTOS(Material.WHITE_CONCRETE, "Scientos",         "radio.frequency.scientos","§f", "⚪", false),
    ADMIN(Material.GREEN_CONCRETE,  "Admin",              "radio.frequency.admin",   "§2", "🟢", false),
    DIST(Material.GRAY_CONCRETE,    "DIST",               "radio.frequency.dist",    "§8", "⬜", false),
    MEDECIN(Material.LIME_CONCRETE, "Medecin",            "radio.frequency.medecin", "§a", "🟩", false),
    IC(Material.BLACK_CONCRETE,     "IC/Chaos Insurgency","radio.frequency.ic",      "§0", "⬛", true);

    private final Material material;
    private final String displayName;
    private final String permission;
    private final String colorCode;
    private final String icon;
    private final boolean restricted;

    Frequency(Material material, String displayName, String permission,
              String colorCode, String icon, boolean restricted) {
        this.material = material;
        this.displayName = displayName;
        this.permission = permission;
        this.colorCode = colorCode;
        this.icon = icon;
        this.restricted = restricted;
    }

    public Material getConcreteMaterial() { return material; }
    public String getDisplayName()        { return displayName; }
    public String getPermission()         { return permission; }
    public String getColorCode()          { return colorCode; }
    public String getIcon()               { return icon; }
    public boolean isRestricted()         { return restricted; }

    public String getColoredName() {
        return colorCode + icon + " " + displayName + "§r";
    }

    public static Frequency fromMaterial(Material m) {
        for (Frequency f : values()) if (f.material == m) return f;
        return null;
    }
}
