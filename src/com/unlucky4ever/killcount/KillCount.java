package com.unlucky4ever.killcount;

import java.io.File;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.unlucky4ever.killcount.extras.sql.MySQL;
import com.unlucky4ever.killcount.extras.sql.SQLite;
import com.unlucky4ever.killcount.listeners.PlayerListener;
import com.unlucky4ever.killcount.extras.Metrics;

public class KillCount extends JavaPlugin {
	
	public static MySQL db;
	public static SQLite sqlite;
	public static String dbhost;
	public static String dbport;
	public static String database;
	public static String dbuser;
	public static String dbpass;
	public static double ratio;
	
	public PlayerListener pl = new PlayerListener(this);
	public HashMap<Player, ArrayList<Block>> hashmap = new HashMap<Player, ArrayList<Block>>();
	
	public void onEnable() {
		File file = new File(this.getDataFolder() + File.separator + "config.yml");
		try {
			if (file.exists() && this.getConfig().getBoolean("debug")) {
				this.getLogger().info("Debug mode enabled! Prepare for a lot of spam!");
			}
		    Metrics metrics = new Metrics(this);
		    getLogger().info("Enabling Metrics...");
		    metrics.start();
			if (!file.exists()) {
				this.getLogger().info("Generating first time config.yml...");
				this.getConfig().addDefault("debug", false);
				this.getConfig().addDefault("storage-type", "sqlite");
				this.getConfig().addDefault("mysql.host", "localhost");
				this.getConfig().addDefault("mysql.port", "3306");
				this.getConfig().addDefault("mysql.database", "minecraft");
				this.getConfig().addDefault("mysql.username", "root");
				this.getConfig().addDefault("mysql.password", "password");
				this.getConfig().options().copyDefaults(true);
				this.saveConfig();
			}
			this.getServer().getPluginManager().registerEvents(this.pl, this);
			if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql") || this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
				setupDatabase();
			}
		} catch (Exception e) {
			if (file.exists() && this.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void onDisable() {
		db = null;
		sqlite = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("kc")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be used by players.");
			} else {
				Player player = (Player) sender;
				if (args.length == 0) {
					if (player.hasPermission("killcount.kc")) {
						if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
					    	db = new MySQL(this.getLogger(), "[KillCount]", dbhost, dbport, database, dbuser, dbpass);
					    	db.open();
					    	if (db.checkConnection()) {
					    		try {
									ResultSet result = db.query("SELECT COUNT(*) FROM killcount WHERE username='" + player.getName() + "'");
									result.first();
									int count = result.getInt(1);
									result.close();
									if (count == 0) {
										player.sendMessage(ChatColor.RED + "Your stats aren't in the database!");
									} else {
										result = db.query("SELECT kills,deaths FROM killcount WHERE username='" + player.getName() + "'");
										result.first();
										int kills = result.getInt(1);
										int deaths = result.getInt(2);
										result.close();
										player.sendMessage(ChatColor.RED + "You have " + kills + " kills and " + deaths + " deaths.");
									}
					    		} catch (Exception e) {
					    			if (this.getConfig().getBoolean("debug")) {
					    				e.printStackTrace();
					    			}
					    		}
						    	db.close();
					    	}
						}
						if (this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
							sqlite = new SQLite(this.getLogger(), "[KillCount]", "users", getDataFolder()+File.separator);
					    	sqlite.open();
					    	if (sqlite.checkConnection()) {
					    		try {
									ResultSet result = sqlite.query("SELECT COUNT(*) FROM killcount WHERE username='" + player.getName() + "'");
									int count = result.getInt(1);
									result.close();
									if (count == 0) {
										player.sendMessage(ChatColor.RED + "Your stats aren't in the database!");
									} else {
										result = sqlite.query("SELECT kills,deaths FROM killcount WHERE username='" + player.getName() + "'");
										int kills = result.getInt(1);
										int deaths = result.getInt(2);
										result.close();
										player.sendMessage(ChatColor.RED + "You have " + kills + " kills and " + deaths + " deaths.");
									}
					    		} catch (Exception e) {
					    			if (this.getConfig().getBoolean("debug")) {
					    				e.printStackTrace();
					    			}
					    		}
					    		sqlite.close();
					    	}
						}
					} else {
						player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
				} else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("top")) {
						if (player.hasPermission("killcount.top")) {
							if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
								db = new MySQL(this.getLogger(), "[KillCount]", dbhost, dbport, database, dbuser, dbpass);
								db.open();
								if (db.checkConnection()) {
									try {
										ResultSet result = db.query("SELECT username,kills FROM killcount ORDER BY kills DESC LIMIT 5");
										player.sendMessage(ChatColor.RED + "---- Top 5 Killers ----");
										int rank = 0;
										while (result.next()) {
											if (result.getInt(2) == 0) {
												continue;
											}
											rank++;
											player.sendMessage(ChatColor.GOLD + "" + rank + ". " + result.getString(1) + ChatColor.RESET + " (" + ChatColor.GREEN + result.getInt(2) + " kills" + ChatColor.RESET + ")");
										}
										result.close();
									} catch (Exception e) {
										if (this.getConfig().getBoolean("debug")) {
											e.printStackTrace();
										}
									}
									db.close();
								}
							}
							if (this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
								sqlite = new SQLite(this.getLogger(), "[KillCount]", "users", getDataFolder()+File.separator);
								sqlite.open();
								if (sqlite.checkConnection()) {
									try {
										ResultSet result = sqlite.query("SELECT username,kills FROM killcount ORDER BY kills DESC LIMIT 5");
										player.sendMessage(ChatColor.RED + "---- Top 5 Killers ----");
										int rank = 0;
										while (result.next()) {
											if (result.getInt(2) == 0) {
												continue;
											}
											rank++;
											player.sendMessage(ChatColor.GOLD + "" + rank + ". " + result.getString(1) + ChatColor.RESET + " (" + ChatColor.GREEN + result.getInt(2) + " kills" + ChatColor.RESET + ")");
										}
										result.close();
									} catch (Exception e) {
										if (this.getConfig().getBoolean("debug")) {
											e.printStackTrace();
										}
									}
									sqlite.close();
								}
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
						}
					} else if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
						player.sendMessage(ChatColor.RED + "---- KillCount Help Menu ----");
						player.sendMessage(ChatColor.GOLD + "/kc ? " + ChatColor.RESET + "-" + ChatColor.RED + " Shows this menu");
						if (player.hasPermission("killcount.kc")) {
							player.sendMessage(ChatColor.GOLD + "/kc " + ChatColor.RESET + "-" + ChatColor.RED + " Look up your kill/death count");
						}
						if (player.hasPermission("killcount.kc.others")) {
							player.sendMessage(ChatColor.GOLD + "/kc [playername] " + ChatColor.RESET + "-" + ChatColor.RED + " Look up someone elses kill/death count");
						}
						if (player.hasPermission("killcount.top")) {
							player.sendMessage(ChatColor.GOLD + "/kc top " + ChatColor.RESET + "-" + ChatColor.RED + " Look up the top 5 killers");
						}
						if (player.hasPermission("killcount.ratio")) {
							player.sendMessage(ChatColor.GOLD + "/kdr " + ChatColor.RESET + "-" + ChatColor.RED + " Look up your kill/death ratio");
						}
						if (player.hasPermission("killcount.ratio.others")) {
							player.sendMessage(ChatColor.GOLD + "/kdr [playername] " + ChatColor.RESET + "-" + ChatColor.RED + " Look up someone elses kill/death ratio");
						}
						if (player.hasPermission("killcount.admin.reset")) {
							player.sendMessage(ChatColor.GOLD + "/kca reset [playername] " + ChatColor.RESET + "-" + ChatColor.RED + " Reset someones kill/death count back to 0");
						}
						if (player.hasPermission("killcount.admin.empty")) {
							player.sendMessage(ChatColor.GOLD + "/kca empty " + ChatColor.RESET + "-" + ChatColor.RED + " Empties the database of kill/death counts");
						}
					} else {
						if (player.hasPermission("killcount.kc.others")) {
							if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
								db = new MySQL(this.getLogger(), "[KillCount]", dbhost, dbport, database, dbuser, dbpass);
								db.open();
								if (db.checkConnection()) {
									try {
										ResultSet result = db.query("SELECT COUNT(*) FROM killcount where username='" + args[0] + "'");
										result.first();
										int count = result.getInt(1);
										result.close();
										if (count == 0) {
											player.sendMessage(ChatColor.RED + args[0] + " isn't in the database!");
										} else {
											result = db.query("SELECT kills,deaths FROM killcount WHERE username='" + args[0] + "'");
											result.first();
											int kills = result.getInt(1);
											int deaths = result.getInt(2);
											result.close();
											player.sendMessage(ChatColor.RED + args[0] + " has " + kills + " kills and " + deaths + " deaths.");
										}
									} catch (Exception e) {
										if (this.getConfig().getBoolean("debug")) {
											e.printStackTrace();
										}
									}
									db.close();
								}
							}
							if (this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
								sqlite = new SQLite(this.getLogger(), "[KillCount]", "users", getDataFolder()+File.separator);
								sqlite.open();
								if (sqlite.checkConnection()) {
									try {
										ResultSet result = sqlite.query("SELECT COUNT(*) FROM killcount where username='" + args[0] + "'");
										int count = result.getInt(1);
										result.close();
										if (count == 0) {
											player.sendMessage(ChatColor.RED + args[0] + " isn't in the database!");
										} else {
											result = sqlite.query("SELECT kills,deaths FROM killcount WHERE username='" + args[0] + "'");
											int kills = result.getInt(1);
											int deaths = result.getInt(2);
											result.close();
											player.sendMessage(ChatColor.RED + args[0] + " has " + kills + " kills and " + deaths + " deaths.");
										}
									} catch (Exception e) {
										if (this.getConfig().getBoolean("debug")) {
											e.printStackTrace();
										}
									}
									sqlite.close();
								}
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "Too many arguments!");
				}
			}
		}
		if (commandLabel.equalsIgnoreCase("kdr")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be used by players.");
			} else {
				Player player = (Player) sender;
				if (args.length == 0) {
					if (player.hasPermission("killcount.ratio")) {
						if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
							db = new MySQL(this.getLogger(), "[KillCount]", dbhost, dbport, database, dbuser, dbpass);
							db.open();
							if (db.checkConnection()) {
								try {
									ResultSet result = db.query("SELECT COUNT(*) FROM killcount WHERE username='" + player.getName() + "'");
									result.first();
									int count = result.getInt(1);
									if (count == 0) {
										player.sendMessage(ChatColor.RED + "Your stats aren't in the database!");
									} else {
										result = db.query("SELECT kills,deaths FROM killcount WHERE username='" + player.getName() + "'");
										result.first();
										int kills = result.getInt(1);
										int deaths = result.getInt(2);
										if (deaths == 0) {
											ratio = kills;
										} else {
											ratio = (double) kills / deaths;
										}
										player.sendMessage(ChatColor.RED + "Your kill/death ratio is: " + roundDouble(ratio) + ".");
									}
								} catch (Exception e) {
									if (this.getConfig().getBoolean("debug")) {
										e.printStackTrace();
									}
								}
								db.close();
							}
						}
						if (this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
							sqlite = new SQLite(this.getLogger(), "[KillCount]", "users", getDataFolder()+File.separator);
							sqlite.open();
							if (sqlite.checkConnection()) {
								try {
									ResultSet result = sqlite.query("SELECT COUNT(*) FROM killcount WHERE username='" + player.getName() + "'");
									int count = result.getInt(1);
									result.close();
									if (count == 0) {
										player.sendMessage(ChatColor.RED + "Your stats aren't in the database!");
									} else {
										result = sqlite.query("SELECT kills,deaths FROM killcount WHERE username='" + player.getName() + "'");
										int kills = result.getInt(1);
										int deaths = result.getInt(2);
										result.close();
										if (deaths == 0) {
											ratio = kills;
										} else {
											ratio = (double) kills / deaths;
										}
										player.sendMessage(ChatColor.RED + "Your kill/death ratio is: " + roundDouble(ratio) + ".");
									}
								} catch (Exception e) {
									if (this.getConfig().getBoolean("debug")) {
										e.printStackTrace();
									}
								}
								sqlite.close();
							}
						}
					} else {
						player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
				} else if (args.length == 1) {
					if (player.hasPermission("killcount.ratio.others")) {
						if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
							db = new MySQL(this.getLogger(), "[KillCount]", dbhost, dbport, database, dbuser, dbpass);
							db.open();
							if (db.checkConnection()) {
								try {
									ResultSet result = db.query("SELECT COUNT(*) FROM killcount WHERE username='" + args[0] + "'");
									result.first();
									int count = result.getInt(1);
									result.close();
									if (count == 0) {
										player.sendMessage(ChatColor.RED + args[0] + " isn't in the database!");
									} else {
										result = db.query("SELECT kills,deaths FROM killcount WHERE username='" + args[0] + "'");
										result.first();
										int kills = result.getInt(1);
										int deaths = result.getInt(2);
										result.close();
										if (deaths == 0) {
											ratio = kills;
										} else {
											ratio = (double) kills / deaths;
										}
										player.sendMessage(ChatColor.RED + args[0] + " has a kill/death ratio of: " + roundDouble(ratio) + ".");
										}
								} catch (Exception e) {
									if (this.getConfig().getBoolean("debug")) {
										e.printStackTrace();
									}
								}
								db.close();
							}
						}
						if (this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
							sqlite = new SQLite(this.getLogger(), "[KillCount]", "users", getDataFolder()+File.separator);
							sqlite.open();
							if (sqlite.checkConnection()) {
								try {
									ResultSet result = sqlite.query("SELECT COUNT(*) FROM killcount WHERE username='" + args[0] + "'");
									int count = result.getInt(1);
									result.close();
									if (count == 0) {
										player.sendMessage(ChatColor.RED + args[0] + " isn't in the database!");
									} else {
										result = sqlite.query("SELECT kills,deaths FROM killcount WHERE username='" + args[0] + "'");
										int kills = result.getInt(1);
										int deaths = result.getInt(2);
										result.close();
										if (deaths == 0) {
											ratio = kills;
										} else {
											ratio = (double) kills / deaths;
										}
										player.sendMessage(ChatColor.RED + args[0] + " has a kill/death ratio of: " + roundDouble(ratio) + ".");
										}
								} catch (Exception e) {
									if (this.getConfig().getBoolean("debug")) {
										e.printStackTrace();
									}
								}
								sqlite.close();
							}
						}
					} else {
						player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
					}
				}
			}
		}
		if (commandLabel.equalsIgnoreCase("kca")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be used by players.");
			} else {
				Player player = (Player) sender;
				if (args.length == 0) {
					player.sendMessage(ChatColor.RED + "Not enough arguments!");
				}
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("empty")) {
						if (player.hasPermission("killcount.admin.empty")) {
							hashmap.put(player.getPlayer(), null);
							player.sendMessage(ChatColor.RED + "Are you sure you wish to empty the database?");
							player.sendMessage(ChatColor.RED + "Type " + ChatColor.GOLD + "/kca confirm" + ChatColor.RED + " to confirm.");
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
						}
					}
					if (args[0].equalsIgnoreCase("reset")) {
						if (player.hasPermission("killcount.admin.reset")) {
							player.sendMessage(ChatColor.RED + "Correct usage is " + ChatColor.GOLD + "/kca reset [playername]");
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
						}
					}
					if (args[0].equalsIgnoreCase("confirm")) {
						if (player.hasPermission("killcount.admin.empty")) {
							if (hashmap.containsKey(player.getPlayer())) {
								if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
									db = new MySQL(this.getLogger(), "[KillCount]", dbhost, dbport, database, dbuser, dbpass);
									db.open();
									if (db.checkConnection()) {
										try {
											db.wipeTable("killcount");
											player.sendMessage(ChatColor.RED + "Database emptied!");
											hashmap.remove(player.getPlayer());
										} catch (Exception e) {
											if (this.getConfig().getBoolean("debug")) {
												e.printStackTrace();
											}
										}
										db.close();
									}
								}
								if (this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
									sqlite = new SQLite(this.getLogger(), "[KillCount]", "users", getDataFolder()+File.separator);
									sqlite.open();
									if (sqlite.checkConnection()) {
										try {
											sqlite.wipeTable("killcount");
											player.sendMessage(ChatColor.RED + "Database emptied!");
											hashmap.remove(player.getPlayer());
										} catch (Exception e) {
											if (this.getConfig().getBoolean("debug")) {
												e.printStackTrace();
											}
										}
										sqlite.close();
									}
								}
							} else {
								player.sendMessage(ChatColor.RED + "What are you confirming?");
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
						}
					}
				} else if (args.length == 2) {
					if (args[0].equalsIgnoreCase("reset")) {
						if (player.hasPermission("killcount.reset")) {
							if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
								db = new MySQL(this.getLogger(), "[KillCount]", dbhost, dbport, database, dbuser, dbpass);
								db.open();
								if (db.checkConnection()) {
									try {
										ResultSet result = db.query("SELECT COUNT(*) FROM killcount WHERE username='" + args[1] + "'");
										result.first();
										int count = result.getInt(1);
										result.close();
										if (count == 0) {
											player.sendMessage(ChatColor.RED + args[1] + " isn't in the database!");
										} else {
											db.query("UPDATE killcount SET kills='0', deaths='0' WHERE username='" + args[1] + "'");
											player.sendMessage(ChatColor.RED + args[1] + " has been reset!");
										}
									} catch (Exception e) {
										if (this.getConfig().getBoolean("debug")) {
											e.printStackTrace();
										}
									}
									db.close();
								}
							}
							if (this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
								sqlite = new SQLite(this.getLogger(), "[KillCount]", "users", getDataFolder()+File.separator);
								sqlite.open();
								if (sqlite.checkConnection()) {
									try {
										ResultSet result = sqlite.query("SELECT COUNT(*) FROM killcount WHERE username='" + args[1] + "'");
										int count = result.getInt(1);
										result.close();
										if (count == 0) {
											player.sendMessage(ChatColor.RED + args[1] + " isn't in the database!");
										} else {
											sqlite.query("UPDATE killcount SET kills='0', deaths='0' WHERE username='" + args[1] + "'");
											result.close();
											player.sendMessage(ChatColor.RED + args[1] + " has been reset!");
										}
									} catch (Exception e) {
										if (this.getConfig().getBoolean("debug")) {
											e.printStackTrace();
										}
									}
									sqlite.close();
								}
							}
						} else {
							player.sendMessage(ChatColor.RED + "You don't have permission to do that!");
						}
					} else {
						player.sendMessage(ChatColor.RED + "Correct usage is " + ChatColor.GOLD + "/kca reset [playername]");
					}
				} else {
					player.sendMessage(ChatColor.RED + "Too many arguments!");
				}
			}
		}
		return false;
	}
	
    private void setupDatabase() {
    	
    	if (this.getConfig().getString("storage-type").equalsIgnoreCase("mysql")) {
        	dbhost = this.getConfig().getString("mysql.host");
        	database = this.getConfig().getString("mysql.database");
        	dbuser = this.getConfig().getString("mysql.username");
        	dbpass = this.getConfig().getString("mysql.password");
        	dbport = this.getConfig().getString("mysql.port");
        	
        	db = new MySQL(this.getLogger(), "[KillCount]", dbhost, dbport, database, dbuser, dbpass);
        	this.getLogger().info("Connecting to MySQL database...");
        	db.open();
        	if (db.checkConnection()) {
        		this.getLogger().info("Successfully connected to database!");
        		if (!db.checkTable("killcount")) {
        			this.getLogger().info("Creating table 'killcount' in database " + this.getConfig().getString("mysql.database"));
        			db.createTable("CREATE TABLE killcount ( id int NOT NULL AUTO_INCREMENT, username VARCHAR(32) NOT NULL, kills int NOT NULL, deaths int NOT NULL, PRIMARY KEY (id) ) ENGINE=MyISAM;");
        		}
        	}
        	db.close();
    	}
    	if (this.getConfig().getString("storage-type").equalsIgnoreCase("sqlite")) {
        	sqlite = new SQLite(this.getLogger(), "[KillCount]", "users", getDataFolder()+File.separator);
        	sqlite.open();
        	if (sqlite.checkConnection()) {
        		if (!sqlite.checkTable("killcount")) {
        			this.getLogger().info("Creating users.db...");
        			sqlite.createTable("CREATE TABLE killcount (id INTEGER NOT NULL, username VARCHAR(32) NOT NULL, kills INTEGER NOT NULL, deaths INTEGER NOT NULL, PRIMARY KEY (id), UNIQUE (username))");
        		}
        	}
        	sqlite.close();
    	}
    }
    
    public double roundDouble(double d) {
    	DecimalFormat format = new DecimalFormat("#.##");
    	return Double.valueOf(format.format(d));
    }
}