package zoara.sfs2x.extension.handlers.items;

import zoara.sfs2x.extension.db.ItemDBHandler;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.simulation.item.WorldItem;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class DestroyWorldItemHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Destroying world item...");
    	
		World world = RoomHelper.getWorld(this);
		
		String uniqueID = data.getUtfString("UniqueItemID");
		WorldItem worldItem = world.destroyWorldItem(uniqueID);
		
		ItemDBHandler.destroyWorldItem(getParentExtension(), worldItem);
    }
}
