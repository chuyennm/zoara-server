package zoara.sfs2x.extension.simulation;

import java.util.HashSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import zoara.sfs2x.extension.*;
import zoara.sfs2x.extension.db.InfrastructureDBHandler;
import zoara.sfs2x.extension.db.ItemDBHandler;
import zoara.sfs2x.extension.simulation.item.Infrastructure;
import zoara.sfs2x.extension.simulation.item.WorldItem;

public class World 
{
	private ZoaraExtension extension;
	
	private HashMap<Integer,ActivePlayer> players = new HashMap<Integer,ActivePlayer>();
	
	private HashSet<Group> groups = new HashSet<Group>();
	private HashSet<PendingGroup> pendingGroups = new HashSet<PendingGroup>();
	private HashSet<Recipe> recipes = new HashSet<Recipe>();
	
	private HashMap<String,WorldItem> worldItems = new HashMap<String,WorldItem>();
	private HashMap<String,WorldItem> destroyedWorldItems = new HashMap<String,WorldItem>();
	
	private HashSet<Infrastructure> infrastructure = new HashSet<Infrastructure>();

	public World (ZoaraExtension _extension)
	{
		extension = _extension;
		
		extension.trace("Created world.");
		
		try {
			loadItems();
			loadRecipes();
			loadInfrastructure();
		} catch (Exception e) {
			extension.trace(e.getMessage());
		}
	}
	
	public ZoaraExtension getExtension()
	{
		return extension;
	}
	
	public int size()
	{
		return worldItems.size() + destroyedWorldItems.size();
	}
	
	public ActivePlayer addPlayer(User user)
	{
		ActivePlayer newPlayer = new ActivePlayer(user, this);
		players.put(user.getId(), newPlayer);
		return newPlayer;
	}

	// Gets the player corresponding to the specified SFS user
	public ActivePlayer getPlayer(User u) 
	{
		return players.get(u.getId());
	}
	
	public ActivePlayer getPlayer(int serverID) 
	{
		return players.get(serverID);
	}
	
	public Collection<ActivePlayer> getPlayers()
	{
		return players.values();
	}
	
	public boolean hasPlayer(String username)
	{
		Collection<ActivePlayer> allPlayers = players.values();
		for (ActivePlayer player : allPlayers)
			if (player.getName().equals(username))
				return true;
		
		return false;
	}
	
	public boolean hasPlayer(ActivePlayer player)
	{
		if (players.containsValue(player))
			return true;
		return false;
	}
	
	public boolean hasUser(User user)
	{
		ActivePlayer player = this.getPlayer(user);
		return hasPlayer(player);
	}

	// When user lefts the room or disconnects - removing him from the players list 
	public ActivePlayer userLeft(User user) 
	{
		ActivePlayer player = this.getPlayer(user);
		if (player == null)
			return null;
		players.remove(user.getId());		
		return player;
	}
	
	public void checkIfPlayersStillConnected()
	{
		Collection<ActivePlayer> allPlayers = players.values();
		for (ActivePlayer player : allPlayers)
			if (!player.getSfsUser().isConnected() && !player.isDisconnecting())
				forceRemovePlayer(player);
	}
	
