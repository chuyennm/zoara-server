package zoara.sfs2x.extension.handlers.player;

import java.util.Collection;

import zoara.sfs2x.extension.ZoaraExtension;
import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.Transform;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

@SuppressWarnings("unused")
public class PlayerTransformHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		if (player.isInClan())
		{
	    	//trace("Received player in clan transform update request: " + user.getId());
	    	
			//Transform oldTransform = player.inClanTransform.clone();
			player.inClanTransform.setPosition(
					data.getFloat("px"), data.getFloat("py"), data.getFloat("pz"));
			player.inClanTransform.setRotation(
					data.getFloat("rx"), data.getFloat("ry"), data.getFloat("rz"));
			
			Collection<ActivePlayer> players = world.getPlayers();
			//if (!player.inClanTransform.equals(oldTransform))
				for (ActivePlayer otherPlayer : players)
					if (otherPlayer.getClanID() == player.getClanID())
						updateInClan(player, otherPlayer);			
		}
		else
		{
	    	//trace("Received player transform update request: " + user.getId());
			//Transform oldTransform = player.transform.clone();
			player.transform.setPosition(
					data.getFloat("px"), data.getFloat("py"), data.getFloat("pz"));
			player.transform.setRotation(
					data.getFloat("rx"), data.getFloat("ry"), data.getFloat("rz"));
			
			/*if (player.transform.equals(oldTransform))
				trace("Transform was not different from the old one!");*/

			updateOthers(user, player);
		}
    }

	// Send the transform to all the clients
	private void updateOthers(User fromUser, ActivePlayer player) 
	{
		ISFSObject data = new SFSObject();
		data.putInt("id", player.getID());
		player.transform.toSFSObject(data);

		//List<User> userList = UserHelper.getAllUsersList(getParentExtension().getParentZone(), fromUser);
		//this.send("playerTransform", data, userList, true); // Use UDP = true
		((ZoaraExtension) getParentExtension()).sendAll("playerTransform", data, fromUser);
	}

	// Send the transform to all the clients
	private void updateInClan(ActivePlayer fromPlayer, ActivePlayer toPlayer) 
	{
		if (!toPlayer.getSfsUser().isConnected()) return;
		ISFSObject data = new SFSObject();
		data.putInt("id", fromPlayer.getID());
		fromPlayer.inClanTransform.toSFSObject(data);
		this.send("playerTransform", data, toPlayer.getSfsUser(), toPlayer.useUDP);
	}
}