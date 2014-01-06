package zoara.sfs2x.extension.handlers;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class UserUDPHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("User UDP init: " + user.getId());
    	
		UserVariable uv_udp = new SFSUserVariable("useUDP", true, true);
		try {
			user.setVariable(uv_udp);
		} catch (SFSVariableException e) {
			trace(e.getMessage());
		}
		
    	World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;		
		player.useUDP = true;
    }
}
