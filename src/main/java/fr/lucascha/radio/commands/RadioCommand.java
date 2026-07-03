package fr.lucascha.radio.commands;

import fr.lucascha.radio.RadioPlugin;
import fr.lucascha.radio.frequencies.Frequency;
import fr.lucascha.radio.items.TalkieWalkieItem;
import fr.lucascha.radio.managers.RadioManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RadioCommand implements CommandExecutor, TabCompleter {

    private final RadioPlugin plugin;

    public RadioCommand(RadioPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {

        String prefix = plugin.getConfig().getString("messages.prefix", "§8[§6Radio§8] §r");

        // Pas d'argument → aide
        if (args.length == 0) {
            sendHelp(sender, prefix);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "help" -> sendHelp(sender, prefix);

            case "reload" -> {
                if (!sender.hasPermission("radio.reload")) {
                    sender.sendMessage(Component.text(prefix + "§cPermission refusee."));
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(Component.text(prefix + "§aConfig rechargee."));
            }

            case "give" -> {
                if (!sender.hasPermission("radio.give")) {
                    sender.sendMessage(Component.text(prefix + "§cPermission refusee."));
                    return true;
                }

                // Cible = argument 1, sinon soi-même
                Player target;
                if (args.length >= 2) {
                    target = null;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().equalsIgnoreCase(args[1])) {
                            target = p;
                            break;
                        }
                    }
                    if (target == null) {
                        sender.sendMessage(Component.text(prefix + "§cJoueur §e" + args[1] + " §cintrouvable ou hors ligne."));
                        return true;
                    }
                } else {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Component.text(prefix + "§cUsage : /radio give <joueur>"));
                        return true;
                    }
                    target = (Player) sender;
                }

                // Fréquence optionnelle = argument 2
                Frequency freq = null;
                if (args.length >= 3) {
                    try {
                        freq = Frequency.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(Component.text(prefix + "§cFrequence inconnue : §e" + args[2]));
                        return true;
                    }
                }

                ItemStack talkie = TalkieWalkieItem.create(freq);
                target.getInventory().addItem(talkie);
                sender.sendMessage(Component.text(prefix + "§aTalkie donne a §e" + target.getName()));
                if (!target.equals(sender)) {
                    target.sendMessage(Component.text(prefix + "§aVous avez recu un Talkie-Walkie !"));
                }
            }

            case "info" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text(prefix + "§cJoueurs uniquement."));
                    return true;
                }
                Frequency freq = RadioManager.getInstance().getFrequency(player);
                if (freq == null) {
                    player.sendMessage(Component.text(prefix + "§eAucune frequence active."));
                } else {
                    long count = RadioManager.getInstance().countPlayersOnFrequency(freq);
                    player.sendMessage(Component.text(prefix + "§7Frequence : " + freq.getColoredName() + " §7(" + count + " joueur(s))"));
                }
            }

            default -> sendHelp(sender, prefix);
        }

        return true;
    }

    private void sendHelp(CommandSender sender, String prefix) {
        sender.sendMessage(Component.text("§8§m------§r §6§lRadio Plugin §8§m------"));
        sender.sendMessage(Component.text("§e/radio give <joueur> §7- Donne un talkie"));
        sender.sendMessage(Component.text("§e/radio give <joueur> <freq> §7- Avec frequence"));
        sender.sendMessage(Component.text("§e/radio info §7- Ta frequence active"));
        sender.sendMessage(Component.text("§e/radio reload §7- Recharge la config"));
        sender.sendMessage(Component.text("§7Frequences : " +
                Arrays.stream(Frequency.values())
                        .map(f -> f.getColorCode() + f.name().toLowerCase() + "§7")
                        .collect(Collectors.joining(", "))));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("give", "info", "reload", "help").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return Arrays.stream(Frequency.values())
                    .map(f -> f.name().toLowerCase())
                    .filter(n -> n.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