	public void forceRemovePlayer(ActivePlayer player)
	{
		players.remove(player.getID());
		try {
			player.disconnect(extension);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void forceRemoveUser(User user)
	{
		forceRemovePlayer(this.getPlayer(user));		
	}
	
	public Group createGroup()
	{
		Group newGroup = new Group(this);
		groups.add(newGroup);
		return newGroup;
	}
	
	public PendingGroup createPendingGroup()
	{
		PendingGroup newGroup = new PendingGroup(this);
		pendingGroups.add(newGroup);
		return newGroup;	
	}
	
	public void addGroup(Group group)
	{
		groups.add(group);
	}
	
	public void addPendingGroup(PendingGroup group)
	{
		pendingGroups.add(group);
	}
	
	public boolean upgradeGroup(Set<ActivePlayer> groupMembers)
	{
		for (PendingGroup group : pendingGroups)
		{
			if (group.getPlayers().equals(groupMembers))
			{
				pendingGroups.remove(group);
				Group newGroup = createGroup();
				newGroup.merge(group);
				for (ActivePlayer player : groupMembers)
					player.setGroup(newGroup);
				return true;
			}
		}
		return false;
	}
	
	public boolean upgradeGroup(PendingGroup group)
	{
		if (pendingGroups.contains(group))
		{
			pendingGroups.remove(group);
			Group newGroup = createGroup();
			newGroup.merge(group);
			Set<ActivePlayer> groupMembers = group.getPlayers();
			for (ActivePlayer player : groupMembers)
				player.setGroup(newGroup);
			return true;
		}
		return false;
	}
	
	public boolean removePendingGroup(Set<ActivePlayer> groupMembers)
	{
		for (PendingGroup group : pendingGroups)
		{
			if (group.getPlayers().equals(groupMembers))
			{
				pendingGroups.remove(group);
				return true;
			}
		}
		return false;
	}
	
	public boolean removePendingGroup(PendingGroup pendingGroup)
	{
		if (pendingGroup == null) return false;
		Set<ActivePlayer> groupMembers = pendingGroup.getPlayers();
		if (groupMembers == null) return false;
		for (ActivePlayer groupMember : groupMembers)
			if (groupMember != null)
				groupMember.setPendingGroup(null);
		boolean result = pendingGroups.remove(pendingGroup);
		pendingGroup = null;
		return result;
	}
	
	public boolean removeGroup(Group group)
	{
		Set<ActivePlayer> groupMembers = group.getPlayers();
		for (ActivePlayer groupMember : groupMembers)
			groupMember.setGroup(null);
		boolean result = groups.remove(group);
		group = null;
		return result;
	}
	
	public void checkGroups()
	{
		for (Group group : groups)
			group.determinePossibleRecipes();
	}
	
	public Recipe addRecipe()
	{
		Recipe task = new Recipe();
		recipes.add(task);
		return task;
	}
	
	public Set<Recipe> getPossibleRecipes()
	{
		return recipes;
	}
	
	public void loadRecipes()
	{
		InfrastructureDBHandler.getRecipes(extension, this);
	}
	
	public WorldItem addWorldItem(String uniqueID)
	{
		WorldItem newWorldItem = new WorldItem();
		worldItems.put(uniqueID, newWorldItem);
		return newWorldItem;
	}
	
	public WorldItem addDestroyedWorldItem(String uniqueID)
	{
		WorldItem newWorldItem = new WorldItem();
		destroyedWorldItems.put(uniqueID, newWorldItem);
		return newWorldItem;
	}
	
	public WorldItem destroyWorldItem(String uniqueID)
	{
		WorldItem worldItem = worldItems.remove(uniqueID);
		if (worldItem == null) return null;
		destroyedWorldItems.put(uniqueID, worldItem);
		return worldItem;
	}
	
	public WorldItem undestroyWorldItem(String uniqueID)
	{
		WorldItem worldItem = destroyedWorldItems.remove(uniqueID);
		if (worldItem == null) return null;
		worldItems.put(uniqueID, worldItem);
		return worldItem;
	}
	
	public WorldItem getWorldItem(String uniqueID)
	{
		return worldItems.get(uniqueID);
	}
	
	public void loadItems()
	{
		worldItems = new HashMap<String,WorldItem>();
		destroyedWorldItems = new HashMap<String,WorldItem>();	
		ItemDBHandler.getWorldItems(extension, this);
		extension.trace("Initialized all world items.");
	}
	
	public Infrastructure addInfrastructure(int templateID)
	{
		Infrastructure building = new Infrastructure(templateID, infrastructure.size() + 1);
		infrastructure.add(building);
		return building;
	}
	
	public void loadInfrastructure()
	{
		InfrastructureDBHandler.getInfrastructure(extension, this);
	}
	
	public void worldItemListToSFSArray(ISFSArray data)
	{
		Collection<WorldItem> items = worldItems.values();
		for (WorldItem worldItem : items)
		{
			ISFSObject itemData = new SFSObject();
			worldItem.toSFSObject(itemData);
			
			if (itemData.size() < 1) continue;
			
			data.addSFSObject(itemData);
		}
	}
	
	public void playerListToSFSArray(ISFSArray data, User user)
	{
		Collection<ActivePlayer> allPlayers = players.values();
		for (ActivePlayer player : allPlayers)
		{
			if (player.getSfsUser().getId() == user.getId()) continue;
			
			ISFSObject playerData = new SFSObject();
			player.toSFSObject(playerData);
			
			data.addSFSObject(playerData);
		}
	}
	
	public void infrastructureToSFSArray(ISFSArray data)
	{
		for (Infrastructure building : infrastructure)
		{
			ISFSObject buildingData = new SFSObject();
			building.toSFSObject(buildingData);
			data.addSFSObject(buildingData);
		}
	}
	
	public void recipesToSFSArray(ISFSArray data)
	{
		for (Recipe recipe : recipes)
			recipe.toSFSArray(data);
	}
	
	public void toSFSObject(ISFSObject data, User user)
	{
		if (players.size() > 1) // otherwise we are the only player
		{
			ISFSArray playersData = new SFSArray();
			playerListToSFSArray(playersData, user);
			data.putSFSArray("Players", playersData);
		}
		if (recipes.size() > 0)
		{
			ISFSArray recipeData = new SFSArray();
			recipesToSFSArray(recipeData);
			data.putSFSArray("AllRecipes", recipeData);
		}
		if (infrastructure.size() > 0)
		{
			ISFSArray infrastructureData = new SFSArray();
			infrastructureToSFSArray(infrastructureData);
			data.putSFSArray("Infrastructure", infrastructureData);
		}
		if (worldItems.size() > 0)
		{
			ISFSArray itemsData = new SFSArray();
			worldItemListToSFSArray(itemsData);
			data.putSFSArray("WorldItems", itemsData);
		}
	}
	
	public void objectsToSFSObject(ISFSObject data, User user)
	{
		if (infrastructure.size() > 0)
		{
			ISFSArray infrastructureData = new SFSArray();
			infrastructureToSFSArray(infrastructureData);
			data.putSFSArray("Infrastructure", infrastructureData);
		}
		if (worldItems.size() > 0)
		{
			ISFSArray itemsData = new SFSArray();
			worldItemListToSFSArray(itemsData);
			data.putSFSArray("WorldItems", itemsData);
		}
	}
}
