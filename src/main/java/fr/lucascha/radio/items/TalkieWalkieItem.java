package fr.lucascha.radio.items;

import fr.lucascha.radio.RadioPlugin;
import fr.lucascha.radio.frequencies.Frequency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class TalkieWalkieItem {

    public static final NamespacedKey KEY_IS_TALKIE = new NamespacedKey(RadioPlugin.getInstance(), "is_talkie_walkie");
    public static final NamespacedKey KEY_FREQUENCY  = new NamespacedKey(RadioPlugin.getInstance(), "talkie_frequency");

    public static ItemStack create() { return create(null); }

    public static ItemStack create(Frequency frequency) {
        // RECOVERY_COMPASS comme base visuelle, mais on utilise ItemMeta (pas CompassMeta)
        ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("📻 Talkie-Walkie")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));

        meta.lore(buildLore(frequency));
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_IS_TALKIE, PersistentDataType.BOOLEAN, true);
        if (frequency != null) {
            pdc.set(KEY_FREQUENCY, PersistentDataType.STRING, frequency.name());
        }

        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack updateFrequency(ItemStack item, Frequency frequency) {
        if (!isTalkieWalkie(item)) return item;
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(KEY_FREQUENCY, PersistentDataType.STRING, frequency.name());
        meta.lore(buildLore(frequency));
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isTalkieWalkie(ItemStack item) {
        if (item == null || item.getType() != Material.RECOVERY_COMPASS) return false;
        if (!item.hasItemMeta()) return false;
        return Boolean.TRUE.equals(item.getItemMeta().getPersistentDataContainer()
                .get(KEY_IS_TALKIE, PersistentDataType.BOOLEAN));
    }

    public static Frequency getFrequency(ItemStack item) {
        if (!isTalkieWalkie(item)) return null;
        String name = item.getItemMeta().getPersistentDataContainer()
                .get(KEY_FREQUENCY, PersistentDataType.STRING);
        if (name == null) return null;
        try { return Frequency.valueOf(name); } catch (IllegalArgumentException e) { return null; }
    }

    private static List<Component> buildLore(Frequency frequency) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("  Clic droit : Choisir la frequence")
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        if (frequency != null) {
            lore.add(Component.text("  Frequence : ")
                    .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(frequency.getIcon() + " " + frequency.getDisplayName())
                            .color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, true)));
        } else {
            lore.add(Component.text("  Frequence : Non definie")
                    .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        return lore;
    }
}
