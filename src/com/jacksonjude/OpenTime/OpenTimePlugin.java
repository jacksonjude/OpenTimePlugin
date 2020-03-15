package com.jacksonjude.OpenTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.jacksonjude.OpenTime.TimeRange.OnSelectedRange;

public class OpenTimePlugin extends JavaPlugin implements Listener 
{	
	private FileConfiguration fileConfig;
	private List<TimeRange> timeRanges;
	
	private Timer kickTimer;
	
	@Override
    public void onEnable()
	{
		saveDefaultConfig();
		loadTimeConfig();
		
		getCommand(Constants.ADMIN_COMMAND).setExecutor(new OpenTimeCommand(this));
		getCommand(Constants.ADMIN_COMMAND).setTabCompleter(new OpenTimeCompleter());
		this.getServer().getPluginManager().registerEvents(this, this);		
    }
	
	@Override
	public void onDisable()
	{
		if (kickTimer != null) kickTimer.cancel();
	}
	
	public void loadTimeConfig()
	{
		fileConfig = getConfig();
		
		timeRanges = new ArrayList<TimeRange>();
		for (String rangeName : fileConfig.getConfigurationSection(Constants.TIME_RANGES_CONFIG_KEY).getKeys(false))
		{
			ConfigurationSection newTimeRangeData = fileConfig.getConfigurationSection(Constants.TIME_RANGES_CONFIG_KEY + "." + rangeName);
			TimeRange newTimeRange = new TimeRange(newTimeRangeData, rangeName, this);
			timeRanges.add(newTimeRange);
		}
		
		restartKickTimer();
	}
	
	class KickTimerTask extends TimerTask
	{
		public void run()
		{
			handleAllPlayerKicks();
		}
	}
	
	public void restartKickTimer()
	{
		if (kickTimer != null) kickTimer.cancel();
		
		Calendar calendar = Calendar.getInstance();
		int seconds = calendar.get(Calendar.SECOND);
		
		kickTimer = new Timer();
		kickTimer.scheduleAtFixedRate(new KickTimerTask(), 1000*(60-seconds), 1000*60);
		handleAllPlayerKicks();		
	}
	
	@EventHandler
	public void playerLogin(PlayerLoginEvent e)
	{
		handlePlayerKick(e.getPlayer());
	}
	
	public void handleAllPlayerKicks()
	{
		for (Player player : this.getServer().getOnlinePlayers())
		{
			handlePlayerKick(player);
		}
	}
	
	public void handlePlayerKick(Player player)
	{
		if (player.hasPermission(Constants.BYPASS_PERMISSION)) return;
		
		if (shouldKickFromServer())
		{
			this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	              public void run() {
	            	  player.kickPlayer(Constants.KICK_REASON);
	              }
			}, 0);
		}
	}
	
	public boolean shouldKickFromServer()
	{
		return shouldKickFromServer((new Date()).getTime());
	}
	
	public boolean shouldKickFromServer(long timeToTest)
	{
		boolean shouldKickFromServer = true;
		for (TimeRange timeRange : timeRanges)
		{
			//System.out.println("  " + timeRange.isTimeInRange(timeToTest));
			//System.out.println("  " + (timeRange.getOnSelectedRange() == OnSelectedRange.CLOSE));
			if (timeRange.getOnSelectedRange() == OnSelectedRange.NONE) continue;
			if (timeRange.isTimeInRange(timeToTest) != (timeRange.getOnSelectedRange() == OnSelectedRange.CLOSE)) { shouldKickFromServer = false; break; }
		}
		
		return shouldKickFromServer;
	}
	
	public void throwComponentIntParseError(String rangeName, String componentKey)
	{
		this.getLogger().log(Level.WARNING, Constants.INT_PARSE_ERROR.replaceFirst("<timeRangesKey>", Constants.TIME_RANGES_CONFIG_KEY).replaceFirst("<rangeName>", rangeName).replaceFirst("<timeKey>", Constants.TIME_CONFIG_KEY).replaceFirst("<componentKey>", componentKey));
	}
	
	public void throwComponentRangeParseError(String rangeName, String componentKey)
	{
		this.getLogger().log(Level.WARNING, Constants.RANGE_PARSE_ERROR.replaceFirst("<timeRangesKey>", Constants.TIME_RANGES_CONFIG_KEY).replaceFirst("<rangeName>", rangeName).replaceFirst("<timeKey>", Constants.TIME_CONFIG_KEY).replaceFirst("<componentKey>", componentKey));
	}
	
	public void throwComponentValueError(String rangeName, String componentKey)
	{
		this.getLogger().log(Level.WARNING, Constants.VALUE_ERROR.replaceFirst("<timeRangesKey>", Constants.TIME_RANGES_CONFIG_KEY).replaceFirst("<rangeName>", rangeName).replaceFirst("<timeKey>", Constants.TIME_CONFIG_KEY).replaceFirst("<componentKey>", componentKey));
	}
}
