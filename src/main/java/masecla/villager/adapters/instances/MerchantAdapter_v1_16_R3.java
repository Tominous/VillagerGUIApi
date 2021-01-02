package masecla.villager.adapters.instances;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMerchantCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import masecla.villager.adapters.BaseAdapter;
import masecla.villager.classes.VillagerInventory;
import masecla.villager.classes.VillagerTrade;
import masecla.villager.events.VillagerInventoryCloseEvent;
import masecla.villager.events.VillagerInventoryModifyEvent;
import masecla.villager.events.VillagerInventoryOpenEvent;
import masecla.villager.events.VillagerTradeCompleteEvent;

public class MerchantAdapter_v1_16_R3 extends BaseAdapter implements Listener {

	private CraftMerchantCustom wrapped = null;

	public MerchantAdapter_v1_16_R3(VillagerInventory toAdapt) {
		super(toAdapt);
		// TODO: Get rid of this magical value here somehow
		Bukkit.getServer().getPluginManager().registerEvents(this,
				Bukkit.getPluginManager().getPlugin("VillagerGUIApi"));
		wrapped = new CraftMerchantCustom(toAdapt.getName());
		wrapped.setRecipes(toNMSRecipes());
	}

	@Override
	public void openFor(Player p) {
		p.openMerchant(wrapped, true);
		VillagerInventoryOpenEvent event = new VillagerInventoryOpenEvent(toAdapt, p);
		Bukkit.getPluginManager().callEvent(event);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer().getUniqueId().equals(this.toAdapt.getForWho().getUniqueId())) {
			VillagerInventoryCloseEvent closeEvent = new VillagerInventoryCloseEvent(toAdapt,
					(Player) event.getPlayer());
			Bukkit.getPluginManager().callEvent(closeEvent);
			HandlerList.unregisterAll(this); // Kill this event listener
		}
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (event.getWhoClicked().getUniqueId().equals(this.toAdapt.getForWho().getUniqueId())) {
			VillagerInventoryModifyEvent modifyEvent = new VillagerInventoryModifyEvent(toAdapt,
					(Player) event.getWhoClicked(), event.getCurrentItem());
			Bukkit.getPluginManager().callEvent(modifyEvent);
			if (event.getRawSlot() == -999)
				return;
			if (event.getRawSlot() == 2 && !event.getCurrentItem().getType().equals(Material.AIR)) {
				ItemStack itemOne = this.toAdapt.getForWho().getOpenInventory().getTopInventory().getItem(0);
				ItemStack itemTwo = this.toAdapt.getForWho().getOpenInventory().getTopInventory().getItem(1);
				ItemStack result = event.getCurrentItem();
				VillagerTradeCompleteEvent completeEvent = new VillagerTradeCompleteEvent(toAdapt,
						(Player) event.getWhoClicked(), new VillagerTrade(itemOne, itemTwo, result, -1));
				Bukkit.getPluginManager().callEvent(completeEvent);
			}
		}
	}

	public List<MerchantRecipe> toNMSRecipes() {
		List<MerchantRecipe> result = new ArrayList<MerchantRecipe>();
		for (VillagerTrade trd : this.toAdapt.getTrades()) {
			MerchantRecipe toAdd = new MerchantRecipe(trd.getResult(), trd.getMaxUses());
			toAdd.addIngredient(trd.getItemOne());
			if (trd.requiresTwoItems())
				toAdd.addIngredient(trd.getItemTwo());
			result.add(toAdd);
		}

		return result;
	}

}
