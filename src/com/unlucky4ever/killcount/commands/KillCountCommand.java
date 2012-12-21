package com.unlucky4ever.killcount.commands;

import java.sql.ResultSet;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.unlucky4ever.killcount.KillCount;
import com.unlucky4ever.killcount.extras.db.MySQL;

public class KillCountCommand extends BaseCommand {
	
	public KillCount plugin;
	public FileConfiguration users;
	
	public KillCountCommand(KillCount instance) {
		plugin = instance;
		command.add("kc");
		
		commandOnly = false;
		helpDescription = "Basic commands for kill count.";
	}
	
	public void perform() {
		if (this.parameters.size() == 0) {
			permFlag = "killcount.kc";
			
			if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
				users = plugin.getCustomConfig();
				int kills = users.getInt(sender.getName().toLowerCase() + ".kills");
				int deaths = users.getInt(sender.getName().toLowerCase() + ".deaths");
				sender.sendMessage(ChatColor.RED + "You have " + kills + " kills and " + deaths + " deaths.");
			}
			
			if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
				plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
				plugin.mysql.open();
				
				if (plugin.mysql.checkConnection()) {
					try {
						ResultSet result = plugin.mysql.query("SELECT COUNT(*) FROM killcount WHERE username='" + player.getName() + "'");
						result.first();
						int count = result.getInt(1);
						result.close();
						
						if (count == 0) {
							sender.sendMessage(ChatColor.RED + "Your stats aren't in the database!");
						} else {
							result = plugin.mysql.query("SELECT kills,deaths FROM killcount WHERE username='" + player.getName() + "'");
							result.first();
							int kills = result.getInt(1);
							int deaths = result.getInt(2);
							result.close();
							sender.sendMessage(ChatColor.RED + "You have " + kills + " kills and " + deaths + " deaths.");
						}
					} catch (Exception e) {
						if (plugin.config.getBoolean("debug")) {
							e.printStackTrace();
						}
					}
					plugin.mysql.close();
				}
			}
		} else if (parameters.size() == 1) {
			if (parameters.get(0).equalsIgnoreCase("help") || parameters.get(0).equalsIgnoreCase("?")) {
				sender.sendMessage(ChatColor.RED + "---- KillCount Help Menu ----");
				sender.sendMessage(ChatColor.GOLD + "/kc ? " + ChatColor.RESET + "-" + ChatColor.RED + " Shows this menu");
				if (sender.hasPermission("killcount.kc")) {
					sender.sendMessage(ChatColor.GOLD + "/kc " + ChatColor.RESET + "-" + ChatColor.RED + " Look up your kill/death count");
				}
				if (sender.hasPermission("killcount.kc.others")) {
					sender.sendMessage(ChatColor.GOLD + "/kc [playername] " + ChatColor.RESET + "-" + ChatColor.RED + " Look up someone elses kill/death count");
				}
				if (sender.hasPermission("killcount..kc.top")) {
					sender.sendMessage(ChatColor.GOLD + "/kc top " + ChatColor.RESET + "-" + ChatColor.RED + " Look up the top 5 killers");
				}
				if (sender.hasPermission("killcount.kc.reset")) {
					sender.sendMessage(ChatColor.GOLD + "/kc reset " + ChatColor.RESET + "-" + ChatColor.RED + " Reset your stats");
				}
				if (sender.hasPermission("killcount.ratio")) {
					sender.sendMessage(ChatColor.GOLD + "/kdr " + ChatColor.RESET + "-" + ChatColor.RED + " Look up your kill/death ratio");
				}
				if (sender.hasPermission("killcount.ratio.others")) {
					sender.sendMessage(ChatColor.GOLD + "/kdr [playername] " + ChatColor.RESET + "-" + ChatColor.RED + " Look up someone elses kill/death ratio");
				}
				if (sender.hasPermission("killcount.admin.reset")) {
					sender.sendMessage(ChatColor.GOLD + "/kca reset [playername] " + ChatColor.RESET + "-" + ChatColor.RED + " Reset someones kill/death count back to 0");
				}
				if (sender.hasPermission("killcount.admin.empty")) {
					sender.sendMessage(ChatColor.GOLD + "/kca empty " + ChatColor.RESET + "-" + ChatColor.RED + " Empties the database of kill/death counts");
				}
			} else if (parameters.get(0).equalsIgnoreCase("top")) {
				if (sender.hasPermission("killcount.kc.top")) {
					if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
						sender.sendMessage(ChatColor.RED + "This command can only be used with MySQL.");
					}
					if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
						plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
						plugin.mysql.open();
						if (plugin.mysql.checkConnection()) {
							try {
								ResultSet result = plugin.mysql.query("SELECT username,kills FROM killcount ORDER BY kills DESC LIMIT 5");
								sender.sendMessage(ChatColor.RED + "---- Top 5 Killers ----");
								int rank = 0;
								while (result.next()) {
									if (result.getInt(2) == 0) {
										continue;
									}
									rank++;
									sender.sendMessage(ChatColor.GOLD + "" + rank + ". " + result.getString(1) + ChatColor.RESET + " (" + ChatColor.GREEN + result.getInt(2) + " kills" + ChatColor.RESET + ")");
								}
								result.close();
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
			} else if (parameters.get(0).equalsIgnoreCase("reset")) {
				if (sender.hasPermission("killcount.kc.reset")) {
					if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
						users = plugin.getCustomConfig();
						users.set(sender.getName().toLowerCase() + ".kills", 0);
						users.set(sender.getName().toLowerCase() + ".deaths", 0);
						plugin.saveCustomConfig();
						sender.sendMessage(ChatColor.RED + "You have reset your kills and deaths back to 0!");
					}
					if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
						plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
						plugin.mysql.open();
						
						if (plugin.mysql.checkConnection()) {
							try {
								ResultSet result = plugin.mysql.query("SELECT COUNT(*) FROM killcount WHERE username='" + sender.getName() + "'");
								result.first();
								int count = result.getInt(1);
								result.close();
								if (count == 0) {
									sender.sendMessage(ChatColor.RED + "You already have 0 kills and deaths!");
								} else {
									plugin.mysql.query("UPDATE killcount SET deaths=0,kills=0 WHERE username='" + sender.getName() + "'");
									sender.sendMessage(ChatColor.RED + "You have reset your kills and deaths back to 0!");
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
			} else {
				if (sender.hasPermission("killcount.kc.others")) {
					if (plugin.config.getString("storage-type").equalsIgnoreCase("file")) {
						users = plugin.getCustomConfig();
						int kills = users.getInt(parameters.get(0).toLowerCase() + ".kills");
						int deaths = users.getInt(parameters.get(0).toLowerCase() + ".deaths");
						sender.sendMessage(ChatColor.RED + parameters.get(0) + " has " + kills + " kills and " + deaths + " deaths.");
					}
					
					if (plugin.config.getString("storage-type").equalsIgnoreCase("mysql")) {
						plugin.mysql = new MySQL(plugin.getLogger(), "[KillCount]", plugin.mysql_host, plugin.mysql_port, plugin.mysql_db, plugin.mysql_user, plugin.mysql_password);
						plugin.mysql.open();
						if (plugin.mysql.checkConnection()) {
							try {
								ResultSet result = plugin.mysql.query("SELECT COUNT(*) FROM killcount where username='" + parameters.get(0) + "'");
								result.first();
								int count = result.getInt(1);
								result.close();
								if (count == 0) {
									player.sendMessage(ChatColor.RED + parameters.get(0) + " isn't in the database!");
								} else {
									result = plugin.mysql.query("SELECT kills,deaths FROM killcount WHERE username='" + parameters.get(0) + "'");
									result.first();
									int kills = result.getInt(1);
									int deaths = result.getInt(2);
									result.close();
									player.sendMessage(ChatColor.RED + parameters.get(0) + " has " + kills + " kills and " + deaths + " deaths.");
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
			}
		} else {
			sender.sendMessage("You put to many arguments!");
		}
	}
}
