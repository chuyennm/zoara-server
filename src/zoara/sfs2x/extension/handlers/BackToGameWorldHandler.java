package zoara.sfs2x.extension.handlers;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class BackToGameWorldHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Player wants to go back to game world: " + user.getId());
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		sendWorldData(user, world, player.useUDP);
		sendInventory(user, player, player.useUDP);
		sendQuestLog(user, player, player.useUDP);
    }
	
	private void sendWorldData(User user, World world, boolean useUDP)
	{
		ISFSObject data = new SFSObject();
		world.objectsToSFSObject(data, user);
		if (data.size() < 1) return;
		this.send("worldData", data, user, useUDP);
		trace("Sent world data to player!");
	}
	
	private void sendInventory(User user, ActivePlayer player, boolean useUDP)
	{
		ISFSArray inventory = new SFSArray();
		player.inventory.toSFSArray(inventory);
		if (inventory.size() < 1) return;
		ISFSObject data = new SFSObject();
		data.putSFSArray("items", inventory);
		this.send("inventory", data, user, useUDP);
		trace("Sent player their inventory information! Size: " + inventory.size());
	}
	
	private void sendQuestLog(User user, ActivePlayer player, boolean useUDP)
	{
		ISFSArray quests = new SFSArray();
		player.quests.toSFSArray(quests);
		if (quests.size() < 1) return;
		ISFSObject data = new SFSObject();
		data.putSFSArray("quests", quests);
		this.send("questLog", data, user, useUDP);
		trace("Sent player their quest log! Size: " + quests.size());
	}
}
