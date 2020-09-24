package dev.reproachful.tappedlol.backpacks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class BackpackListener implements Listener {

    private final Backpacks core;

    public BackpackListener(Backpacks core) {
        this.core = core;
    }

    @EventHandler
    public void open(PlayerInteractEvent event) throws IOException {
        Player player = event.getPlayer();
        ItemStack backpack = event.getItem();

        if (backpack == null) {
            return;
        }

        if (core.isBackpack(backpack)) {
            Inventory inv = Bukkit.createInventory(null, core.backpackSize(backpack), "§0Backpack");
            inv.setContents(core.getBackpackContents(backpack));
            player.openInventory(inv);
        }
    }

    @EventHandler
    public void close(InventoryCloseEvent event) {
        InventoryView view = event.getView();
        if (view.getTitle().equals("§0Backpack")) {
            Player player = (Player) event.getPlayer();
            ItemStack bp = player.getInventory().getItemInMainHand();
            ItemStack newBP = core.updateBackpack(bp, event.getView().getTopInventory().getContents());
            player.getInventory().setItemInMainHand(newBP);
        }
    }

    @EventHandler
    public void move(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§0Backpack")) {
            ItemStack cursor = event.getCursor();
            ItemStack item = event.getCurrentItem();
            if (event.getClick() == ClickType.NUMBER_KEY) {
                event.setCancelled(true);
                return;
            }
            if (item == null || cursor == null) {
                return;
            }
            if (core.isBackpack(item)) {
                event.setCancelled(true);
            }
            if (core.isBackpack(cursor)) {
                event.setCancelled(true);
            }
        }
    }
}
