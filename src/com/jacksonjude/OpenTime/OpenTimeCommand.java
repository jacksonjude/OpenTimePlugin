package com.jacksonjude.OpenTime;

import java.util.Date;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OpenTimeCommand implements CommandExecutor
{
	private final OpenTimePlugin plugin;
	
	public OpenTimeCommand(OpenTimePlugin plugin)
	{
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!sender.hasPermission(Constants.ADMIN_PERMISSION))
		{
			sender.sendMessage(ChatColor.RED + "You cannot edit OpenTime config");
			return true;
		}
		
		if (args.length == 0)
		{
			sender.sendMessage(ChatColor.GOLD + "/opentime reload" + ChatColor.GRAY + " - reloads configuration" + "\n" + ChatColor.GOLD + "/opentime test <time>" + ChatColor.GRAY + " - tests time");
			return true;
		}
		
		switch (args[0].toLowerCase())
		{
		case "reload":
			plugin.reloadConfig();
			plugin.loadTimeConfig();
			sender.sendMessage(ChatColor.GREEN + "OpenTime config reloaded");
			
			return true;
		case "test":
			long timeToTest = (new Date()).getTime();
			if (args.length >= 2) timeToTest = Long.parseLong(args[1]);
			
			sender.sendMessage(String.valueOf(plugin.shouldKickFromServer(timeToTest)));
			
			return true;
		}
		
		return false;
	}
}
