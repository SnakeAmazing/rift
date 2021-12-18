package me.ufo.rift.commands;

import me.ufo.rift.Rift;
import me.ufo.rift.util.Style;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RiftCommand implements CommandExecutor {

  private final Rift plugin;

  public RiftCommand(final Rift plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(final CommandSender sender, final Command cmd, final String l,
                           final String[] args) {

    if (!sender.isOp() && !sender.hasPermission("rift.command")) {
      return false;
    }

    if (args.length <= 0) {
      return false;
    }

    switch (args[0].toLowerCase()) {
      case "debug":
        final boolean mode = this.plugin.toggleDebug();
        if (sender instanceof Player) {
          sender.sendMessage(mode ? "Debug mode is enabled." : "Debug mode is disabled.");
        }

        this.plugin.info(mode ? "Debug mode is enabled." : "Debug mode is disabled.");
        break;

      case "huball":
        final String out = Style.translate("&cSending all players to a hub...");
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
          player.sendMessage(out);
        }
        this.plugin.sendAllToHubs();
        break;

      case "hubs":
        sender.sendMessage(ChatColor.GREEN.toString() + "Hubs:");
        for (final String hub : this.plugin.getHubs()) {
          sender.sendMessage(ChatColor.YELLOW.toString() + hub);
        }
        break;

      case "test":
        if (args.length != 5) {
          sender.sendMessage("Usage: /rift test <-a:-s> <destination> <action> <message>");
          return false;
        }

        if ("-a".equalsIgnoreCase(args[1])) {
          this.plugin.redis().async(args[2], args[3], args[4]);
        } else if ("-s".equalsIgnoreCase(args[1])) {
          this.plugin.redis().sync(args[2], args[3], args[4]);
        } else {
          sender.sendMessage("Usage: /rift test <-a:-s> <destination> <action> <message>");
          return false;
        }
        break;

      case "ping":
        this.plugin.redis().async("ALL", "PING", this.plugin.response());
        break;

    }

    return false;
  }
}
