package zoara.sfs2x.extension.simulation.item;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class Item 
{
	protected int dbID = 0;
	protected int templateID = 0;
	protected String uniqueID = "";
	
	public Item() { }
	
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
	
	public void toSFSObject(ISFSObject data)
	{
		data.putInt("TemplateID", templateID);
		data.putUtfString("UniqueItemID", uniqueID);
	}
}
