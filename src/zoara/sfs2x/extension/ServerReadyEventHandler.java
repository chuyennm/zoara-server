package zoara.sfs2x.extension;

import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class ServerReadyEventHandler extends BaseServerEventHandler
{
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException
	{
		World world = RoomHelper.getWorld(this);
		
		if (world.size() == 0) {
			world.loadItems();
		}
	}
}