package com.ZnD.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener
{
	public ArrayList<String> Taggers = new ArrayList<String>();
	public ArrayList<String> Runners = new ArrayList<String>();
	public ArrayList<String> Frozen = new ArrayList<String>();
	public Map<String, Long> cooldowns = new HashMap<String, Long>();

	public boolean start;
	public boolean taggersWin;
	public boolean runnersWin;
	public boolean firstTime;

	ScoreboardManager manager = Bukkit.getScoreboardManager();
	Scoreboard scoreboard = manager.getNewScoreboard();
	Objective objective = scoreboard.registerNewObjective("runners", "dummy",
			ChatColor.GREEN + "" + ChatColor.BOLD + "Runners");

	Team taggerTeam = scoreboard.registerNewTeam("Taggers");
	Team runnerTeam = scoreboard.registerNewTeam("Runners");
	Team frozenTeam = scoreboard.registerNewTeam("Frozen");

	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(this, this);

		BukkitScheduler scheduler = getServer().getScheduler();

		scheduler.runTaskTimer(this, new Runnable()
		{
			public void run()
			{
				objective.setDisplaySlot(DisplaySlot.SIDEBAR);

				taggerTeam.setPrefix(ChatColor.RED + "[Tagger] ");
				runnerTeam.setPrefix(ChatColor.GREEN + "[Runner] ");
				frozenTeam.setPrefix(ChatColor.AQUA + "[Frozen] ");

				for (int i = 0; i < Runners.size(); i++)
				{
					Bukkit.getPlayer(Runners.get(i)).setScoreboard(scoreboard);
				}

				for (int i = 0; i < Taggers.size(); i++)
				{
					Bukkit.getPlayer(Taggers.get(i)).setScoreboard(scoreboard);
				}

				if (start)
				{
					for (int i = 0; i < Runners.size(); i++)
					{
						if (Frozen.contains(Runners.get(i)))
						{
							frozenTeam.addPlayer(Bukkit.getPlayer(Runners.get(i)));
							runnerTeam.removePlayer(Bukkit.getPlayer(Runners.get(i)));
							objective.getScore(Runners.get(i)).setScore(i);
						}

						objective.getScore(Runners.get(i)).setScore(i);
					}

					for (int i = 0; i < Runners.size(); i++)
					{
						Bukkit.getPlayer(Runners.get(i)).setScoreboard(scoreboard);
					}

					for (int i = 0; i < Taggers.size(); i++)
					{
						Bukkit.getPlayer(Taggers.get(i)).setScoreboard(scoreboard);
					}
				}

				if (Runners.size() == Frozen.size() & start & !taggersWin)
				{
					for (int i = 0; i < Taggers.size(); i++)
					{
						Player p = Bukkit.getPlayer(Taggers.get(i));

						taggerTeam.removePlayer(Bukkit.getPlayer(Taggers.get(i)));
						objective.getScoreboard().resetScores(Bukkit.getPlayer(Taggers.get(i)));
						Bukkit.getPlayer(Taggers.get(i)).setScoreboard(scoreboard);

						p.sendTitle(ChatColor.RED + "Taggers win!", null);
					}

					for (int i = 0; i < Runners.size(); i++)
					{
						Player p = Bukkit.getPlayer(Runners.get(i));

						runnerTeam.removePlayer(Bukkit.getPlayer(Runners.get(i)));
						objective.getScoreboard().resetScores(Bukkit.getPlayer(Runners.get(i)));
						Bukkit.getPlayer(Runners.get(i)).setScoreboard(scoreboard);

						p.sendTitle(ChatColor.RED + "Taggers win!", null);
					}

					for (int i = 0; i < Frozen.size(); i++)
					{
						frozenTeam.removePlayer(Bukkit.getPlayer(Frozen.get(i)));
						objective.getScoreboard().resetScores(Bukkit.getPlayer(Frozen.get(i)));
						Bukkit.getPlayer(Frozen.get(i)).setScoreboard(scoreboard);
					}

					Taggers.clear();
					Runners.clear();
					Frozen.clear();

					Bukkit.broadcastMessage(
							ChatColor.AQUA + "" + ChatColor.BOLD + "Freeze Tag: " + ChatColor.RED + "Taggers win!");
					taggersWin = true;
					start = false;
					return;
				}
			}

		}, 0L, 50L);
	}

	@Override
	public void onDisable()
	{

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg)
	{
		Player player = (Player) sender;

		if (label.equalsIgnoreCase("ft"))
		{
			if (sender instanceof Player)
			{
				if (arg.length == 1)
				{ // Shows all of the players in the teams
					if (arg[0].equalsIgnoreCase("list"))
					{
						player.sendMessage(
								ChatColor.LIGHT_PURPLE + "-----------------------------------------------------");
						player.sendMessage(ChatColor.RED + "Taggers: " + Taggers);
						player.sendMessage(ChatColor.GREEN + "Runners: " + Runners);
						player.sendMessage(ChatColor.AQUA + "Frozen: " + Frozen);
						player.sendMessage(
								ChatColor.LIGHT_PURPLE + "-----------------------------------------------------");

						return true;
					}

				

					if (arg[0].equalsIgnoreCase("join"))
					{
						TextComponent message = new TextComponent(ChatColor.LIGHT_PURPLE + "Join team:      ");
						TextComponent joinTaggersMessage = new TextComponent(
								ChatColor.RED + "" + ChatColor.BOLD + "[Tagger]   ");
						TextComponent joinRunnersMessage = new TextComponent(
								ChatColor.GREEN + "" + ChatColor.BOLD + "   [Runner]");

						joinTaggersMessage
								.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ft join Taggers"));
						joinTaggersMessage
								.setHoverEvent(
										new HoverEvent(HoverEvent.Action.SHOW_TEXT,
												new ComponentBuilder(ChatColor.GREEN + "Click to join the "
														+ ChatColor.RED + "Tagger" + ChatColor.GREEN + " team.")
																.create()));

						joinRunnersMessage
								.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ft join Runners"));
						joinRunnersMessage
								.setHoverEvent(
										new HoverEvent(HoverEvent.Action.SHOW_TEXT,
												new ComponentBuilder(ChatColor.GREEN + "Click to join the "
														+ ChatColor.AQUA + "Runner" + ChatColor.GREEN + " team.")
																.create()));

						message.addExtra(joinTaggersMessage);
						message.addExtra(joinRunnersMessage);

						player.sendMessage(ChatColor.GREEN + "-----------------------------------------------------");
						player.spigot().sendMessage(message);
						player.sendMessage(ChatColor.GREEN + "-----------------------------------------------------");

						return true;
					}

					// This is to reset all of the teams
					if (arg[0].equalsIgnoreCase("reset"))
					{
						for (int i = 0; i < Taggers.size(); i++)
						{
							taggerTeam.removePlayer(Bukkit.getPlayer(Taggers.get(i)));
						}

						for (int i = 0; i < Runners.size(); i++)
						{
							runnerTeam.removePlayer(Bukkit.getPlayer(Runners.get(i)));
						}

						for (int i = 0; i < Frozen.size(); i++)
						{
							frozenTeam.removePlayer(Bukkit.getPlayer(Frozen.get(i)));
						}

						Taggers.clear();
						Runners.clear();
						Frozen.clear();

						start = false;
						taggersWin = false;
						runnersWin = false;

						player.getServer()
								.broadcastMessage(ChatColor.AQUA + "Freeze Tag" + ChatColor.GOLD + " has been reset.");

						return true;
					}

					if (arg[0].equalsIgnoreCase("start"))
					{
						if (Taggers.contains(player.getName()) & start)
						{
							player.sendMessage(ChatColor.AQUA + "Freeze Tag" + ChatColor.RED + " has already started!");
							player.sendMessage(ChatColor.GREEN + "A Player Tracker has been added to your inventory");
							player.getInventory().addItem(playertracker());
							return true;
						}

						if (Runners.contains(player.getName()) & start)
						{
							player.sendMessage(ChatColor.AQUA + "Freeze Tag" + ChatColor.RED + " has already started!");
							player.sendMessage(ChatColor.GREEN + "A Player Tracker has been added to your inventory");
							player.getInventory().addItem(playertracker());
							return true;
						}

						if (Taggers.size() >= 1 & Runners.size() >= 1
								& (Taggers.contains(player.getName()) || Runners.contains(player.getName())))
						{
							start = true;
							taggersWin = false;
							runnersWin = false;

							for (int i = 0; i < Taggers.size(); i++)
							{
								Player p = Bukkit.getPlayer(Taggers.get(i));

								p.getInventory().clear();
								p.setHealth(player.getMaxHealth());
								p.setFoodLevel(20);
								p.setSaturation(10);
								p.sendTitle(ChatColor.AQUA + "Freeze Tag" + ChatColor.GOLD + " has begun!",
										ChatColor.RED + "You are on the Tagger team.");
								p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 3);
							}

							for (int i = 0; i < Runners.size(); i++)
							{
								Player p = Bukkit.getPlayer(Runners.get(i));

								p.getInventory().clear();
								p.setHealth(p.getMaxHealth());
								p.setFoodLevel(20);
								p.setSaturation(10);
								p.sendTitle(ChatColor.AQUA + "Freeze Tag" + ChatColor.GOLD + " has begun!",
										ChatColor.GREEN + "You are on the Runner team.");
								p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 3);
							}

							for (int i = 0; i < Taggers.size(); i++)
							{
								Player target = Bukkit.getServer().getPlayer(Taggers.get(i));
								target.getInventory().addItem(playertracker());
							}

							for (int i = 0; i < Runners.size(); i++)
							{
								Player target = Bukkit.getServer().getPlayer(Runners.get(i));
								target.getInventory().addItem(playertracker());
							}

							Bukkit.getServer()
									.broadcastMessage(ChatColor.AQUA + "Freeze Tag" + ChatColor.GOLD + " has begun!");
							player.getWorld().strikeLightningEffect(player.getLocation().add(0, 100, 0));

							return true;
						}

						else
						{
							player.sendMessage(ChatColor.RED
									+ "You can not start the game because you are not in a team or there needs to be at least 1 player on each team.");
							return true;
						}
					}
				}

				if (arg.length == 2)
				{ // This is to join the team of Taggers
					if (arg[0].equalsIgnoreCase("join") & arg[1].equalsIgnoreCase("taggers"))
					{
						
						

                        // Removes the player if they are already in the team
						if (Runners.contains(player.getName()))
						{
							Runners.remove(player.getName());
							runnerTeam.removePlayer(player);
						}
                       
						Taggers.add(player.getName());
						taggerTeam.addPlayer(player);

						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.AQUA
								+ "You joined team " + ChatColor.RED + "Taggers" + ChatColor.AQUA + "."));
						player.getServer().broadcastMessage(ChatColor.GOLD + player.getDisplayName() + ChatColor.AQUA
								+ " has joined " + ChatColor.RED + "Taggers" + ChatColor.AQUA + ".");

						return true;
					}
					// This is to join the team of Runners
					if (arg[0].equalsIgnoreCase("join") & arg[1].equalsIgnoreCase("runners"))
					{
						// Removes the player if they are already in the team.
						if(start)
						{
                            player.sendMessage("The game has already started you cannot change your team!");
						    return true;
						}

						if (Taggers.contains(player.getName()))
						{
							Taggers.remove(player.getName());
							taggerTeam.removePlayer(player);
							player.sendMessage("You are already in this team!");
						}

						Runners.add(player.getName());
						runnerTeam.addPlayer(player);

						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.AQUA
								+ "You joined team " + ChatColor.GREEN + "Runners" + ChatColor.AQUA + "."));
						player.getServer().broadcastMessage(ChatColor.GOLD + player.getDisplayName() + ChatColor.AQUA
								+ " has joined " + ChatColor.GREEN + "Runners" + ChatColor.AQUA + ".");

						return true;
					}
				}

				if (arg.length == 3)
				{
					Player target = Bukkit.getPlayerExact(arg[2]);

					if (arg[0].equalsIgnoreCase("join") & arg[1].equalsIgnoreCase("taggers"))
					{
						if (arg[2].equals(target.getName()))
						{
							// Removes the player if they are already in the team.
							if (Runners.contains(arg[2]))
							{
								Runners.remove(arg[2]);
								runnerTeam.removePlayer(Bukkit.getPlayer(arg[2]));
							}

							Taggers.add(arg[2]);
							taggerTeam.removePlayer(Bukkit.getPlayer(arg[2]));

							player.getServer().broadcastMessage(ChatColor.GOLD + arg[2] + ChatColor.AQUA
									+ " has joined " + ChatColor.RED + "Taggers" + ChatColor.AQUA + ".");

							return true;
						}

						else
						{
							player.sendMessage(ChatColor.RED + "Error: That player does not exist! (Case-Sensitive) ");

							return true;
						}

						// Removes the player if they are already in the team

					}

					// This is to join the team of Runners
					if (arg[0].equalsIgnoreCase("join") & arg[1].equalsIgnoreCase("runners"))
					{
						if (arg[2].equals(target.getName()))
						{
							// Removes the player if they are already in the team.
							if (Taggers.contains(arg[2]))
							{
								Taggers.remove(arg[2]);
								taggerTeam.removePlayer(Bukkit.getPlayer(arg[2]));
							}

							Runners.add(arg[2]);
							runnerTeam.addPlayer(Bukkit.getPlayer(arg[2]));

							player.getServer().broadcastMessage(ChatColor.GOLD + arg[2] + ChatColor.AQUA
									+ " has joined " + ChatColor.GREEN + "Runners" + ChatColor.AQUA + ".");

							return true;
						}

						else
						{
							player.sendMessage(ChatColor.RED + "Error: That player does not exist! (Case-Sensitive) ");

							return true;
						}
					}
				}

				// Basic UI for all of the commands
				else
				{
					player.sendMessage(ChatColor.GREEN + "-----------------------------------------------------");
					player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to Freeze Tag!");
					player.sendMessage(ChatColor.LIGHT_PURPLE + "Commands:");
					player.sendMessage(ChatColor.AQUA + "/ft list" + ChatColor.LIGHT_PURPLE
							+ ": Displays the list of players on each team and players that are frozen.");
					player.sendMessage(ChatColor.AQUA + "/ft join" + ChatColor.LIGHT_PURPLE + ": Join a team.");
					player.sendMessage(
							ChatColor.AQUA + "/ft join Taggers" + ChatColor.LIGHT_PURPLE + ": Join Tagger team.");
					player.sendMessage(
							ChatColor.AQUA + "/ft join Runners" + ChatColor.LIGHT_PURPLE + ": Join Runner team.");
					player.sendMessage(ChatColor.AQUA + "/ft reset" + ChatColor.LIGHT_PURPLE
							+ ": Resets the game and resets teams.");
					player.sendMessage(ChatColor.AQUA + "/ft start" + ChatColor.LIGHT_PURPLE + ": Starts the game.");
					player.sendMessage(ChatColor.GREEN + "-----------------------------------------------------");

					return true;
				}
			}
		}
		return false;
	}

	public ItemStack playertracker()
	{
		ItemStack playertracker = new ItemStack(Material.COMPASS);
		ItemMeta meta = playertracker.getItemMeta();
		meta.setDisplayName(ChatColor.DARK_RED + "Player Tracker");
		ArrayList<String> lore = new ArrayList<>();
		lore.add("");
		lore.add(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + "Tracks runners!");
		lore.add(ChatColor.DARK_GRAY + "(LEFT CLICK) " + ChatColor.WHITE + "Switches target");
		lore.add(ChatColor.DARK_GRAY + "(RIGHT CLICK) " + ChatColor.WHITE + "Tracks target");
		meta.setLore(lore);
		meta.addItemFlags(new ItemFlag[]
		{ ItemFlag.HIDE_ATTRIBUTES });
		playertracker.setItemMeta(meta);
		return playertracker;
	}

	int index = 0;

	@EventHandler
	public void playerTrackerOnClick(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (player.getInventory().contains(playertracker()))
		{
			if (player.getInventory().getItemInMainHand().equals(playertracker()))
			{
				if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
				{
					this.index++;
					if (this.index == Runners.size())
						this.index = 0;
					player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
							new TextComponent(ChatColor.RED + "Target switched to " + ChatColor.GREEN
									+ (String) Runners.get(index) + ChatColor.RED + "..."));
				}
				if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				{
					Player runner = Bukkit.getServer().getPlayer(Runners.get(this.index));
					Location runnerLocation = runner.getLocation();
					if (runner.getLocation().getWorld().getName().endsWith("_nether")
							|| runner.getLocation().getWorld().getName().endsWith("_the_end"))
					{
						player.sendMessage(ChatColor.GREEN + (String) Runners.get(this.index) + ChatColor.RED
								+ " is not in the overworld.");
					} else
					{
						player.setCompassTarget(runnerLocation);
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED
								+ "Locating " + ChatColor.GREEN + (String) Runners.get(index) + ChatColor.RED + "..."));
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e)
	{
		Player player = e.getPlayer();
		Location from = e.getFrom();
		Location to = e.getTo();

		if (Frozen.contains(player.getName()))
		{
			if (to.getX() == from.getBlockX() && to.getZ() == from.getBlockZ())
			{
				return;
			}

			e.setCancelled(true);

			player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
					new TextComponent(ChatColor.DARK_RED + "You are frozen so you cannot move!"));
		}
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent e)
	{
		if (e.getDamager() instanceof Player & e.getEntity() instanceof Player & start)
		{

			Player damager = (Player) e.getDamager();
			Player damaged = (Player) e.getEntity();
			String damagerName = damager.getDisplayName();
			String damagedName = damaged.getDisplayName();

			if (Taggers.contains(damagerName))
			{
				if (cooldowns.containsKey(damagedName))
				{
					if (cooldowns.get(damagedName) > System.currentTimeMillis())
					{
						long timeLeft = (cooldowns.get(damagedName) - System.currentTimeMillis()) / 1000;
						damager.sendMessage(ChatColor.GREEN + damagedName + ChatColor.GOLD + " can be frozen in "
								+ timeLeft + ChatColor.GOLD + " seconds.");
						damaged.sendTitle(ChatColor.AQUA + "You can be frozen again in: " + timeLeft, null);
						e.setCancelled(true);

						return;
					}
				}

				cooldowns.put(damagedName, System.currentTimeMillis() + (10 * 1000));

				if(!Frozen.contains(damagedName))
				{
					Frozen.add(damagedName);
				}
				
				frozenTeam.addPlayer(damaged);
				e.setCancelled(true);
				damaged.getWorld().playSound(damaged.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 5, 1);
				e.getEntity().sendMessage(ChatColor.AQUA + "You are now frozen!");
				Bukkit.getServer().broadcastMessage(ChatColor.GREEN + damagedName + ChatColor.GOLD
						+ " has been frozen by " + ChatColor.RED + damagerName + ChatColor.GOLD + "!");
			}

			if (Runners.contains(damagerName) & Frozen.contains(damagedName))
			{
				if (cooldowns.containsKey(damagedName))
				{
					if (cooldowns.get(damagedName) > System.currentTimeMillis())
					{
						long timeLeft = (cooldowns.get(damagedName) - System.currentTimeMillis()) / 1000;
						damager.sendMessage(ChatColor.GREEN + damagedName + ChatColor.GOLD + " can be unfrozen in "
								+ timeLeft + ChatColor.GOLD + " seconds.");
						damaged.sendTitle("You can be unfrozen in: " + timeLeft, null);
						e.setCancelled(true);

						return;
					}
				}

				cooldowns.put(damagedName, System.currentTimeMillis() + (10 * 1000));

				Frozen.remove(damagedName);
				frozenTeam.removePlayer(damaged);
				runnerTeam.addPlayer(damaged);
				e.setCancelled(true);
				damaged.getWorld().playSound(damaged.getLocation(), Sound.BLOCK_GLASS_BREAK, 10, 1);
				e.getEntity().sendMessage(ChatColor.GOLD + "You are now unfrozen!");
				Bukkit.getServer().broadcastMessage(ChatColor.GREEN + damagedName + ChatColor.GOLD
						+ " has been unfrozen by " + ChatColor.GREEN + damagerName + ChatColor.GOLD + "!");
			}
		}
	}

	@EventHandler
	public void onDragonDeath(EntityDeathEvent event)
	{
		if (event.getEntity() instanceof EnderDragon)
		{
			if (start & !runnersWin)
			{
				for (int i = 0; i < Taggers.size(); i++)
				{
					Player p = Bukkit.getPlayer(Taggers.get(i));

					taggerTeam.removePlayer(Bukkit.getPlayer(Taggers.get(i)));
					objective.getScoreboard().resetScores(Bukkit.getPlayer(Taggers.get(i)));

					p.sendTitle(ChatColor.GREEN + "Runners win!", null);
				}

				for (int i = 0; i < Runners.size(); i++)
				{
					Player p = Bukkit.getPlayer(Runners.get(i));

					runnerTeam.removePlayer(Bukkit.getPlayer(Runners.get(i)));
					objective.getScoreboard().resetScores(Bukkit.getPlayer(Runners.get(i)));

					p.sendTitle(ChatColor.GREEN + "Runners win!", null);
				}

				for (int i = 0; i < Frozen.size(); i++)
				{
					frozenTeam.removePlayer(Bukkit.getPlayer(Frozen.get(i)));
					objective.getScoreboard().resetScores(Bukkit.getPlayer(Frozen.get(i)));
				}

				Taggers.clear();
				Runners.clear();
				Frozen.clear();

				Bukkit.broadcastMessage(
						ChatColor.AQUA + "" + ChatColor.BOLD + "Freeze Tag: " + ChatColor.GREEN + "Runners win!");
				runnersWin = true;
				start = false;
			}
		}
	}
}
