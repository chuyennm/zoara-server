package zoara.sfs2x.extension.simulation;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class Quest 
{
	private ActivePlayer player;
	
	protected int dbID = 0;
	protected int templateID = 0;
	protected String uniqueID = "";
	protected boolean active = true;
	protected boolean completed = false;
	protected boolean failed = false;
	protected int currentStepNumber = 0;
	
	// this one should not be used
	public Quest() { }
	
	public Quest(ActivePlayer _player)
	{
		player = _player;
	}
	
	public final ActivePlayer getPlayer()
	{
		return player;
	}
	
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

	public String getUniqueID()
	{
		return uniqueID;
	}
	
	public void setUniqueID(String _uniqueID)
	{
		uniqueID = _uniqueID;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void setActive(boolean _active)
	{
		active = _active;
		if (_active) { completed = !_active; }
	}
	
	public boolean isCompleted()
	{
		return completed;
	}
	
	public void setCompleted(boolean _completed)
	{
		completed = _completed;
		if (_completed) { active = !_completed; }
	}
	
	public boolean isFailed()
	{
		return failed;
	}
	
	public void setFailed(boolean _failed)
	{
		failed = _failed;
	}
	
	public final int getCurrentStepNumber()
	{
		return currentStepNumber;
	}
	
	public final void setCurrentStepNumber(int _currentStepNumber)
	{
		currentStepNumber = _currentStepNumber;
	}
	
	public void toSFSObject(ISFSObject data)
	{
		data.putInt("TemplateID", templateID);
		data.putUtfString("UniqueQuestID", uniqueID);
		data.putBool("Completed", completed);
		data.putBool("Failed", failed);
		data.putInt("CurrentStepNumber", currentStepNumber);
	}
}
