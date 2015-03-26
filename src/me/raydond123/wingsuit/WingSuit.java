package me.raydond123.wingsuit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class WingSuit extends JavaPlugin implements Listener
{
    private List<String> toggled = new ArrayList();
    private File configFile;
    private YamlConfiguration config;
    private double fp = 0.2D;
    private double um = 0.08D;

    public void onEnable()
    {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        fp = (config.getDouble("forwardPercent", 20.0D) / 100.0D);
        um = config.getDouble("upwardMotion", 0.08D);

        List<String> sl = config.getStringList("toggled");
        if ((sl != null) && (!sl.isEmpty())) {
            toggled = sl;
        }

        Bukkit.getPluginManager().registerEvents(this, this);

    }

    public void onDisable()
    {
        config.set("toggled", toggled);
        try
        {
            config.save(configFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent e)
    {
        Player p = e.getPlayer();

        if ((!toggled.contains(p.getName())) && (moveEventCalculated(e))) {

            p.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(fp).setY(um));

        }
    }

    private boolean moveEventCalculated(PlayerMoveEvent e)
    {
        Location l0 = e.getFrom();
        Location l1 = e.getTo();

        Location d1 = l0.getBlock().getLocation().clone().add(0.0D, -1.0D, 0.0D);
        Location d2 = l0.getBlock().getLocation().clone().add(0.0D, -2.0D, 0.0D);
        if (l1.getY() < l0.getY())
        {
            if ((d1.getBlock().getTypeId() == 0) && (d2.getBlock().getTypeId() == 0)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public void toggleGlide(Player player) {

        if(toggled.contains(player.getName())) {

            toggled.remove(player.getName());
            player.sendMessage(ChatColor.BLUE + "" +ChatColor.BOLD + "You have opened your wingsuit.");

        } else {

            toggled.add(player.getName());
            player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "You have closed your wingsuit.");

        }

    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        ItemStack held = player.getItemInHand();

        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if(held.getItemMeta().getDisplayName().contains("Wing Suit") && held.getType() == Material.STICK) {

                toggleGlide(player);

            }

        }

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (label.equalsIgnoreCase("getwingsuit")) {
            if (!(sender instanceof Player))
            {
                sender.sendMessage("Must be a player to use this command.");
            }
            else {

                Player player = (Player) sender;

                ItemStack wingsuit = new ItemStack(Material.STICK);
                ItemMeta wingMeta = wingsuit.getItemMeta();
                wingMeta.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + "Wing Suit");
                wingsuit.setItemMeta(wingMeta);

                player.getInventory().addItem(wingsuit);

            }
        }
        return false;
    }
}
