package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
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
                    	try {
                            String rawmessage = event.getMessage().getContentRaw();
                            String[] rawargs = rawmessage.split(" ");
                    		
                    		final User user = event.getGuild().getMemberById(rawargs[1].replaceAll("<@", "").replaceAll(">", "")).getUser();
                    		final String id = user.getId();
                    		if (MySQL.userExists(id)) {
                    			String uuid = MySQL.getValue(id, "uuid");
                    			event.getChannel().sendMessage(user.getAsMention() + " is linked to the following account: https://namemc.com/profile/" + uuid).queue();
                    			
                    		} else {
                    			
                    		}
                    	} catch (Exception e) {
                    		event.getChannel().sendMessage(cfg.getString("Unknown User Id: " + args[1])).queue();
                    	}
            		}
            	}.runTaskAsynchronously(Main.instance);
            }
        }
    }
}
