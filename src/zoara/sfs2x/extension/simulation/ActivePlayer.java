package zoara.sfs2x.extension.simulation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import zoara.sfs2x.extension.db.ItemDBHandler;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.extensions.SFSExtension;

@SuppressWarnings("unused")
public class ActivePlayer 
{
	private World world;
	private User sfsUser; // SFS user that corresponds to this player
	public boolean useUDP = false;
	private boolean isDisconnecting = false;
	
	private int ID = 0;
	private String name = "";
	private String configuration = "";
	private int level = 1;
	private long exp = 0;
	private boolean inClan = false;
	private boolean walking = false;
	private int clanID = 0;
	private String clanName = "";
	private int dbID = 0;
	private Skill skill = Skill.NONE;
	
	public Transform transform;
	public Inventory inventory;
	public QuestLog quests;
	
	public Transform inClanTransform;
	
	private Group group;
	private PendingGroup pendingGroup;
	private int numBuilt = 0;
	
	public ActivePlayer(User _sfsUser, World _world)
	{
		sfsUser = _sfsUser;
		world = _world;
		ID = sfsUser.getId();
		transform = new Transform(this);
		inventory = new Inventory(this);
		quests = new QuestLog(this);
		inClanTransform = new Transform(this);
	}
	
	public User getSfsUser()
	{
		return sfsUser;
	}
	
	public boolean isDisconnecting()
	{
		return isDisconnecting;
	}
	
	public int getID()
	{
		return ID;
	}
	
	public void setID(int _id)
	{
		ID = _id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String _name)
	{
		name = _name;
	}
	
	public String getConfig()
	{
		return configuration;
	}
	
