package com.unlucky4ever.killcount.listeners;

import java.sql.ResultSet;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.unlucky4ever.killcount.KillCount;
import com.unlucky4ever.killcount.extras.db.MySQL;

public class PlayerListener implements Listener {
	
	public KillCount plugin;
	public FileConfiguration users;

	public PlayerListener(KillCount killcount) {
		plugin = killcount;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
			users = plugin.getCustomConfig();
			
			String player = event.getPlayer().getName().toLowerCase();
			int kills = users.getInt(player + ".kills");
			int deaths = users.getInt(player + ".deaths");
			
			if (kills == 0) {
				users.set(player + ".kills", 0);
			}
			
			if (deaths == 0) {
				users.set(player + ".deaths", 0);
			}
			
			plugin.saveCustomConfig();
		}
	}
	
	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event) {
		
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		
		if (!(event.getEntity().getKiller() instanceof Player)) {
			return;
		}
		
		Player killer = event.getEntity().getKiller();
		Player killed = (Player) event.getEntity();
		
		if (killer.hasPermission("killcount.kill")) {
			logKill(killer.getName().toLowerCase());
			if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
				users = plugin.getCustomConfig();
				int kills = users.getInt(killer.getName().toLowerCase() + ".kills");
				killer.sendMessage(ChatColor.RED + "You now have " + kills + " kills!");
			}
			if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
				plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
				plugin.mysql.open();
				if (plugin.mysql.checkConnection()) {
					try {
						ResultSet kill = plugin.mysql.query("SELECT kills FROM killcount WHERE username='" + killer.getName() + "'");
						kill.first();
						int kills = kill.getInt(1);
						kill.close();
						killer.sendMessage(ChatColor.RED + "You now have " + kills + " kills!");
					} catch (Exception e) {
						if (plugin.config.getBoolean("debug")) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		if (killed.hasPermission("killcount.death")) {
			logDeath(killed.getName().toLowerCase());
			if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
				users = plugin.getCustomConfig();
				int deaths = users.getInt(killed.getName().toLowerCase() + ".deaths");
				killed.sendMessage(ChatColor.RED + "You now have " + deaths + " deaths!");
			}
			if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
				plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
				plugin.mysql.open();
				if (plugin.mysql.checkConnection()) {
					try {
						ResultSet death = plugin.mysql.query("SELECT deaths FROM killcount WHERE username='" + killed.getName() + "'");
						death.first();
						int deaths = death.getInt(1);
						death.close();
						killed.sendMessage(ChatColor.RED + "You now have " + deaths + " deaths!");
					} catch (Exception e) {
						if (plugin.config.getBoolean("debug")) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public void logKill(String username) {
		
		if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
			FileConfiguration users = plugin.getCustomConfig();
			
			int kills = users.getInt(username.toLowerCase() + ".kills");
			kills++;
			users.set(username.toLowerCase() + ".kills", kills);
			
			plugin.saveCustomConfig();
		}
		
		if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
			
			plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
			
			plugin.mysql.open();
			
			if (plugin.mysql.checkConnection()) {
				try {
					
					ResultSet kill = plugin.mysql.query("SELECT COUNT(*) FROM killcount WHERE username='" + username + "'");
					kill.first();
					int count = kill.getInt(1);
					
					if (count == 0) {
						if (plugin.config.getBoolean("debug")) {
							plugin.getLogger().info("Inserted " + username);
						}
						plugin.mysql.query("INSERT INTO killcount (username, kills, deaths) VALUES ('" + username + "', 0, 0)");
						kill.close();
					}
					
					kill = plugin.mysql.query("SELECT kills FROM killcount WHERE username='" + username + "'");
					kill.first();
					int kills = kill.getInt(1);
					kill.close();
					kills++;
					
					if (plugin.config.getBoolean("debug")) {
						plugin.getLogger().info("Added a kill to " + username);
					}
					
					plugin.mysql.query("UPDATE killcount SET kills='" + kills + "' WHERE username='" + username + "'");
				
				} catch (Exception e) {
					if (plugin.config.getBoolean("debug")) {
						e.printStackTrace();
					}
				}
				
				plugin.mysql.close();
				
			}
		}
	}
	
	public void logDeath(String username) {
		
		if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
			FileConfiguration users = plugin.getCustomConfig();
			
			int deaths = users.getInt(username.toLowerCase() + ".deaths");
			deaths++;
			users.set(username.toLowerCase() + ".deaths", deaths);
			
			plugin.saveCustomConfig();
		}
		
		if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
			plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
			plugin.mysql.open();
			
			if (plugin.mysql.checkConnection()) {
				try {
					ResultSet death = plugin.mysql.query("SELECT COUNT(*) FROM killcount WHERE username='" + username + "'");
					death.first();
					int count = death.getInt(1);
					
					if (count == 0) {
						if (plugin.config.getBoolean("debug")) {
							plugin.getLogger().info("Inserted " + username);
						}
						
						plugin.mysql.query("INSERT INTO killcount (username, kills, deaths) VALUES ('" + username + "', 0, 0)");
						death.close();
					}
					
					death = plugin.mysql.query("SELECT deaths FROM killcount WHERE username='" + username + "'");
					death.first();
					int deaths = death.getInt(1);
					death.close();
					deaths++;
					
					if (plugin.config.getBoolean("debug")) {
						plugin.getLogger().info("Added a death to " + username);
					}
					
					plugin.mysql.query("UPDATE killcount SET deaths='" + deaths + "' WHERE username='" + username + "'");
					
				} catch (Exception e) {
					if (plugin.getConfig().getBoolean("debug")) {
						e.printStackTrace();
					}
				}
				
				plugin.mysql.close();
				
			}
		}
	}
}
