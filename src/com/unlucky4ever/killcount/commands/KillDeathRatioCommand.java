package com.unlucky4ever.killcount.commands;

import java.sql.ResultSet;
import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.unlucky4ever.killcount.KillCount;
import com.unlucky4ever.killcount.extras.db.MySQL;

public class KillDeathRatioCommand extends BaseCommand {
	
	public KillCount plugin;
	public FileConfiguration users;
	
	public double ratio;
	
	public KillDeathRatioCommand(KillCount instance) {
		plugin = instance;
		command.add("kdr");
		commandOnly = false;
		helpDescription = "Basic commands for kill/death ratio.";
	}
	
	public void perform() {
		if (parameters.size() == 0) {
			permFlag = "killcount.ratio";
			
			if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
				users = plugin.getCustomConfig();
				int kills = users.getInt(sender.getName().toLowerCase() + ".kills");
				int deaths = users.getInt(sender.getName().toLowerCase() + ".deaths");
				
				if (deaths == 0) {
					ratio = kills;
				} else {
					ratio = (double) kills / deaths;
				}
				
				sender.sendMessage(ChatColor.RED + "Your kill/death ratio is: " + roundDouble(ratio) + ".");
			}
			
			if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
				if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
					plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
					plugin.mysql.open();
					if (plugin.mysql.checkConnection()) {
						try {
							ResultSet result = plugin.mysql.query("SELECT COUNT(*) FROM killcount WHERE username='" + player.getName() + "'");
							result.first();
							int count = result.getInt(1);
							if (count == 0) {
								sender.sendMessage(ChatColor.RED + "Your stats aren't in the database!");
							} else {
								result = plugin.mysql.query("SELECT kills,deaths FROM killcount WHERE username='" + player.getName() + "'");
								result.first();
								int kills = result.getInt(1);
								int deaths = result.getInt(2);
								if (deaths == 0) {
									ratio = kills;
								} else {
									ratio = (double) kills / deaths;
								}
								sender.sendMessage(ChatColor.RED + "Your kill/death ratio is: " + roundDouble(ratio) + ".");
							}
						} catch (Exception e) {
							if (plugin.config.getBoolean("debug")) {
								e.printStackTrace();
							}
						}
						plugin.mysql.close();
					}
				}
			}
		} else if (parameters.size() == 1) {
			if (sender.hasPermission("killcount.ratio.others")) {
				if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
					users = plugin.getCustomConfig();
					int kills = users.getInt(parameters.get(0).toLowerCase() + ".kills");
					int deaths = users.getInt(parameters.get(0).toLowerCase() + ".deaths");
					if (deaths == 0) {
						ratio = kills;
					} else {
						ratio = (double) kills / deaths;
					}
					sender.sendMessage(ChatColor.RED + parameters.get(0) + " has a kill/death ratio of: " + roundDouble(ratio) + ".");
				}
				if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
					plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
					plugin.mysql.open();
					if (plugin.mysql.checkConnection()) {
						try {
							ResultSet result = plugin.mysql.query("SELECT COUNT(*) FROM killcount WHERE username='" + parameters.get(0) + "'");
							result.first();
							int count = result.getInt(1);
							result.close();
							if (count == 0) {
								player.sendMessage(ChatColor.RED + parameters.get(0) + " has a kill/death ratio of: " + roundDouble(ratio) + ".");
							} else {
								result = plugin.mysql.query("SELECT kills,deaths FROM killcount WHERE username='" + parameters.get(0) + "'");
								result.first();
								int kills = result.getInt(1);
								int deaths = result.getInt(2);
								result.close();
								if (deaths == 0) {
									ratio = kills;
								} else {
									ratio = (double) kills / deaths;
								}
								player.sendMessage(ChatColor.RED + parameters.get(0) + " has a kill/death ratio of: " + roundDouble(ratio) + ".");
							}
						} catch (Exception e) {
							if (plugin.config.getBoolean("debug")) {
								e.printStackTrace();
							}
						}
						plugin.mysql.close();
					}
				}
			} else {
				sender.sendMessage("You lack the permissions to do this command.");
			}
		} else {
			sender.sendMessage("You put to many arguments!");
		}
	}
	
    private double roundDouble(double d) {
    	DecimalFormat format = new DecimalFormat("#.##");
    	return Double.valueOf(format.format(d));
    }
}