package zoara.sfs2x.extension.simulation.item;

import zoara.sfs2x.extension.simulation.Transform;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class WorldItem extends Item 
{
	public Transform transform;
	
	private boolean grabbable = true;
	private boolean skill = false;
	private boolean isDestroyed = false;
	
	public WorldItem()
	{
		transform = new Transform(this);
	}

	public boolean isGrabbable()
	{
		return grabbable;
	}
	
	public void setGrabbable(boolean _grabbable)
	{
		grabbable = _grabbable;
	}
	
	public boolean isSkillItem()
	{
		return skill;
	}
	
	public void setSkillItem(boolean _skill)
	{
		skill = _skill;
	}
	
	public boolean isDestroyed()
	{
		return isDestroyed;
	}
	
	public void setDestroyed(boolean _isDestroyed)
	{
		isDestroyed = _isDestroyed;
	}
	
	public void toSFSObject(ISFSObject data)
	{
		if (isDestroyed) return;
		super.toSFSObject(data);
		transform.toSFSObject(data);
		data.putBool("Grabbable", grabbable);
		data.putBool("Skill", skill);
	}
}
