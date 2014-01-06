package zoara.sfs2x.extension.social;

import java.sql.Timestamp;
import java.util.Date;

import zoara.sfs2x.extension.ZoaraExtension;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class PublicMessageEventHandler extends BaseServerEventHandler
{
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException
	{		
		Room currentRoom = (Room) event.getParameter(SFSEventParam.ROOM);
		User user = (User) event.getParameter(SFSEventParam.USER);
		String message = (String) event.getParameter(SFSEventParam.MESSAGE);
		
		trace("[CHAT] [" + currentRoom.getName() + "] " + user.getName() + ": " + message);
		
		Date date = new Date();
		Timestamp time = new Timestamp(date.getTime());
		
		Chat newChat = new Chat(currentRoom.getName(), user.getName(), message, time);
		((ZoaraExtension) getParentExtension()).addToChatLog(newChat);
	}
}