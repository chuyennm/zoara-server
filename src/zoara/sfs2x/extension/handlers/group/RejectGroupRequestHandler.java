package zoara.sfs2x.extension.handlers.group;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class RejectGroupRequestHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Received player group rejection request: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		int otherPlayerID = data.getInt("OtherPlayerID");
		ActivePlayer otherPlayer = world.getPlayer(otherPlayerID);
		if (otherPlayer == null)
		{
			notifyFailure(user, otherPlayerID, player.useUDP);
			return;
		}
		
		if (!player.getPendingGroup().removePlayer(player))
		{
			notifyFailure(user, otherPlayerID, player.useUDP);
			return;			
		}
		
		updateOther(user, otherPlayer.getSfsUser(), otherPlayer.useUDP);
    }

	private void updateOther(User fromUser, User toUser, boolean useUDP) 
	{
		ISFSObject data = new SFSObject();
		data.putInt("OtherPlayerID", fromUser.getId());
		this.send("rejectedGroupRequest", data, toUser, useUDP);
	}
	
	private void notifyFailure(User fromUser, int id, boolean useUDP)
	{
		ISFSObject data = new SFSObject();
		data.putInt("OtherPlayerID", id);
		this.send("groupRequestRejectFailed", data, fromUser, useUDP);
	}
}