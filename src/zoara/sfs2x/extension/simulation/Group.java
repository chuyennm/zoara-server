package zoara.sfs2x.extension.simulation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import zoara.sfs2x.extension.simulation.item.Item;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class Group extends PendingGroup
{
	private Room room;
	
	private HashSet<Skill> skills = new HashSet<Skill>();
	/** These are recipes that we have the necessary skills to complete */
	private Set<Recipe> possibleRecipes = new HashSet<Recipe>();
	/** These are recipes that we have the necessary resources to complete */
	private Set<Recipe> availableRecipes = new HashSet<Recipe>();
	
	private HashMap<Integer,Integer> playerVotes = new HashMap<Integer,Integer>();
	private ActivePlayer chosenPlayer = null;
	
	public Group(World world)
	{
		super(world);
		
		room = world.getExtension().createGroupRoom();
	}
	
	public Room getRoom()
	{
		return room;
	}
	
	@Override
	public boolean addPlayer(ActivePlayer player) 
	{
		if (player.isInGroup())
		{
			Group otherGroup = player.getGroup();
			merge(otherGroup);
		}	
		else
		{
			players.add(player);
			player.setGroup(this);
			/*if (room != null)
			{
				try {
					player.getSfsUser().addJoinedRoom(room);
				} catch (Exception e) {
					world.getExtension().trace(e.getMessage());
				}
			}*/
			notifyJoined(player);
			notifyMemberUpdateExcept(player);
			skills.add(player.getSkill());
			determinePossibleRecipes();
		}
		return true;
	}
	
	@Override
	public boolean addUser(User user) 
	{
		return addPlayer(world.getPlayer(user));
	}
	
	@Override
	public boolean removePlayer(ActivePlayer player)
	{
		boolean removed = players.remove(player);
		if (!removed) return false;
		player.setGroup(null);

		/*try {
			if (player.getSfsUser().isJoinedInRoom(room))
				player.getSfsUser().removeJoinedRoom(room);
		} catch (Exception e) {
			world.getExtension().trace(e.getMessage());
		}*/
		
		if (size() <= 1)
		{
			notifyDisbanding();
			world.removeGroup(this);
		}
		else
		{
			notifyMemberUpdate();
			determinePossibleRecipes();
		}
		return true;
	}
	
	@Override
	public boolean removeUser(User user)
	{
		return removePlayer(world.getPlayer(user));
	}
	
	@Override
	public PendingGroup merge(PendingGroup pendingGroup)
	{
		Set<ActivePlayer> groupMembers = pendingGroup.getPlayers();
		for (ActivePlayer player : groupMembers) {
			player.setGroup(null);
			player.setPendingGroup(null);
			addPlayer(player);
		}
		world.removePendingGroup(pendingGroup);
		return this;
	}
	
	public Group merge(Group otherGroup)
	{
		Set<ActivePlayer> groupMembers = otherGroup.getPlayers();
		for (ActivePlayer player : groupMembers) {
			player.setGroup(null);
			addPlayer(player);
		}
		world.removeGroup((Group) otherGroup);
		return this;
	}

	@Override
	protected void notifyDisbanding()
	{
		List<User> users = getUsers();
		if (users == null) return;
		ISFSObject data = new SFSObject();
		world.getExtension().send("groupDisbanded", data, users);
		
		/*if (room != null)
		{
			for (User user : users)
			{
				try {
					if (room.containsUser(user))
						room.removeUser(user);
				} catch (Exception e) {
					world.getExtension().trace(e.getMessage());
				}
			}
		}*/
	}
	
	public void determinePossibleRecipes()
	{
		//possibleRecipes = new HashSet<Recipe>();
		Set<Recipe> allPossibleRecipes = world.getPossibleRecipes();
		Set<Recipe> changedRecipes = new HashSet<Recipe>();
		for (Recipe possibleRecipe : allPossibleRecipes)
		{
			if (possibleRecipe.areSkillsMet(skills))
				possibleRecipes.add(possibleRecipe);
			else if (possibleRecipes.remove(possibleRecipe))
				changedRecipes.add(possibleRecipe);
		}
		notifyPossible();
		determineAvailableRecipes();
		notifyChanged(changedRecipes, false);
	}
	
	public void determineAvailableRecipes()
	{
		//availableRecipes = new HashSet<Recipe>();
		Set<Recipe> changedRecipes = new HashSet<Recipe>();
		for (Recipe possibleRecipe : possibleRecipes)
		{
			if (possibleRecipe.areResourcesMet(players))
				availableRecipes.add(possibleRecipe);
			else if (availableRecipes.remove(possibleRecipe))
				changedRecipes.add(possibleRecipe);
		}
		notifyAvailable();
		notifyChanged(changedRecipes, true);
	}
	
	public boolean isAvailableRecipe(int recipeID)
	{
		for (Recipe availableRecipe : availableRecipes)
			if (availableRecipe.getTemplateID() == recipeID)
				return true;
		return false;
	}
	
	public Recipe getAvailableRecipe(int recipeID)
	{
		for (Recipe availableRecipe : availableRecipes)
			if (availableRecipe.getTemplateID() == recipeID)
				return availableRecipe;
		return null;
	}
	
	public boolean addVote(int playerID, int recipeID)
	{
		if (!isAvailableRecipe(recipeID)) return false;
		playerVotes.put(playerID, recipeID);
		tallyVotes();
		return true;
	}
	
	public boolean revokeVote(int playerID)
	{
		if (playerVotes.remove(playerID) != null) return true;
		else return false;
	}
	
	public void tallyVotes()
	{
		if (playerVotes.size() != players.size()) return;
		
		boolean allInAgreement = true;
		int lastVote = -1;
		
		Collection<Integer> votes = playerVotes.values();
		for (Integer vote : votes)
		{
			if (lastVote != -1 && vote != lastVote)
			{
				allInAgreement = false;
				break;
			}
			lastVote = vote;
		}
		
		if (allInAgreement)
		{
			world.getExtension().trace("We're all in agreement! Yay!");
			Recipe chosenRecipe = getAvailableRecipe(lastVote);
			prepareToBuild(chosenRecipe.getTemplateID());
		} else {
			world.getExtension().trace("We're not in agreement yet...");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void prepareToBuild(int templateID)
	{
		Set<ActivePlayer> groupMembers = (Set<ActivePlayer>) players.clone();
		Random rand = new Random();
		while (chosenPlayer == null)
		{
			int randMember = rand.nextInt(players.size());
			int count = 0;
			for (ActivePlayer player : groupMembers)
			{
				if (count == randMember)
				{
					chosenPlayer = player;
					notifyChosen(player, templateID);
					break;
				}
				count++;
			}
		}
		groupMembers.remove(chosenPlayer);
		notifyNotChosen(groupMembers, templateID);
	}
	
	public ActivePlayer getChosenPlayer()
	{
		return chosenPlayer;
	}
	
	public void buildInfrastructure(int templateID)
	{
		for (ActivePlayer player : players)
		{
			player.increaseBuilt();
			notifyBuilt(player, templateID);
		}
		chosenPlayer = null;
		playerVotes.clear();
		//determinePossibleRecipes();
	}
	
	public void playersToSFSArray(ISFSObject data)
	{
		playersToSFSArray(data, null);
	}
	
	private void playersToSFSArray(ISFSObject data, ActivePlayer player)
	{
		ISFSArray groupMembers = new SFSArray();
		for (ActivePlayer otherPlayer : players)
			if (player != null && otherPlayer.getID() != player.getID())
				groupMembers.addInt(otherPlayer.getID());
		if (groupMembers.size() > 0)
			data.putSFSArray("members", groupMembers);		
	}
	
	private void notifyJoined(ActivePlayer player)
	{
		ISFSObject data = new SFSObject();
		if (room != null)
			data.putUtfString("room", room.getName());
		playersToSFSArray(data, player);
		world.getExtension().send("successfullyJoinedGroup", data, player.getSfsUser(), player.useUDP);
	}
	
	private void notifyMemberUpdate()
	{
		for (ActivePlayer player : players)
			notifyMemberUpdate(player);
	}
	
	private void notifyMemberUpdate(ActivePlayer player)
	{
		ISFSObject data = new SFSObject();
		playersToSFSArray(data, player);
		world.getExtension().send("updatedMemberList", data, player.getSfsUser(), player.useUDP);		
	}
	
	private void notifyMemberUpdateExcept(ActivePlayer newPlayer)
	{
		for (ActivePlayer player : players)
			if (player != newPlayer)
				notifyMemberUpdate(player);
	}
	
	private void notifyPossible()
	{
		for (ActivePlayer player : players)
			notifyPossible(player);
	}
	
	private void notifyPossible(ActivePlayer player)
	{
		ISFSArray recipes = new SFSArray();
		
		for (Recipe possibleRecipe : possibleRecipes)
			possibleRecipe.possibleToSFSArray(recipes);
		
		ISFSObject data = new SFSObject();
		data.putSFSArray("recipes", recipes);
		world.getExtension().send("possibleRecipes", data, player.getSfsUser(), player.useUDP);
		world.getExtension().trace("Sent player possible recipes list, size: " + recipes.size());
	}
	
	private void notifyAvailable()
	{
		for (Recipe availableRecipe : availableRecipes)
			notifyAvailable(availableRecipe);
	}
	
	private void notifyAvailable(Recipe availableRecipe)
	{
		HashMap<ActivePlayer,HashMap<Item,Integer>> resourcesByPlayer = 
			availableRecipe.resourcesByPlayer(players);
		
		for (ActivePlayer player : players)
		{
			HashMap<Item,Integer> contributions = resourcesByPlayer.get(player);
			ISFSObject data = new SFSObject();
			availableRecipe.playerResourcesToSFSArray(data, contributions);
			world.getExtension().send("availableRecipe", data, player.getSfsUser(), player.useUDP);
			world.getExtension().trace("Sent player updated available recipe: " + 
					availableRecipe.getTemplateID());
		}
	}
	
	private void notifyChanged(Set<Recipe> changedRecipes, boolean possible)
	{
		for (ActivePlayer player : players)
			notifyChanged(changedRecipes, player, possible);
	}
	
	private void notifyChanged(Set<Recipe> changedRecipes, ActivePlayer player, boolean possible)
	{
		ISFSArray recipes = new SFSArray();
		
		for (Recipe changedRecipe : changedRecipes)
			changedRecipe.changedToSFSArray(recipes, possible);
		
		ISFSObject data = new SFSObject();
		data.putSFSArray("recipes", recipes);
		world.getExtension().send("changedRecipes", data, player.getSfsUser(), player.useUDP);	
	}
	
	private void notifyChosen(ActivePlayer player, int templateID)
	{
		ISFSObject data = new SFSObject();
		data.putInt("TemplateID", templateID);
		world.getExtension().send("chosenToBuild", data, player.getSfsUser(), player.useUDP);
	}
	
	private void notifyNotChosen(Set<ActivePlayer> groupMembers, int templateID)
	{
		for (ActivePlayer player : groupMembers)
			notifyNotChosen(player, templateID);		
	}
	
	private void notifyNotChosen(ActivePlayer toPlayer, int templateID)
	{
		ISFSObject data = new SFSObject();
		data.putInt("TemplateID", templateID);
		data.putUtfString("ChosenPlayer", chosenPlayer.getName());
		world.getExtension().send("notChosenToBuild", data, toPlayer.getSfsUser(), toPlayer.useUDP);		
	}
	
	@SuppressWarnings("unused")
	private void notifyBuilt(int templateID)
	{
		for (ActivePlayer player : players)
			notifyBuilt(player, templateID);		
	}
	
	private void notifyBuilt(ActivePlayer player, int templateID)
	{
		ISFSObject data = new SFSObject();
		data.putInt("TemplateID", templateID);
		world.getExtension().send("builtInfrastructureSuccess", data, player.getSfsUser(), player.useUDP);
	}
}
