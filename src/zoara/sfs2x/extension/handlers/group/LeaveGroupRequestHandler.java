package zoara.sfs2x.extension.handlers.group;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.Group;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class LeaveGroupRequestHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Received player leave group request: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		Group group = player.getGroup();
		if (group == null || !group.removePlayer(player))
			notifyFailure(user, player.useUDP);
		else
			notifySuccess(user, player.useUDP);
    }
	
	private void notifyFailure(User user, boolean useUDP)
	{
		ISFSObject data = new SFSObject();
		this.send("leaveGroupFailed", data, user, useUDP);
	}
	
	private void notifySuccess(User user, boolean useUDP)
	{
		ISFSObject data = new SFSObject();
		this.send("leaveGroupSuccess", data, user, useUDP);
	}
}

