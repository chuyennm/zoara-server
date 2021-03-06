package zoara.sfs2x.extension.handlers.group;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.Group;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class RecipeVoteRequestHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Received player recipe vote request: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		if (!player.isInGroup())
			return;
		
		Group group = player.getGroup();
		if (!group.addVote(player.getID(), data.getInt("TemplateID")))
			notifyFailure(user, player.useUDP);
    }
	
	private void notifyFailure(User fromUser, boolean useUDP)
	{
		trace("Something went wrong and the vote was not received from: " + fromUser.getId());
		ISFSObject data = new SFSObject();
		this.send("recipeVoteFailed", data, fromUser, useUDP);
	}
}
