package zoara.sfs2x.extension.handlers.quests;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.Quest;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class NewQuestHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Adding new quest for: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		Quest quest = player.quests.addQuest(data.getUtfString("UniqueQuestID"));
		quest.setTemplateID(data.getInt("TemplateID"));
		quest.setUniqueID(data.getUtfString("UniqueQuestID"));
		//quest.setActive(data.getBool("Active"));
		if (data.containsKey("Completed"))
			quest.setCompleted(data.getBool("Completed"));
		if (data.containsKey("Failed"))
			quest.setFailed(data.getBool("Failed"));
		if (data.containsKey("CurrentStepNumber"))
			quest.setCurrentStepNumber(data.getInt("CurrentStepNumber"));
    }
}
