package zoara.sfs2x.extension.handlers.items;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.simulation.item.InventoryItem;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class NewInventoryItemHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Adding new inventory item for: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		InventoryItem item = player.inventory.addItem(data.getInt("InventoryIndex"));
		item.setTemplateID(data.getInt("TemplateID"));
		item.setUniqueID(data.getUtfString("UniqueItemID"));
		//item.setInventoryIndex(data.getInt("InventoryIndex"));
		item.setQuantity(data.getInt("Quantity"));
		item.setNew(true);
    }
}