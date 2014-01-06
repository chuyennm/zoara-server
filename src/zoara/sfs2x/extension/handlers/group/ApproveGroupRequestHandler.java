package zoara.sfs2x.extension.handlers.group;

import java.util.HashSet;
import java.util.Set;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.Group;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

@SuppressWarnings("unused")
public class ApproveGroupRequestHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Received player group approval request: " + user.getId());
    	
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
		
		/*if (player.isInGroup())
		{
			Group group = player.getGroup();
			group.addPlayer(otherPlayer);
		}
		else if (otherPlayer.isInGroup())
		{
			Group group = otherPlayer.getGroup();
			group.addPlayer(player);
		}
		else if (player.getPendingGroup().size() > 2) // we have more than just the 2 of us
		{*/
			Group group = world.createGroup();
			group.addPlayer(player);
			group.addPlayer(otherPlayer);		
		/*}
		else
		{
			Set<ActivePlayer> players = new HashSet<ActivePlayer>();
			players.add(player);
			players.add(otherPlayer);
			
			if (!world.upgradeGroup(players))
			{
				if (player.getPendingGroup().size() > 2)
					player.getPendingGroup().removePlayer(otherPlayer);
				else
					player.setPendingGroup(null);
				notifyFailure(user, otherPlayerID);
				return;			
			}
		}*/
		
		updateOther(user, otherPlayer.getSfsUser(), otherPlayer.useUDP);
    }

	private void updateOther(User fromUser, User toUser, boolean useUDP) 
	{
		ISFSObject data = new SFSObject();
		data.putInt("OtherPlayerID", fromUser.getId());
		this.send("approvedGroupRequest", data, toUser, useUDP);
	}
	
	private void notifyFailure(User fromUser, int id, boolean useUDP)
	{
		ISFSObject data = new SFSObject();
		data.putInt("OtherPlayerID", id);
		this.send("groupRequestApprovalFailed", data, fromUser, useUDP);
	}
}
