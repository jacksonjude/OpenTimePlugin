package com.jacksonjude.OpenTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bukkit.configuration.ConfigurationSection;

public class TimeRange
{
	enum OnSelectedRange
	{
		OPEN,
		CLOSE,
		NONE
	}
	
	private OpenTimePlugin plugin;
	
	private Map<String,Object> timeComponentData;
	private Map<String,List<Integer>> timeComponentValues;
	private OnSelectedRange onSelectedRange;
	private String timeZoneCode;
	private String timeRangeName;
	
	public TimeRange(ConfigurationSection rangeData, String rangeName, OpenTimePlugin plugin)
	{
		this.timeComponentData = rangeData.getConfigurationSection(Constants.TIME_CONFIG_KEY).getValues(false);
		this.onSelectedRange = OnSelectedRange.valueOf(rangeData.getString(Constants.ON_SELECTED_RANGE_KEY).toUpperCase());
		this.timeZoneCode = rangeData.getString(Constants.TIMEZONE_CONFIG_KEY);
		this.timeRangeName = rangeName;
		
		this.plugin = plugin;
		
		updateTimeComponentValues();		
	}
	
	public void updateTimeComponentValues()
	{
		timeComponentValues = new HashMap<String,List<Integer>>();
		
		List<Integer> allowedYears = getAllowedTimeComponentValues(Constants.TIME_COMPONENT_YEAR_KEY);
		List<Integer> allowedMonths = getAllowedTimeComponentValues(Constants.TIME_COMPONENT_MONTH_KEY);
		List<Integer> allowedWeeks = getAllowedTimeComponentValues(Constants.TIME_COMPONENT_WEEK_KEY);
		List<Integer> allowedWeekdays = getAllowedTimeComponentValues(Constants.TIME_COMPONENT_WEEKDAY_KEY);
		List<Integer> allowedDays = getAllowedTimeComponentValues(Constants.TIME_COMPONENT_DAY_KEY);
		List<Integer> allowedHours = getAllowedTimeComponentValues(Constants.TIME_COMPONENT_HOUR_KEY);
		List<Integer> allowedMinutes = getAllowedTimeComponentValues(Constants.TIME_COMPONENT_MINUTE_KEY);
		
		timeComponentValues.put(Constants.TIME_COMPONENT_YEAR_KEY, allowedYears);
		timeComponentValues.put(Constants.TIME_COMPONENT_MONTH_KEY, allowedMonths);
		timeComponentValues.put(Constants.TIME_COMPONENT_WEEK_KEY, allowedWeeks);
		timeComponentValues.put(Constants.TIME_COMPONENT_WEEKDAY_KEY, allowedWeekdays);
		timeComponentValues.put(Constants.TIME_COMPONENT_DAY_KEY, allowedDays);
		timeComponentValues.put(Constants.TIME_COMPONENT_HOUR_KEY, allowedHours);
		timeComponentValues.put(Constants.TIME_COMPONENT_MINUTE_KEY, allowedMinutes);
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
				if (splitTimeComponentItem.length != 2) { plugin.throwComponentRangeParseError(timeRangeName, componentKey); return null; }
				
				if (!isInteger(splitTimeComponentItem[0]) || !isInteger(splitTimeComponentItem[1])) { plugin.throwComponentIntParseError(timeRangeName, componentKey); return null; }
				
				int rangeStart = Integer.parseInt(splitTimeComponentItem[0]);
				int rangeEnd = Integer.parseInt(splitTimeComponentItem[1]);
				if (rangeStart > rangeEnd || rangeStart < 0 || rangeStart > getMaxTimeComponentValue(componentKey) || rangeEnd < 0 || rangeEnd > getMaxTimeComponentValue(componentKey)) { plugin.throwComponentValueError(timeRangeName, componentKey); return null; }
				
				for (int j=rangeStart; j <= rangeEnd; j++)
				{
					timeComponentValues.add(j);
				}
			}
			else
			{
				if (!isInteger(componentValueString)) { plugin.throwComponentIntParseError(timeRangeName, componentKey); return null; }
				
				int componentValue = Integer.parseInt(componentValueString);
				if (componentValue < 0 || componentValue > getMaxTimeComponentValue(componentKey)) { plugin.throwComponentValueError(timeRangeName, componentKey); return null; }
				
				timeComponentValues.add(componentValue);
			}
		}
		
		return timeComponentValues;
	}
	
	public int getMaxTimeComponentValue(String componentKey)
	{
		switch (componentKey)
		{
		case Constants.TIME_COMPONENT_YEAR_KEY:
			return Integer.MAX_VALUE;
		case Constants.TIME_COMPONENT_MONTH_KEY:
			return Constants.TIME_COMPONENT_MONTH_MAX;
		case Constants.TIME_COMPONENT_WEEK_KEY:
			return Constants.TIME_COMPONENT_WEEK_MAX;
		case Constants.TIME_COMPONENT_WEEKDAY_KEY:
			return Constants.TIME_COMPONENT_WEEKDAY_MAX;
		case Constants.TIME_COMPONENT_DAY_KEY:
			return Constants.TIME_COMPONENT_DAY_MAX;
		case Constants.TIME_COMPONENT_HOUR_KEY:
			return Constants.TIME_COMPONENT_HOUR_MAX;
		case Constants.TIME_COMPONENT_MINUTE_KEY:
			return Constants.TIME_COMPONENT_MINUTE_MAX;
		}
		
		return 0;
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
	
	public boolean isTimeInRange(long timeToTest)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeToTest);
		
		TimeZone timeZone = TimeZone.getTimeZone(timeZoneCode);
		calendar.setTimeZone(timeZone);
		
		List<Integer> allowedYears = timeComponentValues.get(Constants.TIME_COMPONENT_YEAR_KEY);
		List<Integer> allowedMonths = timeComponentValues.get(Constants.TIME_COMPONENT_MONTH_KEY);
		List<Integer> allowedWeeks = timeComponentValues.get(Constants.TIME_COMPONENT_WEEK_KEY);
		List<Integer> allowedWeekdays = timeComponentValues.get(Constants.TIME_COMPONENT_WEEKDAY_KEY);
		List<Integer> allowedDays = timeComponentValues.get(Constants.TIME_COMPONENT_DAY_KEY);
		List<Integer> allowedHours = timeComponentValues.get(Constants.TIME_COMPONENT_HOUR_KEY);
		List<Integer> allowedMinutes = timeComponentValues.get(Constants.TIME_COMPONENT_MINUTE_KEY);
		
		//System.out.println("  - " + allowedHours + " / " + calendar.get(Calendar.HOUR) + " -- " + calendar.get(Calendar.WEEK_OF_YEAR) + " -- " + calendar.get(Calendar.YEAR));
		//System.out.println(this.timeRangeName + ": " + componentInRange(calendar, Calendar.YEAR, allowedYears) + " -- " + componentInRange(calendar, Calendar.MONTH, allowedMonths) + " -- " + componentInRange(calendar, Calendar.WEEK_OF_YEAR, allowedWeeks) + " -- " + calendar.get(Calendar.DAY_OF_WEEK) + "/" + allowedWeekdays + "=" + componentInRange(calendar, Calendar.DAY_OF_WEEK, allowedWeekdays) + " -- " + componentInRange(calendar, Calendar.DAY_OF_MONTH, allowedDays) + " -- " + componentInRange(calendar, Calendar.HOUR_OF_DAY, allowedHours) + " -- " + componentInRange(calendar, Calendar.MINUTE, allowedMinutes));
		
		return componentInRange(calendar, Calendar.YEAR, allowedYears)
				&& componentInRange(calendar, Calendar.MONTH, allowedMonths)
				&& componentInRange(calendar, Calendar.WEEK_OF_YEAR, allowedWeeks)
				&& componentInRange(calendar, Calendar.DAY_OF_WEEK, allowedWeekdays)
				&& componentInRange(calendar, Calendar.DAY_OF_MONTH, allowedDays)
				&& componentInRange(calendar, Calendar.HOUR_OF_DAY, allowedHours)
				&& componentInRange(calendar, Calendar.MINUTE, allowedMinutes);
	}
	
	public boolean componentInRange(Calendar calendar, int calendarField, List<Integer> allowedComponentValues)
	{
		return allowedComponentValues == null || allowedComponentValues.size() == 0 || allowedComponentValues.contains(calendar.get(calendarField));
	}
	
	public OnSelectedRange getOnSelectedRange()
	{
		return onSelectedRange;
	}
}
