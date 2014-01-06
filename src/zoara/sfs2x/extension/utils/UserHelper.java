package zoara.sfs2x.extension.utils;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;

import java.util.ArrayList;
import java.util.List;

// Helper methods to easily get socket channel list to send response message to clients
public class UserHelper {


	public static List<User> getRecipientsList(Room room, User exceptUser) {
		List<User> users = room.getUserList();
		if (exceptUser != null) {
			users.remove(exceptUser);
		}
		return users;
	}

	public static List<User> getRecipientsList(Room currentRoom) {
		return getRecipientsList(currentRoom, null);
	}

	public static List<User> getAllUsersList(Zone currentZone, User exceptUser) {
		List<User> users = new ArrayList<User>(currentZone.getUserList());
		if (exceptUser != null) {
			users.remove(exceptUser);
		}
		return users;
	}
	
	public static List<User> getAllUsersList(Zone currentZone) {
		return getAllUsersList(currentZone, null);
	}
}
