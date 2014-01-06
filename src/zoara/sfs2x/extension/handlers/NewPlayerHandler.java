package zoara.sfs2x.extension.handlers;

import zoara.sfs2x.extension.ZoaraExtension;
import zoara.sfs2x.extension.db.ItemDBHandler;
import zoara.sfs2x.extension.db.QuestDBHandler;
import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class NewPlayerHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Received new player request: " + user.getId());
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.addPlayer(user);
		
		UserVariable uv_dbId = new SFSUserVariable("dbid", 
				user.getSession().getProperty(ZoaraExtension.DATABASE_ID), true);
		player.setDBID(uv_dbId.getIntValue());
		
		player.setName(data.getUtfString("name"));
		player.setLevel(data.getInt("level"));
		player.setEXP(data.getLong("EXP"));
		player.transform.setPosition(data.getFloat("px"), data.getFloat("py"), data.getFloat("pz"));
		player.transform.setRotation(data.getFloat("rx"), data.getFloat("ry"), data.getFloat("rz"));
		if (data.getUtfString("config") != null) { player.setConfig(data.getUtfString("config")); }
		if (data.getInt("clan") != null) { player.setClanID(data.getInt("clan")); }
		if (data.getUtfString("clanName") != null) { player.setClanName(data.getUtfString("clanName")); }
		if (data.getInt("skill") != null) { player.setSkill(data.getInt("skill")); }
		
		if (user.containsVariable("useUDP")) {
			player.useUDP = user.getVariable("useUDP").getBoolValue();
		}
		
		ISFSObject blank = new SFSObject();
		this.send("playerCreateAccepted", blank, user, player.useUDP);
		updateOthers(user, player);
		
		sendWorldData(user, world, player.useUDP);
		
		ItemDBHandler.getPlayerInventory(getParentExtension(), player);
		sendInventory(user, player, player.useUDP);
		
		QuestDBHandler.getPlayerQuests(getParentExtension(), player);
		sendQuestLog(user, player, player.useUDP);
    }

	private void updateOthers(User fromUser, ActivePlayer player) 
	{
		ISFSObject data = new SFSObject();

		player.toSFSObject(data);

		//List<User> userList = UserHelper.getAllUsersList(getParentExtension().getParentZone(), fromUser);
		//this.send("spawnPlayer", data, userList, true); // Use UDP = true
		((ZoaraExtension) getParentExtension()).sendAll("spawnPlayer", data, fromUser);
	}
	
	private void sendWorldData(User user, World world, boolean useUDP)
	{
		ISFSObject data = new SFSObject();
		world.toSFSObject(data, user);
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