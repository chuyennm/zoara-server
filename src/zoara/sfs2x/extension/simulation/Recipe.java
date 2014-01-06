package zoara.sfs2x.extension.simulation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import zoara.sfs2x.extension.simulation.item.Item;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

public class Recipe
{
	private int dbID;
	private int templateID;
	private String name;
	private String description;
	private HashMap<Item,Skill> skillResources = new HashMap<Item,Skill>();
	private HashMap<Item,Integer> resourcesNeeded = new HashMap<Item,Integer>();
	
	public int getDBID()
	{
		return dbID;
	}
	
	public void setDBID(int _dbID)
	{
		dbID = _dbID;
	}
	
	public int getTemplateID()
	{
		return templateID;
	}
	
	public void setTemplateID(int _templateID)
	{
		templateID = _templateID;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String _name)
	{
		name = _name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public void setDescription(String _description)
	{
		description = _description;
	}
	
	public void addResource(int skill, String uniqueID, int quantity)
	{
		Item item = new Item();
		item.setUniqueID(uniqueID);
		skillResources.put(item, Skill.values()[skill]);
		resourcesNeeded.put(item, quantity);
	}
	
	public boolean areSkillsMet(Set<Skill> skills)
	{
		Set<Skill> neededSkills = new HashSet<Skill>(skillResources.values());
		if (skills.containsAll(neededSkills)) return true;
		return false;
	}
	
	public boolean areResourcesMet(Set<ActivePlayer> players)
	{		
		Set<Item> resources = resourcesNeeded.keySet();
		for (Item resource : resources)
		{
			Skill neededSkill = skillResources.get(resource);
			int amountNeeded = resourcesNeeded.get(resource);
			int amountFound = 0;
			
			for (ActivePlayer player : players)
			{
				if (player.getSkill() != neededSkill) continue;
				amountFound += player.inventory.containsNum(resource);
			}
			
			if (amountFound < amountNeeded) return false;
		}
		
		return true;
	}
	
	private HashMap<Skill,Set<ActivePlayer>> needFairDistribution(Set<ActivePlayer> players)
	{
		boolean moreThanOne = false;
		HashMap<Skill,Set<ActivePlayer>> playersBySkill = new HashMap<Skill,Set<ActivePlayer>>();
		for (ActivePlayer player : players)
		{
			Set<ActivePlayer> playersOfSkill = playersBySkill.get(player.getSkill());
			if (playersOfSkill == null)
				playersOfSkill = new HashSet<ActivePlayer>();
			else
				moreThanOne = true;
			playersOfSkill.add(player);
			playersBySkill.put(player.getSkill(), playersOfSkill);
		}
		if (!moreThanOne)
			return null;
		return playersBySkill;
	}
	
	public HashMap<ActivePlayer,HashMap<Item,Integer>> resourcesByPlayer(Set<ActivePlayer> players)
	{
		HashMap<ActivePlayer,HashMap<Item,Integer>> resourcesByPlayer = 
			new HashMap<ActivePlayer,HashMap<Item,Integer>>();
		
		HashMap<Skill,Set<ActivePlayer>> playersBySkill = needFairDistribution(players);
		if (playersBySkill == null)
		{
			for (ActivePlayer player : players)
			{
				HashMap<Item,Integer> contributions = resourcesPerPlayer(player);
				resourcesByPlayer.put(player, contributions);
			}
			return resourcesByPlayer;
		}
		
		Set<Item> resources = resourcesNeeded.keySet();
		for (Item resource : resources)
		{
			int amountNeeded = resourcesNeeded.get(resource);
			Skill neededSkill = skillResources.get(resource);
			Set<ActivePlayer> playersOfSkill = playersBySkill.get(neededSkill);
			if (playersOfSkill.size() > 1)
			{
				int totalAmountFound = 0;
				int numPeople = 0;
				HashMap<Integer,ActivePlayer> peopleByAmounts = new HashMap<Integer,ActivePlayer>();
				
				for (ActivePlayer player : playersOfSkill)
				{
					if (player.getSkill() != neededSkill) continue;
					int amountFound = player.inventory.containsNum(resource);
					if (amountFound > 0)
					{
						numPeople++;
						peopleByAmounts.put(amountFound, player);
					}
					totalAmountFound += amountFound;
				}
				
				if (numPeople > 1)
				{
					Integer[] sortedAmounts = (Integer[]) peopleByAmounts.keySet().toArray();
					Arrays.sort(sortedAmounts);
					
					boolean tryAgain = false;
					int numAttempts = 0;
					int average = (int) Math.ceil((double)amountNeeded / (double)numPeople);
					do
					{
						int otherAverage = Math.round((float)totalAmountFound / (float)numPeople);
						int totalAllocated = 0;
						int stillNeeded = amountNeeded;
						HashMap<ActivePlayer,Integer> 
							tentativeContributions = new HashMap<ActivePlayer,Integer>();
						int numPeopleTried = 0;
						//int halfway = (int) Math.floor((double)numPeople / 2.0);
						for (int amount : sortedAmounts)
						{
							ActivePlayer player = peopleByAmounts.get(amount);
							int toAllocate;
							if (numPeopleTried >= 1 && 
									totalAllocated < (numPeopleTried * average))
							{
								int difference = (numPeopleTried * average) - totalAllocated;
								toAllocate = Math.min(amount, average + difference);
							}
							else if (numAttempts > 0 && amount >= otherAverage)
							{
								toAllocate = Math.min(otherAverage, stillNeeded);							
							}
							else if (amount >= average)
							{
								toAllocate = Math.min(average, stillNeeded);
							}
							else
							{
								toAllocate = Math.min(amount, stillNeeded);
							}
							totalAllocated += toAllocate;
							stillNeeded -= toAllocate;
							tentativeContributions.put(player, toAllocate);
							numPeopleTried++;
						}
						if (stillNeeded > 0)
						{
							tryAgain = true;
							numAttempts++;
							average--;
						}
						else
						{
							Set<ActivePlayer> contributingPlayers = tentativeContributions.keySet();
							for (ActivePlayer player : contributingPlayers)
							{
								int toAllocate = tentativeContributions.get(player);
								HashMap<Item,Integer> contributions = resourcesByPlayer.get(player);
								if (contributions == null)
									contributions = new HashMap<Item,Integer>();
								contributions.put(resource, toAllocate);
								resourcesByPlayer.put(player, contributions);
							}
						}
					} while (tryAgain == true && average > 0);
					if (tryAgain)
					{
						// This didn't work, so we reverse it and put the burden on the
						// one(s) with the most. Not really fair, but we can't do this forever.
						List<Integer> reverseSortedAmounts = Arrays.asList(sortedAmounts);
						Collections.reverse(reverseSortedAmounts);
						int stillNeeded = amountNeeded;
						for (Integer amount : reverseSortedAmounts)
						{
							ActivePlayer player = peopleByAmounts.get(amount);
							HashMap<Item,Integer> contributions = resourcesByPlayer.get(player);
							if (contributions == null)
								contributions = new HashMap<Item,Integer>();
							int toAllocate = Math.min(stillNeeded, amount);
							contributions.put(resource, toAllocate);
						}
					}
				}
				else
				{
					for (ActivePlayer player : playersOfSkill)
					{
						HashMap<Item,Integer> contributions = resourcesByPlayer.get(player);
						if (contributions == null)
							contributions = new HashMap<Item,Integer>();			
						int amountFound = player.inventory.containsNum(resource);			
						if (amountFound > 0)
							contributions.put(resource, Math.min(amountNeeded, amountFound));
						resourcesByPlayer.put(player, contributions);
					}
				}
			}
			else
			{
				for (ActivePlayer player : playersOfSkill)
				{
					HashMap<Item,Integer> contributions = resourcesByPlayer.get(player);
					if (contributions == null)
						contributions = new HashMap<Item,Integer>();				
					int amountFound = player.inventory.containsNum(resource);			
					if (amountFound > 0)
						contributions.put(resource, Math.min(amountNeeded, amountFound));
					resourcesByPlayer.put(player, contributions);
				}
			}
		}
		return resourcesByPlayer;
	}
	
	private HashMap<Item,Integer> resourcesPerPlayer(ActivePlayer player)
	{		
		HashMap<Item,Integer> contributions = new HashMap<Item,Integer>();
		
		Set<Item> resources = resourcesNeeded.keySet();
		for (Item resource : resources)
		{
			Skill neededSkill = skillResources.get(resource);
			if (player.getSkill() != neededSkill) continue;

			int amountNeeded = resourcesNeeded.get(resource);
			int amountFound = player.inventory.containsNum(resource);			
			if (amountFound > 0)
				contributions.put(resource, Math.min(amountNeeded, amountFound));
		}
		
		if (contributions.size() > 0)
			return contributions;
		else
			return null;
	}
	
	public void toSFSObject(ISFSObject data)
	{
		toSFSObject(data, true);
	}
	
	public void toSFSObject(ISFSObject data, boolean verbose)
	{
		data.putInt("TemplateID", templateID);
		if (verbose)
		{
			data.putUtfString("Name", name);
			data.putUtfString("Description", description);
		}
	}

	public void toSFSObject(ISFSObject data, Set<Item> items)
	{
		toSFSObject(data, items, true);
	}
	
	public void toSFSObject(ISFSObject data, Set<Item> items, boolean verbose)
	{
		toSFSObject(data, verbose);

		ISFSArray resources = new SFSArray();
		for (Item item : items)
		{
			int quantity = resourcesNeeded.get(item);
			ISFSObject sfso = new SFSObject();
			if (item.getTemplateID() > 0)
				sfso.putInt("TemplateID", item.getTemplateID());
			sfso.putUtfString("UniqueItemID", item.getUniqueID());
			sfso.putInt("Quantity", quantity);
			resources.addSFSObject(sfso);
		}
		data.putSFSArray("Resources", resources);
	}
	
	public void toSFSObject(ISFSObject data, HashMap<Item,Integer> contributions, boolean verbose)
	{
		toSFSObject(data, verbose);

		ISFSArray resources = new SFSArray();
		Set<Item> items = contributions.keySet();
		for (Item item : items)
		{
			int quantity = contributions.get(item);
			ISFSObject sfso = new SFSObject();
			if (item.getTemplateID() > 0)
				sfso.putInt("TemplateID", item.getTemplateID());
			sfso.putUtfString("UniqueItemID", item.getUniqueID());
			sfso.putInt("Quantity", quantity);
			resources.addSFSObject(sfso);
		}
		data.putSFSArray("Resources", resources);
	}
	
	public void toSFSArray(ISFSArray data)
	{		
		ISFSObject subData = new SFSObject();		
		Set<Item> items = resourcesNeeded.keySet();
		toSFSObject(subData, items);
		data.addSFSObject(subData);
	}
	
	public void playerResourcesToSFSArray(ISFSObject data, HashMap<Item,Integer> contributions)
	{
		if (contributions == null) return;		
		toSFSObject(data, contributions, false);
	}
	
	public void possibleToSFSArray(ISFSArray data)
	{
		ISFSObject subData = new SFSObject();
		toSFSObject(subData, false);
		subData.putBool("Possible", true);
		data.addSFSObject(subData);
	}
	
	public void changedToSFSArray(ISFSArray data, boolean possible)
	{
		ISFSObject subData = new SFSObject();
		toSFSObject(subData, false);
		if (possible)
			subData.putBool("Available", false);
		else
			subData.putBool("Possible", false);
		data.addSFSObject(subData);
	}
}
