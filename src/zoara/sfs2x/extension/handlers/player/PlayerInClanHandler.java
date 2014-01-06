package zoara.sfs2x.extension.handlers.player;

import zoara.sfs2x.extension.ZoaraExtension;
import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class PlayerInClanHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Received player back from clan HQ request: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		player.setInClan(data.getBool("inClan"));
		
		updateOthers(user, player);
    }

	// Send the transform to all the clients
	private void updateOthers(User fromUser, ActivePlayer player) 
	{
		ISFSObject data = new SFSObject();
		data.putInt("id", player.getID());
		data.putBool("inClan", player.isInClan());
		player.transform.toSFSObject(data);

		//List<User> userList = UserHelper.getAllUsersList(getParentExtension().getParentZone(), fromUser);
		//this.send("playerInClan", data, userList, true); // Use UDP = true
		((ZoaraExtension) getParentExtension()).sendAll("playerInClan", data, fromUser);
	}
}