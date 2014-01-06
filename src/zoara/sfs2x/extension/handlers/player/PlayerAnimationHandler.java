package zoara.sfs2x.extension.handlers.player;

import zoara.sfs2x.extension.ZoaraExtension;
import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class PlayerAnimationHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	//trace("Received player animation change request: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		player.setWalking(data.getBool("walking"));
		
		updateOthers(user, player);
    }

	private void updateOthers(User fromUser, ActivePlayer player) 
	{
		ISFSObject data = new SFSObject();
		data.putBool("walking", player.isWalking());
		data.putInt("id", player.getID());

		//List<User> userList = UserHelper.getAllUsersList(getParentExtension().getParentZone(), fromUser);
		//this.send("playerAnimation", data, userList, true); // Use UDP = true
		((ZoaraExtension) getParentExtension()).sendAll("playerAnimation", data, fromUser);
	}
}