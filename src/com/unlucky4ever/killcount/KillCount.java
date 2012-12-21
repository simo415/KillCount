package com.unlucky4ever.killcount;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.unlucky4ever.killcount.commands.*;
import com.unlucky4ever.killcount.extras.*;
import com.unlucky4ever.killcount.extras.db.*;
import com.unlucky4ever.killcount.listeners.PlayerListener;

public class KillCount extends JavaPlugin {
	
	public PluginDescriptionFile description;
	public FileConfiguration config;
	public MySQL mysql;
	public String mysql_host;
	public String mysql_port;
	public String mysql_db;
	public String mysql_user;
	public String mysql_password;
	
	public List<BaseCommand> commands = new ArrayList<BaseCommand>();
	public PlayerListener pl = new PlayerListener(this);
	public HashMap<Player, ArrayList<Block>> hashmap = new HashMap<Player, ArrayList<Block>>();
	
	public FileConfiguration customConfig = null;
	public File customConfigFile = null;
	
	
	@Override
	public void onLoad() {
		this.config = this.getConfig();
	}
	
	public void onEnable() {
		description = this.getDescription();
		
		config.addDefault("debug", false);
		config.addDefault("storage-type", "file");
		config.addDefault("mysql.host", "localhost");
		config.addDefault("mysql.port", "3306");
		config.addDefault("mysql.database", "minecraft");
		config.addDefault("mysql.username", "root");
		config.addDefault("mysql.password", "password");
		
		config.options().copyDefaults(true);
		
		if (config.getString("storage-type").equalsIgnoreCase("file")) {
			this.getCustomConfig();
			this.saveCustomConfig();
		}
		
		this.saveConfig();
		
		try {
			Metrics metrics = new Metrics(this);
			getLogger().info("Enabling Metrics...");
			metrics.start();
			setupDatabase();
			setupCommands();
		} catch (IOException e) {
			if (config.getBoolean("debug")) {
				e.printStackTrace();
			}
		}
		
		getServer().getPluginManager().registerEvents(pl, this);
	}
	
	public void onDisable() {
		mysql = null;
	}
	
    public void reloadCustomConfig() {
    	if (customConfigFile == null) {
    		customConfigFile = new File(getDataFolder(), "users.yml");
    	}
    	customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }
    
    public boolean deleteCustomConfig() {
    	return customConfigFile.delete();
    }
    
    public FileConfiguration getCustomConfig() {
    	if (customConfig == null) {
    		this.reloadCustomConfig();
    	}
    	return customConfig;
    }
    
    public void saveCustomConfig() {
    	if (customConfig == null || customConfigFile == null) {
    		return;
    	}
    	try {
    		getCustomConfig().save(customConfigFile);
    	} catch (IOException ex) {
    		this.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
    	}
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    	List<String> parameters = new ArrayList<String>(Arrays.asList(args));
    	String commandName = cmd.getName();
    	for (BaseCommand command : this.commands) {
    		if (command.getCommands().contains(commandName)) {
    			command.execute(sender, parameters);
    			return true;
    		}
    	}
    	return false;
    }
    
    private void setupDatabase() {
    	
    	if (config.getString("storage-type").equalsIgnoreCase("mysql")) {
    		mysql_host = config.getString("mysql.host");
    		mysql_db = config.getString("mysql.database");
    		mysql_user = config.getString("mysql.username");
    		mysql_password = config.getString("mysql.password");
    		mysql_port = config.getString("mysql.port");
    		
    		mysql = new MySQL(getLogger(), "[KillCount]", mysql_host, mysql_port, mysql_db, mysql_user, mysql_password);
    		
    		getLogger().info("Connecting to MySQL Database...");
    		mysql.open();
    		
    		if (mysql.checkConnection()) {
    			getLogger().info("Successfully connected to database!");
    			
    			if (!mysql.checkTable("killcount")) {
    				getLogger().info("Creating table 'killcount' in database " + config.getString("mysql.database"));
    				mysql.createTable("CREATE TABLE killcount ( id int NOT NULL AUTO_INCREMENT, username VARCHAR(32) NOT NULL, kills int NOT NULL, deaths int NOT NULL, PRIMARY KEY (id) ) ENGINE=MyISAM;");
    			}
    		} else {
    			getLogger().severe("Error connecting to database, shutting down!");
    			this.getPluginLoader().disablePlugin(this);
    		}
    		mysql.close();
    	}
    }
    
    private void setupCommands() {
    	commands.add(new KillCountCommand(this));
    	commands.add(new KillDeathRatioCommand(this));
    	commands.add(new KillCountAdminCommand(this));
    }
}