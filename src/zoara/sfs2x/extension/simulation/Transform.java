package zoara.sfs2x.extension.simulation;

import java.util.Arrays;

import zoara.sfs2x.extension.simulation.item.Item;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class Transform 
{
	 // parent (one or the other)
	private ActivePlayer player;
	private Item item;
	
	private float[] position = { 0.0f, 0.0f, 0.0f };
	private float[] rotation = { 0.0f, 0.0f, 0.0f };
	
	public Transform(ActivePlayer _player)
	{
		player = _player;
	}
	
	public Transform(Item _item)
	{
		item = _item;
	}
	
	public ActivePlayer getPlayer()
	{
		return player;
	}
	
	public Item getItem()
	{
		return item;
	}
	
	public float[] getPosition()
	{
		return position;
	}
	
	public void setPosition(float[] _position)
	{
		position = _position;
	}
	
	public void setPosition(float _x, float _y, float _z)
	{
		position[0] = _x;
		position[1] = _y;
		position[2] = _z;
	}
	
	public void setPositionX(float _x)
	{
		position[0] = _x;
	}
	
	public void setPositionY(float _y)
	{
		position[1] = _y;
	}
	
	public void setPositionZ(float _z)
	{
		position[2] = _z;
	}
	
	public float[] getRotation()
	{
		return rotation;
	}
	
	public void setRotation(float[] _rotation)
	{
		rotation = _rotation;
	}
	
	public void setRotationX(float _x)
	{
		rotation[0] = _x;
	}
	
	public void setRotationY(float _y)
	{
		rotation[1] = _y;
	}
	
	public void setRotationZ(float _z)
	{
		rotation[2] = _z;
	}
	
	public void setRotation(float _x, float _y, float _z)
	{
		rotation[0] = _x;
		rotation[1] = _y;
		rotation[2] = _z;
	}

	public void toSFSObject(ISFSObject data) 
	{
		data.putFloat("posx", this.position[0]);
		data.putFloat("posy", this.position[1]);
		data.putFloat("posz", this.position[2]);
		data.putFloat("rotx", this.rotation[0]);
		data.putFloat("roty", this.rotation[1]);
		data.putFloat("rotz", this.rotation[2]);
	}
	
	public boolean equals(Transform t)
	{
		if (Arrays.equals(this.getPosition(), t.getPosition()) && 
			Arrays.equals(this.getRotation(), t.getRotation()))
			return true;
		return false;
	}
	
	public static boolean equals(Transform t1, Transform t2)
	{
		if (Arrays.equals(t1.getPosition(), t2.getPosition()) && 
			Arrays.equals(t1.getRotation(), t2.getRotation()))
			return true;
		return false;	
	}
	
	@Override
	public Transform clone()
	{
		Transform newTransform = null;
		if (item != null)
			newTransform = new Transform(item);
		else
			newTransform = new Transform(player);
		newTransform.setPosition(this.getPosition());
		newTransform.setRotation(this.getRotation());
		return newTransform;
	}
	
	public static Transform clone(Transform t)
	{
		return t.clone();
	}
	
	public String print()
	{
		String output = "Position: " + position[0] + ", " + position[1] + ", " + position[2] + 
			"\r\n" + "Rotation: " + rotation[0] + ", " + rotation[1] + ", " + rotation[2];
		return output;
	}
}
