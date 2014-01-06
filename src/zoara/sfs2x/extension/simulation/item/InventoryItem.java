package zoara.sfs2x.extension.simulation.item;

import zoara.sfs2x.extension.simulation.ActivePlayer;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class InventoryItem extends Item 
{
	private ActivePlayer player;
	
	private int inventoryIndex = 0;
	private int quantity = 1;
	private boolean isNew = true; // it has not yet been added to the database
	
	public InventoryItem(ActivePlayer _player)
	{
		player = _player;
	}
	
	public ActivePlayer getPlayer()
	{
		return player;
	}
	
	public int getInventoryIndex()
	{
		return inventoryIndex;
	}
	
	public void setInventoryIndex(int _inventoryIndex)
	{
		inventoryIndex = _inventoryIndex;
	}
	
	public int getQuantity()
	{
		return quantity;
	}
	
	public void setQuantity(int _quantity)
	{
		quantity = _quantity;
	}
	
	public boolean isNew()
	{
		return isNew;
	}
	
	public void setNew(boolean _isNew)
	{
		isNew = _isNew;
	}
	
	public void toSFSObject(ISFSObject data)
	{
		super.toSFSObject(data);
		data.putInt("InventoryIndex", inventoryIndex);
		data.putInt("Quantity", quantity);
	}
}
