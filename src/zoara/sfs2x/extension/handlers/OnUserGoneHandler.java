package zoara.sfs2x.extension.handlers;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

import zoara.sfs2x.extension.ZoaraExtension;
import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

public class OnUserGoneHandler extends BaseServerEventHandler 
{
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException 
	{
		User user = (User) event.getParameter(SFSEventParam.USER);
		//trace("Received user disconnect request: " + user.getId());
		World world = RoomHelper.getWorld(this);
		
		updateOthers(user);
		
		ActivePlayer player = world.userLeft(user);		
		if (player == null)
			return; // we can't do anything more here
		
		player.disconnect(getParentExtension());
		
		player = null;
    }

	// Send the transform to all the clients
	private void updateOthers(User fromUser) 
	{
		ISFSObject data = new SFSObject();
		data.putInt("id", fromUser.getId());

		//List<User> userList = UserHelper.getAllUsersList(getParentExtension().getParentZone(), fromUser);
		//if (userList != null && userList.size() > 0) {
		((ZoaraExtension) getParentExtension()).sendAll("removePlayer", data, fromUser);
		//}
	}
}
