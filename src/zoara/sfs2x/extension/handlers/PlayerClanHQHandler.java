package zoara.sfs2x.extension.handlers;

import java.util.EnumSet;
import zoara.sfs2x.extension.ZoaraExtension;
import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.ISFSApi;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.SFSRoomSettings;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class PlayerClanHQHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Player wants to go to Clan HQ: " + user.getId());
		
    	World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		String clanName = player.getClanName();		
		if (clanName.length() <= 1)
			return;

		player.setInClan(true);
		player.resetInClanTransform();
		
		updateOthers(user, player);
		sendInventory(user, player, player.useUDP);
		sendQuestLog(user, player, player.useUDP);
		
		// Join the user
		Room clanHQ = getParentExtension().getParentZone().getRoomByName(clanName);
		if (clanHQ == null)
		{
			try {
				createClanRoom(getApi(), getParentExtension().getParentZone(), clanName);
			} catch (SFSCreateRoomException e) {
				e.printStackTrace();
				return;
			}
		}
		
		try {
			getApi().joinRoom(user, clanHQ);
			user.subscribeGroup(clanName);
		} catch (SFSJoinRoomException e) {
			e.printStackTrace();
			return;
		}
		trace("Joining clan room...");
		
		user.setPrivilegeId((short) 1);
    }

	// Send the transform to all the clients
	private void updateOthers(User fromUser, ActivePlayer player) 
	{
		ISFSObject data = new SFSObject();
		data.putBool("inClan", player.isInClan());
		data.putInt("id", player.getID());

		//List<User> userList = UserHelper.getAllUsersList(getParentExtension().getParentZone(), fromUser);
		//this.send("playerInClan", data, userList, true); // Use UDP = true
		((ZoaraExtension) getParentExtension()).sendAll("playerInClan", data, fromUser);
	}
	
	public static Room createClanRoom(ISFSApi api, Zone zone, String clanName) throws SFSCreateRoomException
	{
		CreateRoomSettings params = new CreateRoomSettings();
		params.setName(clanName);
		params.setGame(false);
		params.setGroupId(clanName);
		params.setMaxUsers(5);
		params.setAutoRemoveMode(SFSRoomRemoveMode.NEVER_REMOVE);
		params.setHidden(true);
		params.setUseWordsFilter(true);
		params.setRoomSettings(EnumSet.of(
				SFSRoomSettings.PUBLIC_MESSAGES, SFSRoomSettings.USER_COUNT_CHANGE_EVENT, 
				SFSRoomSettings.USER_ENTER_EVENT, SFSRoomSettings.USER_EXIT_EVENT));
		Room clanRoom = api.createRoom(zone, params, null);
		return clanRoom;
	}
	
	/*public static void addClanBuddies(ISFSApi api, Zone zone, User user, World world)
	{
		ActivePlayer player = world.getPlayer(user);
		int clanID = player.getClanID();
		
		List<User> allUsers = UserHelper.getAllUsersList(zone, user);
		for (User _u : allUsers)
		{
			ActivePlayer _p = world.getPlayer(_u);
			int _c = _p.getClanID();
			if (_c == clanID)
				user.
		}
	}*/
	
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
