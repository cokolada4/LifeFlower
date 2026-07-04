package io.github.lifeflower;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LifeFlowerCommand implements BasicCommand {
    private final LifeFlowerPlugin plugin;
    private final LifeFlowerManager manager;

    public LifeFlowerCommand(LifeFlowerPlugin plugin, LifeFlowerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /lifeflower <new|pardon|raidtool|reload> ...", NamedTextColor.YELLOW));
            return;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("reload")) {
            if (!sender.hasPermission("lifeflower.command.reload")) {
                sender.sendMessage(getMessage("no-permission"));
                return;
            }

            if (plugin.reloadPlugin()) {
                sender.sendMessage(getMessage("reload-success"));
            } else {
                sender.sendMessage(getMessage("reload-error"));
            }
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /lifeflower <new|pardon|raidtool> <player>", NamedTextColor.YELLOW));
            return;
        }

        String targetName = args[1];

        if (subCommand.equals("new")) {
            if (!sender.hasPermission("lifeflower.admin")) {
                sender.sendMessage(getMessage("no-permission"));
                return;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
            UUID uuid = target.getUniqueId();

            manager.resetFlower(uuid);
            LifeFlower flower = manager.createFlower(uuid);

            ItemStack item = LifeFlowerUtils.createLifeFlowerItem(plugin, uuid, target.getName());

            if (target.isOnline()) {
                Player onlineTarget = target.getPlayer();
                if (onlineTarget != null) {
                    onlineTarget.getInventory().addItem(item);
                    onlineTarget.sendMessage(Component.text("Your LifeFlower has been reset by an admin!", NamedTextColor.LIGHT_PURPLE));
                }
            }

            sender.sendMessage(Component.text("Reset LifeFlower for " + targetName, NamedTextColor.GREEN));

        } else if (subCommand.equals("pardon")) {
            if (!sender.hasPermission("lifeflower.admin")) {
                sender.sendMessage(getMessage("no-permission"));
                return;
            }

            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            // Logic to unban
            boolean pardoned = Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(target.getName());
            if (pardoned) {
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(target.getName());
                sender.sendMessage(Component.text("Pardoned " + targetName, NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text(targetName + " is not banned.", NamedTextColor.YELLOW));
            }
        } else if (subCommand.equals("raidtool")) {
            if (!sender.hasPermission("lifeflower.command.raidtool")) {
                sender.sendMessage(getMessage("no-permission"));
                return;
            }

            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(getMessage("player-not-found"));
                return;
            }

            ItemStack raidTool = LifeFlowerUtils.createRaidTool(plugin);
            target.getInventory().addItem(raidTool);

            sender.sendMessage(getMessage("raidtool-given").replaceText(config -> config.matchLiteral("%player%").replacement(target.getName())));
            target.sendMessage(Component.text("You have received a LifeFlower Removal Tool!", NamedTextColor.LIGHT_PURPLE));
        }
    }

    private Component getMessage(String key) {
        String msg = plugin.getMessages().getString(key);
        if (msg == null) return Component.text("Missing message: " + key, NamedTextColor.RED);
        return MiniMessage.miniMessage().deserialize(msg);
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender) {
        return sender.hasPermission("lifeflower.use");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();

        if (args.length <= 1) {
            String currentArg = args.length == 0 ? "" : args[0].toLowerCase();
            return List.of("new", "pardon", "raidtool", "reload", "help").stream()
                    .filter(sub -> hasSubPermission(sender, sub))
                    .filter(sub -> sub.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String currentArg = args[1].toLowerCase();
            if (sub.equals("new") || sub.equals("raidtool")) {
                if (hasSubPermission(sender, sub)) {
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(currentArg))
                            .collect(Collectors.toList());
                }
            } else if (sub.equals("pardon")) {
                if (hasSubPermission(sender, sub)) {
                    return Bukkit.getBanList(org.bukkit.BanList.Type.NAME).getEntries().stream()
                            .map(org.bukkit.BanEntry::getTarget)
                            .filter(name -> name != null && name.toLowerCase().startsWith(currentArg))
                            .collect(Collectors.toList());
                }
            }
        }

        return List.of();
    }

    private boolean hasSubPermission(CommandSender sender, String sub) {
        return switch (sub) {
            case "new", "pardon" -> sender.hasPermission("lifeflower.admin");
            case "raidtool" -> sender.hasPermission("lifeflower.command.raidtool");
            case "reload" -> sender.hasPermission("lifeflower.command.reload");
            default -> true;
        };
    }
}
