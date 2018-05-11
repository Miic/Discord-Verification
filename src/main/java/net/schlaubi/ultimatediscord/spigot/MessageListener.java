package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.GuildController;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class MessageListener extends ListenerAdapter {

    private static HashMap<String, String> users = CommandDiscord.users;

    private static String getUser(String code) {
        for(String key : users.keySet()) {
            String value = users.get(key);
            if(value.equalsIgnoreCase(code)) {
                return key;
            }
        }
        return null;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        FileConfiguration cfg = Main.getConfiguration();
        if(event.isFromType(ChannelType.PRIVATE)){
            String message = event.getMessage().getContentDisplay();
            String[] args = message.split(" ");
            JDA jda = event.getJDA();
            if(args[0].equalsIgnoreCase("!verify")) {
                if (users.containsValue(args[1])) {
                    GuildController guild = new GuildController(Main.jda.getGuilds().get(0));
                    Role role = guild.getGuild().getRoleById(cfg.getString("Roles.defaultrole"));
                    guild.addRolesToMember(guild.getGuild().getMember(event.getAuthor()), role).queue();
                    event.getPrivateChannel().sendMessage(cfg.getString("Messages.success").replace("%discord%", event.getAuthor().getName())).queue();
                    MySQL.createUser(getUser(args[1]), event.getAuthor().getId());
                    users.remove(getUser(args[1]));
                } else {
                    event.getPrivateChannel().sendMessage(cfg.getString("Messages.invalidcode")).queue();
                }
            } else if(args[0].equalsIgnoreCase("!roles")){
                StringBuilder sb = new StringBuilder();
                for(Role r : jda.getGuilds().get(0).getRoles()){
                    sb.append("[R: " + r.getName() + "(" + r.getId() + ")");
                }
                event.getPrivateChannel().sendMessage(sb.toString()).queue();
            }
        }
    }
}
