package zoara.sfs2x.extension.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import zoara.sfs2x.extension.db.ItemDBHandler;
import zoara.sfs2x.extension.simulation.item.InventoryItem;
import zoara.sfs2x.extension.simulation.item.Item;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class Inventory 
{
	private ActivePlayer player;
	
	private HashMap<Integer,InventoryItem> inventory = new HashMap<Integer,InventoryItem>();
	private HashSet<InventoryItem> itemsToAdd = new HashSet<InventoryItem>();
	private HashSet<InventoryItem> itemsToUpdate = new HashSet<InventoryItem>();
	private HashSet<InventoryItem> itemsToRemove = new HashSet<InventoryItem>();

	public Inventory(ActivePlayer _player)
	{
		player = _player;
	}
	
	public int size()
	{
		return inventory.size();
	}
	
	public InventoryItem getItem(int inventoryIndex)
	{
		return inventory.get(inventoryIndex);
	}
	
	public InventoryItem getItem(String uniqueID)
	{
		Collection<InventoryItem> items = inventory.values();
		for (InventoryItem item : items)
		{
			if (item.getUniqueID() == uniqueID)
				return item;
		}
		return null;
	}
	
	public InventoryItem addItem(int inventoryIndex)
	{
		return addItem(inventoryIndex, false);
	}
	
	public InventoryItem addItem(int inventoryIndex, boolean fromDB)
	{
		InventoryItem newItem = new InventoryItem(player);
		inventory.put(inventoryIndex, newItem);
		newItem.setInventoryIndex(inventoryIndex);
		if (itemsToRemove.contains(newItem)) itemsToRemove.remove(newItem);
		if (fromDB) { itemsToUpdate.add(newItem); }
		else { itemsToAdd.add(newItem); }
		player.checkGroup();
		return newItem;
	}
	
	public boolean updateItem(int inventoryIndex, int newQuantity)
	{
		InventoryItem item = inventory.get(inventoryIndex);
		if (item == null) return false;
		item.setQuantity(newQuantity);
		if (!itemsToAdd.contains(item)) itemsToUpdate.add(item);
		player.checkGroup();
		return true;
	}
	
	public boolean removeItem(int inventoryIndex)
	{
		InventoryItem item = inventory.remove(inventoryIndex);
		if (item == null) return false;
		itemsToRemove.add(item);
		if (itemsToAdd.contains(item)) itemsToAdd.remove(item);
		if (itemsToUpdate.contains(item)) itemsToUpdate.remove(item);
		player.checkGroup();
		return true;
	}
	
	public boolean contains(Item item)
	{
		return contains (item, 1);
	}
	
	public boolean contains(Item item, int quantity)
	{
		Collection<InventoryItem> items = inventory.values();
		int foundQuantity = 0;
		for (InventoryItem i : items)
		{
			if (!item.getUniqueID().equals(i.getUniqueID())) continue;
			foundQuantity += i.getQuantity();
			if (foundQuantity >= quantity) return true;
		}
		return false;
	}
	
	public int containsNum(Item item)
	{
		Collection<InventoryItem> items = inventory.values();
		int foundQuantity = 0;
		for (InventoryItem i : items)
		{
			if (!item.getUniqueID().equals(i.getUniqueID())) continue;
			foundQuantity += i.getQuantity();
		}
		return foundQuantity;
	}
	
	public void toSFSArray(ISFSArray data)
	{
		Collection<InventoryItem> inventoryItems = inventory.values();
		for (InventoryItem item : inventoryItems)
		{
			ISFSObject itemData = new SFSObject();
			item.toSFSObject(itemData);
			data.addSFSObject(itemData);
		}
	}
	
	public void updateDB(SFSExtension extension)
	{
		if (itemsToAdd.size() > 0)
		{
			for (InventoryItem item : itemsToAdd)
				ItemDBHandler.addInventoryItem(extension, item);
		}
		if (itemsToUpdate.size() > 0)
			ItemDBHandler.updateInventoryItems(extension, new ArrayList<InventoryItem>(itemsToUpdate));
		if (itemsToRemove.size() > 0)
			ItemDBHandler.removeInventoryItems(extension, new ArrayList<InventoryItem>(itemsToRemove));
	}
}
