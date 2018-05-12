package net.schlaubi.ultimatediscord.util;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.schlaubi.ultimatediscord.spigot.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class MySQL {

    //private static Connection connection;
	private static HikariDataSource hikari;
	
    public static void connect(){
        FileConfiguration cfg = Main.getConfiguration();
        String host = cfg.getString("MySQL.host");
        Integer port = cfg.getInt("MySQL.port");
        String user = cfg.getString("MySQL.user");
        String database = cfg.getString("MySQL.database");
        String password = cfg.getString("MySQL.password");

        HikariConfig config = new HikariConfig();
		config.setDriverClassName("com.mysql.jdbc.Driver");
		config.setMinimumIdle(3);
		config.setMaximumPoolSize(5);
		config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +"?autoReconnect=true&autoReconnectForPools=true&interactiveClient=true&characterEncoding=UTF-8");
		config.setUsername(user);
		config.setPassword(password);
		hikari = new HikariDataSource(config);
		
		Bukkit.getConsoleSender().sendMessage("§a§l[UltimateDiscord]MySQL connection success");
    }

    public static void connect(Configuration config){
        Configuration cfg = config;
        String host = cfg.getString("MySQL.host");
        Integer port = cfg.getInt("MySQL.port");
        String user = cfg.getString("MySQL.user");
        String database = cfg.getString("MySQL.database");
        String password = cfg.getString("MySQL.password");

        HikariConfig config1 = new HikariConfig();
		config1.setDriverClassName("com.mysql.jdbc.Driver");
		config1.setMinimumIdle(3);
		config1.setMaximumPoolSize(5);
		config1.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database +"?autoReconnect=true&autoReconnectForPools=true&interactiveClient=true&characterEncoding=UTF-8");
		config1.setUsername(user);
		config1.setPassword(password);
		hikari = new HikariDataSource(config1);
		
		Bukkit.getConsoleSender().sendMessage("§a§l[UltimateDiscord]MySQL connection success");
    }

    private static boolean isConnected(){
        return hikari != null && !hikari.isClosed();
    }

    public static void disconnect(){
        if(!isConnected()){
            hikari.close();
        }
    }

    public static void createDatabase()
    {
        try
        {
            Connection connection = hikari.getConnection();
            	
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ultimatediscord( `id` BIGINT NOT NULL AUTO_INCREMENT , `uuid` varchar(36) NOT NULL , `discordid` TEXT NOT NULL , PRIMARY KEY (`id`)) ENGINE = InnoDB;");
            ps.execute();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean userExists(Player player)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid =?");
            ps.setString(1,player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean userExists(ProxiedPlayer player)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid = ?");
            ps.setString(1,player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean userExists(String id)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE discordid =?");
            if (!Bukkit.getServer().getOnlineMode()) {
                ps.setString(1, id);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean createUser(Player player, String identity)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            
            if (userExists(player)) {
            	return false;
            }
            
            if (userExists(identity)) {
            	return false;
            }
            
            PreparedStatement ps = connection.prepareStatement("INSERT INTO ultimatediscord(`uuid`,`discordid`) VALUES (?, ?)");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, identity);
            ps.execute();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    
    public static boolean createUser(ProxiedPlayer player, String identity)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            
            if (userExists(player)) {
            	return false;
            }
            
            if (userExists(identity)) {
            	return false;
            }
            
            PreparedStatement ps = connection.prepareStatement("INSERT INTO ultimatediscord(`uuid`,`discordid`) VALUES (?, ?)");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, identity);
            ps.execute();
            return true;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return false;
        }
    }



    public static String getValue(Player player, String type)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid = ?");
            ps.setString(1,player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(type);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getValue(ProxiedPlayer player, String type)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid = ?");
            ps.setString(1,player.getUniqueId().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(type);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getValue(String identity, String type)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE discordid = ?");
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(type);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    public static void deleteUser(Player player)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM ultimatediscord WHERE uuid=?");
            ps.setString(1, player.getUniqueId().toString());
            ps.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void deleteUser(ProxiedPlayer player)
    {
        try
        {
        	Connection connection = hikari.getConnection();
            PreparedStatement ps = connection.prepareStatement("DELETE FROM ultimatediscord WHERE uuid=?");
            ps.setString(1,player.getUniqueId().toString());
            ps.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }




}
