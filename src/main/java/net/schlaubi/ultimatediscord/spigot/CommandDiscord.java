package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.managers.GuildController;
import net.schlaubi.ultimatediscord.util.CodeHandling;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CommandDiscord implements CommandExecutor, TabExecutor {	
	
	/**
	 * 
	 */
    @Override
    public boolean onCommand(CommandSender sender, Command name, String lable, String[] args) {
        if(sender instanceof Player){
            FileConfiguration cfg = Main.getConfiguration();
            Player player = (Player) sender;
            if(args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("discord.reload")) {
                        player.sendMessage("§7[§Discord§7]§a Settings reloaded");
                        try {
                            cfg.save(new File("plugins/TeamspeakVerifyer", "config.yml"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (args[0].equalsIgnoreCase("verify")) {
                	new BukkitRunnable() {
                		public void run() {
                            if (MySQL.userExists(player)) {
                                player.sendMessage(cfg.getString("Messages.verified").replace("&", "§"));
                            } else {
                            	final String rand = CodeHandling.generateCodeForPlayer(player.getUniqueId(), CodeHandling.CODE_LENGTH);
                                player.sendMessage(cfg.getString("Messages.verify").replace("&", "§").replaceAll("%code%", rand));
                                Bukkit.getScheduler().runTaskLater(Main.instance, new Runnable() {
                                    @Override
                                    public void run() {
                                       CodeHandling.redeemCode(player.getUniqueId());
                                    }
                                }, 60 * 1000);
                            }
                		}
                	}.runTaskAsynchronously(Main.instance);
                } else if (args[0].equalsIgnoreCase("unlink")) {
                	new BukkitRunnable() {
                		public void run() {
                            if (!MySQL.userExists(player)) {
                                player.sendMessage(cfg.getString("Messages.notverified").replace("&", "§"));
                            } else {
                                GuildController guild = new GuildController(Main.jda.getGuilds().get(0));
                                Member member = guild.getGuild().getMember(Main.jda.getUserById(MySQL.getValue(player, "discordid")));
                                guild.removeRolesFromMember(member, guild.getGuild().getRoleById(cfg.getString("Roles.defaultrole"))).queue();
                                MySQL.deleteUser(player);
                                player.sendMessage(cfg.getString("Messages.unlinked").replace("&", "§"));
                            }
                		}
                	}.runTaskAsynchronously(Main.instance);
                }
            } else {
                player.sendMessage(cfg.getString("Messages.help").replace("%nl", "\n").replace("&", "§"));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("§4§l[UltimateDiscord] You must be a player to run this command");
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command name, String lable, String[] args) {
        String[] subcommands = {"reload", "verify", "unlink", "update"};
        if(args.length > 1 || args.length == 0){
            return  Arrays.asList(subcommands);
        }
        if(args.length > 0){
            List<String> matches = new ArrayList<>();
            for (String subcommand : subcommands){
                if(subcommand.startsWith(args[0]))
                    matches.add(subcommand);
            }
            return matches;
        }
        return null;
    }
}
