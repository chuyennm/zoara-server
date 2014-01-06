package zoara.sfs2x.extension.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class PendingGroup
{
	protected World world;
	protected HashSet<ActivePlayer> players = new HashSet<ActivePlayer>();
	
	public PendingGroup(World _world) 
	{
		world = _world;
	}
	
	public boolean addPlayer(ActivePlayer player) 
	{
		if (player.isInPendingGroup())
		{
			PendingGroup otherGroup = player.getPendingGroup();
			merge(otherGroup);
		}
		else
		{
			players.add(player);
			player.setPendingGroup(this);
		}
		return true;
	}
	
	public boolean addUser(User user) 
	{
		return addPlayer(world.getPlayer(user));
	}

	public boolean removePlayer(ActivePlayer player)
	{
		boolean result = players.remove(player);
		if (result) player.setPendingGroup(null);
		if (size() <= 1)
		{
			notifyDisbanding();
			world.removePendingGroup(this);
		}
		return result;
	}
	
	public boolean removeUser(User user)
	{
		return removePlayer(world.getPlayer(user));
	}
	
	public boolean contains(ActivePlayer player) 
	{
		return players.contains(player);
	}
	
	public boolean contains(User user)
	{
		return contains(world.getPlayer(user));
	}
	
	public int size()
	{
		return players.size();
	}
	
	public Set<ActivePlayer> getPlayers()
	{
		return players;
	}
	
	public List<User> getUsers()
	{
		List<User> users = new ArrayList<User>();
		for (ActivePlayer player : players)
			if (player != null && player.getSfsUser().isConnected())
				users.add(player.getSfsUser());
		if (users.size() == 0) return null;
		return users;
	}
	
	public PendingGroup merge(PendingGroup pendingGroup)
	{
		Set<ActivePlayer> groupMembers = pendingGroup.getPlayers();
		for (ActivePlayer player : groupMembers) {
			player.setPendingGroup(null);
			addPlayer(player);
		}
		world.removePendingGroup((PendingGroup) pendingGroup);
		return this;
	}

	protected void notifyDisbanding()
	{
		List<User> users = getUsers();
		if (users == null) return;
		ISFSObject data = new SFSObject();
		world.getExtension().send("pendingGroupDisbanded", data, users);
	}
}
