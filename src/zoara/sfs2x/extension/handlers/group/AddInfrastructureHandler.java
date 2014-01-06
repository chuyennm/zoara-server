package zoara.sfs2x.extension.handlers.group;

import zoara.sfs2x.extension.ZoaraExtension;
import zoara.sfs2x.extension.db.InfrastructureDBHandler;
import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.Group;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.simulation.item.Infrastructure;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class AddInfrastructureHandler extends BaseClientRequestHandler
{
    @Override
    public void handleClientRequest(User user, ISFSObject data)
    {
    	trace("Received player add infrastructure request: " + user.getId());
    	
		World world = RoomHelper.getWorld(this);
		ActivePlayer player = world.getPlayer(user);
		if (player == null)
			return;
		
		if (!player.isInGroup())
			return;
		
		Group group = player.getGroup();
		if (group.getChosenPlayer() != player)
			notifyFailure(user, player.useUDP);
		
		int templateID = data.getInt("TemplateID");
		Infrastructure newInfrastructure = world.addInfrastructure(templateID);
		newInfrastructure.transform.setPosition(
				data.getFloat("PositionX"), data.getFloat("PositionY"), data.getFloat("PositionZ"));
		newInfrastructure.transform.setRotation(
				data.getFloat("RotationX"), data.getFloat("RotationY"), data.getFloat("RotationZ"));
		
		group.buildInfrastructure(templateID);
		updateEveryone(newInfrastructure);
		InfrastructureDBHandler.addInfrastructure(getParentExtension(), newInfrastructure);
    }
	
	private void notifyFailure(User fromUser, boolean useUDP)
	{
		ISFSObject data = new SFSObject();
		this.send("buildingInfrastructureFailed", data, fromUser, useUDP);
	}
	
	private void updateEveryone(Infrastructure newInfrastructure)
	{
		ISFSObject data = new SFSObject();
		newInfrastructure.toSFSObject(data);

		((ZoaraExtension) getParentExtension()).sendAll("newInfrastructure", data);
	}
}
