package zoara.sfs2x.extension.simulation.item;


import com.smartfoxserver.v2.entities.data.ISFSObject;

import zoara.sfs2x.extension.simulation.Transform;

public class Infrastructure extends Item
{
	public Transform transform;
	
	private String prefix = "INFRASTRUCTURE";
	
	public Infrastructure(int _templateID, int id)
	{
		templateID = _templateID;
		uniqueID = prefix + id;
		transform = new Transform(this);
	}
	
	@Override
	public void toSFSObject(ISFSObject data)
	{
		super.toSFSObject(data);
		transform.toSFSObject(data);
	}
}
