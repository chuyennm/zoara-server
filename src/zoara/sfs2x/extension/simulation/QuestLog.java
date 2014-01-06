package zoara.sfs2x.extension.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import java.util.HashMap;

import zoara.sfs2x.extension.db.QuestDBHandler;

import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class QuestLog 
{
	private ActivePlayer player;
	
	private HashMap<String,Quest> quests = new HashMap<String,Quest>();
	private HashSet<Quest> questsToAdd = new HashSet<Quest>();
	private HashSet<Quest> questsToUpdate = new HashSet<Quest>();
	private HashSet<Quest> questsToRemove = new HashSet<Quest>();
	
	public QuestLog(ActivePlayer _player)
	{
		player = _player;
	}
	
	public Quest getQuest(String uniqueID)
	{
		return quests.get(uniqueID);
	}
	
	public Quest addQuest(String uniqueID)
	{
		return addQuest(uniqueID, false);
	}
	
	public Quest addQuest(String uniqueID, boolean fromDB)
	{
		Quest newQuest = new Quest(player);
		quests.put(uniqueID, newQuest);
		if (questsToRemove.contains(newQuest)) questsToRemove.remove(newQuest);
		if (fromDB) { questsToUpdate.add(newQuest); }
		else { questsToAdd.add(newQuest); }
		return newQuest;
	}
	
	public boolean updateQuest(String uniqueID, boolean completed)
	{
		return updateQuest(uniqueID, completed, false, -1);
	}
	
	public boolean updateQuest(String uniqueID, boolean completed, boolean failed)
	{
		return updateQuest(uniqueID, completed, failed, -1);
	}
	
	public boolean updateQuest(String uniqueID, boolean completed, boolean failed, int currentStep)
	{
		Quest quest = quests.get(uniqueID);
		if (quest == null) return false;
		quest.setCompleted(completed);
		quest.setFailed(failed);
		if (currentStep != -1)
			quest.setCurrentStepNumber(currentStep);
		if (!questsToAdd.contains(quest)) questsToUpdate.add(quest);
		return true;
	}
	
	public void removeQuest(String uniqueID)
	{
		Quest quest = quests.remove(uniqueID);
		questsToRemove.add(quest);
		if (questsToAdd.contains(quest)) questsToAdd.remove(quest);
		if (questsToUpdate.contains(quest)) questsToUpdate.remove(quest);
	}
	
	public void toSFSArray(ISFSArray data)
	{
		Collection<Quest> allQuests = quests.values();
		for (Quest quest : allQuests)
		{
			ISFSObject questData = new SFSObject();
			quest.toSFSObject(questData);
			data.addSFSObject(questData);
		}
	}
	
	public void updateDB(SFSExtension extension)
	{
		if (questsToAdd.size() > 0)
		{
			for (Quest quest : questsToAdd)
				QuestDBHandler.addQuest(extension, quest);
		}
		if (questsToUpdate.size() > 0)
			QuestDBHandler.updateQuests(extension, new ArrayList<Quest>(questsToUpdate));
		if (questsToRemove.size() > 0)
			QuestDBHandler.removeQuests(extension, new ArrayList<Quest>(questsToRemove));
	}
}
