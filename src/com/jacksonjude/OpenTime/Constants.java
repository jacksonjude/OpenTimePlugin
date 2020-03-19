package com.jacksonjude.OpenTime;

public class Constants {
	public static final String TIME_RANGES_CONFIG_KEY = "time-ranges";
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
	public static final int TIME_COMPONENT_WEEK_MAX = 52;
	public static final int TIME_COMPONENT_WEEKDAY_MAX = 7;
	public static final int TIME_COMPONENT_DAY_MAX = 31;
	public static final int TIME_COMPONENT_HOUR_MAX = 23;
	public static final int TIME_COMPONENT_MINUTE_MAX = 59;
	
	public static final String KICK_REASON = "The server is closed (OpenTime)";
	public static final String INT_PARSE_ERROR = "Integer parse error on <timeRangesKey>.<rangeName>.<timeKey>.<componentKey>";
	public static final String RANGE_PARSE_ERROR = "Range parse error on <timeRangesKey>.<rangeName>.<timeKey>.<componentKey>";
	public static final String VALUE_ERROR = "Value error on <timeRangesKey>.<rangeName>.<timeKey>.<componentKey>";
	
	public static final String ADMIN_COMMAND = "opentime";
	public static final String ADMIN_PERMISSION = "opentime.admin";
	public static final String BYPASS_PERMISSION = "opentime.bypass";
}
