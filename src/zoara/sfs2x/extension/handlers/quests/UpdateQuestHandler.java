package zoara.sfs2x.extension.handlers.quests;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class UpdateQuestHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Updating quest for: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		String uniqueID = data.getUtfString("UniqueQuestID");
		//boolean active = data.getBool("Active");
		boolean completed = data.getBool("Completed");
		if (data.containsKey("CurrentStepNumber")) 
		{
			int currentStepNumber = data.getInt("CurrentStepNumber");
			boolean failed = data.getBool("Failed");
			player.quests.updateQuest(uniqueID, completed, failed, currentStepNumber);
		}
		else if (data.containsKey("Failed"))
		{
			boolean failed = data.getBool("Failed");
			player.quests.updateQuest(uniqueID, completed, failed);
		}	
		else
			player.quests.updateQuest(uniqueID, completed);
			
    }
}
