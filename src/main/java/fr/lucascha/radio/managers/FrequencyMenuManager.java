package fr.lucascha.radio.managers;

import fr.lucascha.radio.RadioPlugin;
import fr.lucascha.radio.frequencies.Frequency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class FrequencyMenuManager {

    public static final NamespacedKey MENU_FREQ_KEY =
            new NamespacedKey(RadioPlugin.getInstance(), "menu_frequency");

    public static final String MENU_TITLE = "📻 Choisir une Frequence";

    public static void openMenu(Player player, Frequency currentFreq) {
        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(MENU_TITLE).color(NamedTextColor.GOLD)
                        .decoration(TextDecoration.BOLD, true));

        // Remplissage déco
        ItemStack filler = createFiller();
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        // Fréquences : slots du milieu
        int[] slots = {10, 12, 14, 16, 11, 13};
        Frequency[] freqs = Frequency.values();
        for (int i = 0; i < freqs.length && i < slots.length; i++) {
            boolean hasAccess = player.hasPermission(freqs[i].getPermission());
            inv.setItem(slots[i], createFrequencyItem(freqs[i], hasAccess, freqs[i] == currentFreq));
        }

        inv.setItem(22, createCloseButton());
        player.openInventory(inv);
    }

    private static ItemStack createFrequencyItem(Frequency freq, boolean hasAccess, boolean isCurrent) {
        ItemStack item = new ItemStack(hasAccess ? freq.getConcreteMaterial() : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        if (hasAccess) {
            meta.displayName(Component.text(freq.getIcon() + " " + freq.getDisplayName())
                    .color(isCurrent ? NamedTextColor.GREEN : NamedTextColor.WHITE)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            meta.displayName(Component.text("🔒 " + freq.getDisplayName())
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false));
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        if (hasAccess) {
            if (isCurrent) {
                lore.add(Component.text("  ✔ Frequence active")
                        .color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            } else {
                lore.add(Component.text("  Clic pour rejoindre")
                        .color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            }
        } else {
            lore.add(Component.text("  Acces refuse")
                    .color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        meta.lore(lore);

        // Tag NBT pour identifier la fréquence
        meta.getPersistentDataContainer().set(MENU_FREQ_KEY, PersistentDataType.STRING, freq.name());
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createFiller() {
        ItemStack g = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta m = g.getItemMeta();
        m.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        g.setItemMeta(m);
        return g;
    }

    private static ItemStack createCloseButton() {
        ItemStack item = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("✖ Fermer")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    public static Frequency getFrequencyFromMenuItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String name = item.getItemMeta().getPersistentDataContainer()
                .get(MENU_FREQ_KEY, PersistentDataType.STRING);
        if (name == null) return null;
        try { return Frequency.valueOf(name); } catch (IllegalArgumentException e) { return null; }
    }
}
