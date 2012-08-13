package com.unlucky4ever.killcount.listeners;

import java.sql.ResultSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import com.unlucky4ever.killcount.KillCount;
import com.unlucky4ever.killcount.extras.sql.MySQL;

public class PlayerListener implements Listener {
	
	public KillCount plugin;
	
	public PlayerListener(KillCount instance) {
		plugin = instance;
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
			logKill(killer.getName());
		}
		if (killed.hasPermission("killcount.death")) {
			logDeath(killed.getName());
		}
	}
	public void logKill(String username) {
		KillCount.db = new MySQL(plugin.getLogger(), "[KillCount]", KillCount.dbhost, KillCount.dbport, KillCount.database, KillCount.dbuser, KillCount.dbpass);
		KillCount.db.open();
		if (KillCount.db.checkConnection()) {
			try {
				ResultSet kill = KillCount.db.query("SELECT COUNT(*) FROM killcount WHERE username='" + username + "'");
				kill.first();
				int count = kill.getInt(1);
				if (count == 0) {
					if (plugin.getConfig().getBoolean("debug")) {
						plugin.getLogger().info("Inserted " + username);
					}
					KillCount.db.query("INSERT INTO killcount (username, kills, deaths) VALUES ('" + username + "', 0, 0)");
					kill.close();
				}
				kill = KillCount.db.query("SELECT kills FROM killcount WHERE username='" + username + "'");
				kill.first();
				int kills = kill.getInt(1);
				kill.close();
				kills++;
				if (plugin.getConfig().getBoolean("debug")) {
					plugin.getLogger().info("Added a kill to " + username);
				}
				KillCount.db.query("UPDATE killcount SET kills='" + kills + "' WHERE username='" + username + "'");
			} catch (Exception e) {
				if (plugin.getConfig().getBoolean("debug")) {
					e.printStackTrace();
				}
			}
		}
	}
	public void logDeath(String username) {
		KillCount.db = new MySQL(plugin.getLogger(), "[KillCount]", KillCount.dbhost, KillCount.dbport, KillCount.database, KillCount.dbuser, KillCount.dbpass);
		KillCount.db.open();
		if (KillCount.db.checkConnection()) {
			try {
				ResultSet death = KillCount.db.query("SELECT COUNT(*) FROM killcount WHERE username='" + username + "'");
				death.first();
				int count = death.getInt(1);
				if (count == 0) {
					if (plugin.getConfig().getBoolean("debug")) {
						plugin.getLogger().info("Inserted " + username);
					}
					KillCount.db.query("INSERT INTO killcount (username, kills, deaths) VALUES ('" + username + "', 0, 0)");
					death.close();
				}
				death = KillCount.db.query("SELECT deaths FROM killcount WHERE username='" + username + "'");
				death.first();
				int deaths = death.getInt(1);
				death.close();
				deaths++;
				if (plugin.getConfig().getBoolean("debug")) {
					plugin.getLogger().info("Added a death to " + username);
				}
				KillCount.db.query("UPDATE killcount SET deaths='" + deaths + "' WHERE username='" + username + "'");
			} catch (Exception e) {
				if (plugin.getConfig().getBoolean("debug")) {
					e.printStackTrace();
				}
			}
		}
	}
}