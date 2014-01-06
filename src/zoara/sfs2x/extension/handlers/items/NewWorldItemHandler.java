package zoara.sfs2x.extension.handlers.items;

import zoara.sfs2x.extension.db.ItemDBHandler;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.simulation.item.WorldItem;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class NewWorldItemHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Adding new world item...");
    	
		World world = RoomHelper.getWorld(this);
		
		String uniqueID = data.getUtfString("UniqueItemID");
		WorldItem worldItem = world.addWorldItem(uniqueID);		
		worldItem.setTemplateID(data.getInt("TemplateID"));
		worldItem.setUniqueID(uniqueID);
		worldItem.transform.setPosition(
				data.getFloat("PositionX"), data.getFloat("PositionY"), data.getFloat("PositionZ"));
		worldItem.transform.setRotation(
				data.getFloat("RotationX"), data.getFloat("RotationY"), data.getFloat("RotationZ"));
		worldItem.setGrabbable(data.getBool("Grabbable"));
		worldItem.setSkillItem(data.getBool("Skill"));
		
		if (data.getBool("UpdateDB"))
			ItemDBHandler.addWorldItem(getParentExtension(), worldItem);
    }
}