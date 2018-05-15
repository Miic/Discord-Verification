package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import net.schlaubi.ultimatediscord.util.MySQL;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.cache.Cache;

import java.util.UUID;

public class MessageListener extends ListenerAdapter {

    private static Cache<String, UUID> users = CommandDiscord.users;

    private static String getUser(String code) {
    	UUID uuid = users.getIfPresent(code); 
    	if (uuid != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player.hasPlayedBefore()) {
            	return player.getName();		
            }
            return uuid.toString();
    	}
        return null;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        FileConfiguration cfg = Main.getConfiguration();
        if(!event.isFromType(ChannelType.PRIVATE)){
            String message = event.getMessage().getContentDisplay();
            String[] args = message.split(" ");
            JDA jda = event.getJDA();
            if(args[0].equalsIgnoreCase("!verify")) {
                if (users.getIfPresent(args[1]) != null) {
                	final UUID user = users.getIfPresent(args[1]);
                    GuildController guild = new GuildController(Main.jda.getGuilds().get(0));
                    Role role = guild.getGuild().getRoleById(cfg.getString("Roles.defaultrole"));
                    guild.addRolesToMember(guild.getGuild().getMember(event.getAuthor()), role).queue();
                    new BukkitRunnable() {
                    	public void run() {
                    		if (MySQL.createUser(Bukkit.getPlayer(user), event.getAuthor().getId())) {
                                event.getChannel().sendMessage(cfg.getString("Messages.success").replaceAll("%discord%", event.getAuthor().getAsMention())
                                		.replaceAll("%minecraft%", getUser(args[1]))).queue();
                    		} else {
                                event.getChannel().sendMessage("Account linking failed. That Discord account or Minecraft account may already be associated with another account.").queue();
                    		}
                    		users.invalidate(args[1]);
                    	}
                    }.runTaskAsynchronously(Main.instance);
                    //event.getMessage().delete().queue();
                } else {
                    event.getChannel().sendMessage(cfg.getString("Messages.invalidcode")).queue();
                    //event.getMessage().delete().queue();
                }
            } else if(args[0].equalsIgnoreCase("!roles")){
            	if (!event.getGuild().getMember(event.getAuthor()).hasPermission(Permission.MANAGE_SERVER) && !event.getGuild().getOwner().getUser().equals(event.getAuthor())) {
            		return;
            	}
            	
                StringBuilder sb = new StringBuilder();
                for(Role r : jda.getGuilds().get(0).getRoles()){
                    sb.append("[R: " + r.getName() + "(" + r.getId() + ")");
                }
                event.getChannel().sendMessage(sb.toString()).queue();
                event.getMessage().delete().queue();
            } else if (args[0].equalsIgnoreCase("!whois")) {
            	new BukkitRunnable() {
            		public void run() {
                        String rawmessage = event.getMessage().getContentRaw();
                        String[] rawargs = rawmessage.split(" ");
                    	try {                    		
                            if (rawargs[1].startsWith("<@")) {
                            	String id = rawargs[1].replaceAll("<@", "").replaceAll(">", "").replaceAll("!", "");
                            	//Bukkit.getLogger().log(Level.INFO, "Ultimate Discord Processing Discord ID: " + id);
	                    		User user = event.getGuild().getMemberById(id).getUser();
	                    		id = user.getId();
	                    		if (MySQL.userExists(id)) {
	                    			String uuid = MySQL.getValue(id, "uuid");
	                    			event.getChannel().sendMessage(user.getAsMention() + " is linked to the following account: https://namemc.com/profile/" + uuid).queue();
	                    			
	                    		} else {
	                    			event.getChannel().sendMessage(user.getAsMention() + " is not currently linked to a Minecraft Account.").queue();;
	                    		}
                            } else {
                            	event.getChannel().sendMessage("You did not mention a discord user.").queue();
                            }
                    	} catch (Exception e) {
                    		event.getChannel().sendMessage("Unknown User Id: " + rawargs[1]).queue();
                    	}
            		}
            	}.runTaskAsynchronously(Main.instance);
            }
        }
    }
    
    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
    	final String id = event.getMember().getUser().getId();
    	new BukkitRunnable() {
    		public void run() {
    			if (MySQL.userExists(id)) {
    				MySQL.deleteUser(id);
    			}
    		}
    	}.runTaskAsynchronously(Main.instance);
    }
}
