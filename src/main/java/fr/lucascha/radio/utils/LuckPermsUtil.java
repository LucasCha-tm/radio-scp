package fr.lucascha.radio.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LuckPermsUtil {

    private static LuckPerms api;

    public static boolean init() {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) return false;
        try {
            api = LuckPermsProvider.get();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    public static void addPermission(Player player, String permission) {
        if (api == null) return;
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        user.data().add(Node.builder(permission).build());
        api.getUserManager().saveUser(user);
    }

    public static void removePermission(Player player, String permission) {
        if (api == null) return;
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;
        user.data().clear(n -> n.getKey().equals(permission));
        api.getUserManager().saveUser(user);
    }

    public static boolean isAvailable() { return api != null; }
}
