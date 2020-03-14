package com.jacksonjude.OpenTime;

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
		if (!sender.hasPermission(OpenTimePlugin.ADMIN_PERMISSION))
		{
			sender.sendMessage(ChatColor.RED + "You cannot edit DisableCraft config");
			return true;
		}
		
		if (args.length == 0)
		{
			sender.sendMessage(ChatColor.GOLD + "/opentime reload" + ChatColor.GRAY + " - reloads configuration");
			return true;
		}
		
		switch (args[0].toLowerCase())
		{
		case "reload":
			plugin.reloadConfig();
			plugin.loadTimeConfig();
			sender.sendMessage(ChatColor.GREEN + "OpenTime config reloaded");
			
			return true;
		}
		
		return false;
	}
}