	public void setConfig(String _configuration)
	{
		configuration = _configuration;
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void setLevel(int _level)
	{
		level = _level;
	}
	
	public long getEXP()
	{
		return exp;
	}
	
	public void setEXP(long _exp)
	{
		exp = _exp;
	}
	
	public boolean isInClan()
	{
		return inClan;
	}
	
	public void setInClan(boolean _inClan)
	{
		inClan = _inClan;
	}
	
	public boolean isWalking()
	{
		return walking;
	}
	
	public void setWalking(boolean _walking)
	{
		walking = _walking;
	}
	
	public int getClanID()
	{
		return clanID;
	}
	
	public void setClanID(int _clanID)
	{
		clanID = _clanID;
	}
	
	public String getClanName()
	{
		return clanName;
	}
	
	public void setClanName(String _clanName)
	{
		clanName = _clanName;
	}
	
	public int getDBID()
	{
		return dbID;
	}
	
	public void setDBID(int _dbID)
	{
		dbID = _dbID;
	}
	
	public Skill getSkill()
	{
		return skill;
	}
	
	public void setSkill(Skill _skill)
	{
		skill = _skill;
	}
	
	public void setSkill(int skillNumber)
	{
		skill = Skill.values()[skillNumber];
	}
	
	public boolean isInGroup()
	{
		if (group == null) return false;
		else return true;
	}
	
	public Group getGroup()
	{
		return group;
	}
	
	public void setGroup(Group newGroup)
	{
		group = newGroup;
		if (newGroup == null) return;
		if (pendingGroup != null)
		{
			Set<ActivePlayer> pendingGroupMembers = pendingGroup.getPlayers();
			Set<ActivePlayer> thisGroupMembers = newGroup.getPlayers();
			if (thisGroupMembers.containsAll(pendingGroupMembers))
			{
				pendingGroup = null;
				world.removePendingGroup(pendingGroup);
			}
		}
	}
	
	public boolean isInPendingGroup()
	{
		if (pendingGroup == null) return false;
		else return true;
	}
	
	public PendingGroup getPendingGroup()
	{
		return pendingGroup;
	}
	
	public void setPendingGroup(PendingGroup _pendingGroup)
	{
		pendingGroup = _pendingGroup;
	}
	
	public void checkGroup()
	{
		if (group != null)
			group.determinePossibleRecipes();
	}
	
	public int getNumBuilt()
	{
		return numBuilt;
	}
	
	public void increaseBuilt()
	{
		numBuilt += 1;
	}
	
	public void resetInClanTransform()
	{
		inClanTransform = new Transform(this);
	}

	public void toSFSObject(ISFSObject playerData) 
	{
		playerData.putInt("id", ID);
		playerData.putInt("level", level);
		playerData.putUtfString("name", name);
		playerData.putUtfString("config", configuration);
		transform.toSFSObject(playerData);
		playerData.putInt("clan", clanID);
	}
	
	public void disconnect(SFSExtension extension)
	{
		this.isDisconnecting = true;
		
		updateDB(extension);

		try 
		{
			Group group = this.getGroup();
			if (group != null)
				group.removePlayer(this);
			
			PendingGroup pendingGroup = this.getPendingGroup();
			if (pendingGroup != null)
				pendingGroup.removePlayer(this);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		this.isDisconnecting = false;
	}
	
	public void updateDB(SFSExtension extension)
	{
		this.updatePlayerDB(extension);
		this.inventory.updateDB(extension);
		this.quests.updateDB(extension);		
	}
	
	public void updatePlayerDB(SFSExtension extension)
	{
		int dbId = this.getDBID();		
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"UPDATE player_data SET Configuration = ?, " +
	        			"Level = ?, EXP = ?, Skill = ?, " +
	        			"PositionX = ?, PositionY = ?, PositionZ = ?, " +
	        			"RotationX = ?, RotationY = ?, RotationZ = ? " +
	        			"WHERE ID = ?"
	        		);
	        stmt.setString(1, this.getConfig());
	        stmt.setInt(2, this.getLevel());
	        stmt.setLong(3, this.getEXP());
	        stmt.setInt(4, this.getSkill().ordinal());
	        try {
		        float[] pos = this.transform.getPosition();
		        stmt.setFloat(5, pos[0]);
		        stmt.setFloat(6, pos[1]);
		        stmt.setFloat(7, pos[2]);
	        } catch (ArrayIndexOutOfBoundsException aioobe) {
	        	extension.trace("Something is wrong here. Skipping position.");
	        }
	        try {
		        float[] rot = this.transform.getRotation();
		        stmt.setFloat(8, rot[0]);
		        stmt.setFloat(9, rot[1]);
		        stmt.setFloat(10, rot[2]);
	        } catch (ArrayIndexOutOfBoundsException aioobe) {
	        	extension.trace("Something is wrong here. Skipping rotation.");
	        }
	        stmt.setInt(11, dbId);
	        // Execute query
			stmt.execute();
			
			if (this.getNumBuilt() > 0)
			{
		        PreparedStatement stmt2 = connection.prepareStatement(
	        			"SELECT NumBuilt FROM clan_data WHERE ID = ?"
	        		);
		        stmt2.setInt(1, this.getClanID());
		        ResultSet res = stmt2.executeQuery();
		        
		        if (res.first())
		        {
		        	int newNum = res.getInt("NumBuilt") + this.getNumBuilt();
			        PreparedStatement stmt3 = connection.prepareStatement(
			        			"UPDATE clan_data SET NumBuilt = ? WHERE ID = ?"
			        		);
			        stmt3.setInt(1, newNum);
			        stmt3.setInt(2, this.getClanID());
					stmt3.execute();
		        }
		        else
		        {
			        PreparedStatement stmt3 = connection.prepareStatement(
		        			"INSERT INTO clan_data (ID, NumBuilt) VALUES (?, ?)"
		        		);
			        stmt3.setInt(1, this.getClanID());
			        stmt3.setInt(2, this.getNumBuilt());
					stmt3.execute();
		        }
			}
			
			// Return connection to the DBManager connection pool
			connection.close();
        }
        catch (SQLException e)
        {
        	SFSErrorData errData = new SFSErrorData(SFSErrorCode.GENERIC_ERROR);
        	errData.addParameter("SQL Error: " + e.getMessage());
        	extension.trace("A SQL Error occurred: " + e.getMessage());
        }		
	}
}
