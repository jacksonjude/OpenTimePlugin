package com.jacksonjude.OpenTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class OpenTimePlugin extends JavaPlugin implements Listener 
{
	public static final String TIME_CONFIG_KEY = "time";
	public static final String ON_SELECTED_RANGE_KEY = "on-selected-range";
	public static final String TIMEZONE_CONFIG_KEY = "timezone";
	
	public static final String TIME_COMPONENT_YEAR_KEY = "year";
	public static final String TIME_COMPONENT_MONTH_KEY = "month";
	public static final String TIME_COMPONENT_WEEK_KEY = "week";
	public static final String TIME_COMPONENT_WEEKDAY_KEY = "weekday";
	public static final String TIME_COMPONENT_DAY_KEY = "day";
	public static final String TIME_COMPONENT_HOUR_KEY = "hour";
	public static final String TIME_COMPONENT_MINUTE_KEY = "minute";
	
	public static final int TIME_COMPONENT_MONTH_MAX = 11;
	public static final int TIME_COMPONENT_WEEK_MAX = 51;
	public static final int TIME_COMPONENT_WEEKDAY_MAX = 6;
	public static final int TIME_COMPONENT_DAY_MAX = 31;
	public static final int TIME_COMPONENT_HOUR_MAX = 23;
	public static final int TIME_COMPONENT_MINUTE_MAX = 59;
	
	public static final String OPEN_VALUE = "open";
	public static final String CLOSE_VALUE = "close";
	
	public static final String KICK_REASON = "The server is closed";
	public static final String INT_PARSE_ERROR = "Integer parse error on " + TIME_CONFIG_KEY + ".";
	public static final String RANGE_PARSE_ERROR = "Range parse error on " + TIME_CONFIG_KEY + ".";
	public static final String VALUE_ERROR = "Value error on " + TIME_CONFIG_KEY + ".";
	
	public static final String ADMIN_COMMAND = "opentime";
	public static final String ADMIN_PERMISSION = "opentime.admin";
	public static final String BYPASS_PERMISSION = "opentime.bypass";
	
	private FileConfiguration fileConfig;
	private Map<String,Object> timeComponentData;
	private Map<String,List<Integer>> timeComponentValues;
	private boolean isTimeRangeSelectingClose;
	
	private Timer kickTimer;
	
	@Override
    public void onEnable()
	{
		saveDefaultConfig();
		loadTimeConfig();
		
		getCommand(ADMIN_COMMAND).setExecutor(new OpenTimeCommand(this));
		getCommand(ADMIN_COMMAND).setTabCompleter(new OpenTimeCompleter());
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
		
		timeComponentData = fileConfig.getConfigurationSection(TIME_CONFIG_KEY).getValues(false);
		updateTimeComponentValues();
		
		isTimeRangeSelectingClose = fileConfig.getString(ON_SELECTED_RANGE_KEY).toLowerCase().equals(CLOSE_VALUE);
		
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
		if (player.hasPermission(BYPASS_PERMISSION)) return;
		
		if (isTimeRangeSelectingClose == isTimeInRange((new Date()).getTime()))
		{
			this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	              public void run() {
	            	  player.kickPlayer(KICK_REASON);
	              }
			}, 0);
		}
	}
	
	public void updateTimeComponentValues()
	{
		timeComponentValues = new HashMap<String,List<Integer>>();
		
		List<Integer> allowedYears = getAllowedTimeComponentValues(TIME_COMPONENT_YEAR_KEY);
		List<Integer> allowedMonths = getAllowedTimeComponentValues(TIME_COMPONENT_MONTH_KEY);
		List<Integer> allowedWeeks = getAllowedTimeComponentValues(TIME_COMPONENT_WEEK_KEY);
		List<Integer> allowedWeekdays = getAllowedTimeComponentValues(TIME_COMPONENT_WEEKDAY_KEY);
		List<Integer> allowedDays = getAllowedTimeComponentValues(TIME_COMPONENT_DAY_KEY);
		List<Integer> allowedHours = getAllowedTimeComponentValues(TIME_COMPONENT_HOUR_KEY);
		List<Integer> allowedMinutes = getAllowedTimeComponentValues(TIME_COMPONENT_MINUTE_KEY);
		
		timeComponentValues.put(TIME_COMPONENT_YEAR_KEY, allowedYears);
		timeComponentValues.put(TIME_COMPONENT_MONTH_KEY, allowedMonths);
		timeComponentValues.put(TIME_COMPONENT_WEEK_KEY, allowedWeeks);
		timeComponentValues.put(TIME_COMPONENT_WEEKDAY_KEY, allowedWeekdays);
		timeComponentValues.put(TIME_COMPONENT_DAY_KEY, allowedDays);
		timeComponentValues.put(TIME_COMPONENT_HOUR_KEY, allowedHours);
		timeComponentValues.put(TIME_COMPONENT_MINUTE_KEY, allowedMinutes);
	}
	
	public boolean isTimeInRange(long timeToTest)
	{	
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeToTest);
		
		TimeZone timeZone = TimeZone.getTimeZone(fileConfig.getString(TIMEZONE_CONFIG_KEY));
		calendar.setTimeZone(timeZone);
		
		List<Integer> allowedYears = timeComponentValues.get(TIME_COMPONENT_YEAR_KEY);
		List<Integer> allowedMonths = timeComponentValues.get(TIME_COMPONENT_MONTH_KEY);
		List<Integer> allowedWeeks = timeComponentValues.get(TIME_COMPONENT_WEEK_KEY);
		List<Integer> allowedWeekdays = timeComponentValues.get(TIME_COMPONENT_WEEKDAY_KEY);
		List<Integer> allowedDays = timeComponentValues.get(TIME_COMPONENT_DAY_KEY);
		List<Integer> allowedHours = timeComponentValues.get(TIME_COMPONENT_HOUR_KEY);
		List<Integer> allowedMinutes = timeComponentValues.get(TIME_COMPONENT_MINUTE_KEY);
		
		return componentInRange(calendar, Calendar.YEAR, allowedYears)
				&& componentInRange(calendar, Calendar.MONTH, allowedMonths)
				&& componentInRange(calendar, Calendar.WEEK_OF_YEAR, allowedWeeks)
				&& componentInRange(calendar, Calendar.DAY_OF_WEEK, allowedWeekdays)
				&& componentInRange(calendar, Calendar.DAY_OF_MONTH, allowedDays)
				&& componentInRange(calendar, Calendar.HOUR, allowedHours)
				&& componentInRange(calendar, Calendar.MINUTE, allowedMinutes);
	}
	
	public boolean componentInRange(Calendar calendar, int calendarField, List<Integer> allowedComponentValues)
	{
		return allowedComponentValues == null || allowedComponentValues.size() == 0 || allowedComponentValues.contains(calendar.get(calendarField));
	}
	
	public List<Integer> getAllowedTimeComponentValues(String componentKey)
	{
		String[] rawTimeComponentData;
		String rawTimeComponentString = ((String) timeComponentData.get(componentKey));
		
		if (rawTimeComponentString.contains(","))
		{
			rawTimeComponentData = rawTimeComponentString.split(",");
		}
		else
		{
			rawTimeComponentData = new String[]{rawTimeComponentString};
		}
		
		List<Integer> timeComponentValues = new ArrayList<Integer>();
		
		for (int i=0; i < rawTimeComponentData.length; i++)
		{
			String componentValueString = rawTimeComponentData[i];
			if (componentValueString.equals("*"))
			{
				return timeComponentValues;
			}
			else if (componentValueString.contains("-"))
			{
				String[] splitTimeComponentItem = rawTimeComponentData[i].split("-");
				if (splitTimeComponentItem.length != 2) { throwComponentRangeParseError(componentKey); return null; }
				
				if (!isInteger(splitTimeComponentItem[0]) || !isInteger(splitTimeComponentItem[1])) { throwComponentIntParseError(componentKey); return null; }
				
				int rangeStart = Integer.parseInt(splitTimeComponentItem[0]);
				int rangeEnd = Integer.parseInt(splitTimeComponentItem[1]);
				if (rangeStart > rangeEnd || rangeStart < 0 || rangeStart > getMaxTimeComponentValue(componentKey) || rangeEnd < 0 || rangeEnd > getMaxTimeComponentValue(componentKey)) { throwComponentValueError(componentKey); return null; }
				
				for (int j=rangeStart; j <= rangeEnd; j++)
				{
					timeComponentValues.add(j);
				}
			}
			else
			{
				if (!isInteger(componentValueString)) { throwComponentIntParseError(componentKey); return null; }
				
				int componentValue = Integer.parseInt(componentValueString);
				if (componentValue < 0 || componentValue > getMaxTimeComponentValue(componentKey)) { throwComponentValueError(componentKey); return null; }
				
				timeComponentValues.add(componentValue);
			}
		}
		
		return timeComponentValues;
	}
	
	public int getMaxTimeComponentValue(String componentKey)
	{
		switch (componentKey)
		{
		case TIME_COMPONENT_YEAR_KEY:
			return Integer.MAX_VALUE;
		case TIME_COMPONENT_MONTH_KEY:
			return TIME_COMPONENT_MONTH_MAX;
		case TIME_COMPONENT_WEEK_KEY:
			return TIME_COMPONENT_WEEK_MAX;
		case TIME_COMPONENT_WEEKDAY_KEY:
			return TIME_COMPONENT_WEEKDAY_MAX;
		case TIME_COMPONENT_DAY_KEY:
			return TIME_COMPONENT_DAY_MAX;
		case TIME_COMPONENT_HOUR_KEY:
			return TIME_COMPONENT_HOUR_MAX;
		case TIME_COMPONENT_MINUTE_KEY:
			return TIME_COMPONENT_MINUTE_MAX;
		}
		
		return 0;
	}
	
	public void throwComponentIntParseError(String componentKey)
	{
		this.getLogger().log(Level.WARNING, INT_PARSE_ERROR + componentKey);
	}
	
	public void throwComponentRangeParseError(String componentKey)
	{
		this.getLogger().log(Level.WARNING, RANGE_PARSE_ERROR + componentKey);
	}
	
	public void throwComponentValueError(String componentKey)
	{
		this.getLogger().log(Level.WARNING, VALUE_ERROR + componentKey);
	}
	
	public static boolean isInteger(String s) {
	    return isInteger(s, 10);
	}

	public static boolean isInteger(String s, int radix) {
	    if (s.isEmpty()) return false;
	    for (int i = 0; i < s.length(); i++) {
	        if (i == 0 && s.charAt(i) == '-') {
	            if (s.length() == 1) return false;
	            else continue;
	        }
	        if (Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
}
