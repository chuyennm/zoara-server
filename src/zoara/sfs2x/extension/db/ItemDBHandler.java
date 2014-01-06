package zoara.sfs2x.extension.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.simulation.item.InventoryItem;
import zoara.sfs2x.extension.simulation.item.WorldItem;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class ItemDBHandler 
{	
	public static void addWorldItem(SFSExtension extension, WorldItem worldItem)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"INSERT INTO world_objects " +
	        			"(TemplateID, UniqueItemID, PositionX, PositionY, PositionZ, " +
	        			"RotationX, RotationY, RotationZ, Grabbable, Skill) " + 
	        			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
	        		);
	        stmt.setInt(1, worldItem.getTemplateID());
	        stmt.setString(2, worldItem.getUniqueID());
	        stmt.setFloat(3, worldItem.transform.getPosition()[0]);
	        stmt.setFloat(4, worldItem.transform.getPosition()[1]);
	        stmt.setFloat(5, worldItem.transform.getPosition()[2]);
	        stmt.setFloat(6, worldItem.transform.getRotation()[0]);
	        stmt.setFloat(7, worldItem.transform.getRotation()[1]);
	        stmt.setFloat(8, worldItem.transform.getRotation()[2]);
	        stmt.setBoolean(9, worldItem.isGrabbable());
	        stmt.setBoolean(10, worldItem.isSkillItem());
	        // Execute query
			stmt.execute();
			
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

	public static void destroyWorldItem(SFSExtension extension, WorldItem item)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"UPDATE world_objects SET Destroyed = ?, DestroyTime = NOW() " +
	        			"WHERE uid = ?"
	        		);
	        stmt.setBoolean(1, item.isDestroyed());
	        stmt.setInt(2, item.getDBID());
	        // Execute query
			stmt.execute();
			
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

	public static void updateWorldItem(SFSExtension extension, WorldItem item)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"UPDATE world_objects SET " +
	        			"PositionX = ?, PositionY = ?, PositionZ = ?, " +
	        			"RotationX = ?, RotationY = ?, RotationZ = ? " +
	        			"WHERE uid = ?"
	        		);
	        try {
		        float[] pos = item.transform.getPosition();
		        stmt.setFloat(1, pos[0]);
		        stmt.setFloat(2, pos[1]);
		        stmt.setFloat(3, pos[2]);
	        } catch (ArrayIndexOutOfBoundsException aioobe) {
	        	extension.trace("Something is wrong here. Skipping position.");
				connection.close();
				return;
	        }
	        try {
		        float[] rot = item.transform.getRotation();
		        stmt.setFloat(4, rot[0]);
		        stmt.setFloat(5, rot[1]);
		        stmt.setFloat(6, rot[2]);
	        } catch (ArrayIndexOutOfBoundsException aioobe) {
	        	extension.trace("Something is wrong here. Skipping rotation.");
				connection.close();
				return;
	        }
	        stmt.setInt(7, item.getDBID());
	        // Execute query
			stmt.execute();
			
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

	public static void getWorldItems(SFSExtension extension, World world)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"SELECT * FROM world_objects ORDER BY uid ASC"
	        		);
	        // Execute query
			ResultSet res = stmt.executeQuery();
			
			while (res.next())
			{
				String uniqueID = res.getString("UniqueItemID");
				boolean isDestroyed = res.getBoolean("Destroyed");
				WorldItem worldItem;
				if (isDestroyed) {
					worldItem = world.addDestroyedWorldItem(uniqueID);
				} else {
					worldItem = world.addWorldItem(uniqueID);
				}
				worldItem.setDBID(res.getInt("uid"));
				worldItem.setTemplateID(res.getInt("TemplateID"));
				worldItem.setUniqueID(uniqueID);
				worldItem.transform.setPositionX(res.getFloat("PositionX"));
				worldItem.transform.setPositionY(res.getFloat("PositionY"));
				worldItem.transform.setPositionZ(res.getFloat("PositionZ"));
				worldItem.transform.setRotationX(res.getFloat("RotationX"));
				worldItem.transform.setRotationY(res.getFloat("RotationY"));
				worldItem.transform.setRotationZ(res.getFloat("RotationZ"));
				worldItem.setGrabbable(res.getBoolean("Grabbable"));
				worldItem.setSkillItem(res.getBoolean("Skill"));
				worldItem.setDestroyed(isDestroyed);
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

	public static void addInventoryItem(SFSExtension extension, InventoryItem item)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"INSERT INTO player_inventory " +
	        			"(PlayerID, TemplateID, UniqueItemID, InventoryIndex, Quantity) " + 
	        			"VALUES (?, ?, ?, ?, ?)"
	        		);
	        stmt.setInt(1, item.getPlayer().getDBID());
	        stmt.setInt(2, item.getTemplateID());
	        stmt.setString(3, item.getUniqueID());
	        stmt.setInt(4, item.getInventoryIndex());
	        stmt.setInt(5, item.getQuantity());
	        // Execute query
			stmt.execute();
			
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

	public static void updateInventoryItem(SFSExtension extension, InventoryItem item)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"UPDATE player_inventory SET InventoryIndex = ?, Quantity = ? " +
	        			"WHERE uid = ?"
	        		);
	        stmt.setInt(1, item.getInventoryIndex());
	        stmt.setInt(2, item.getQuantity());
	        stmt.setInt(3, item.getDBID());
	        // Execute query
			stmt.execute();
			
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

	public static void updateInventoryItems(SFSExtension extension, List<InventoryItem> items)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        Statement stmt = connection.createStatement();
	        connection.setAutoCommit(false);
	        
	        for (InventoryItem item : items)
	        {
		        stmt.addBatch("UPDATE player_inventory " + 
		        		"SET InventoryIndex = " + item.getInventoryIndex() + 
		        		", Quantity = " + item.getQuantity() + " " +
	        			"WHERE uid = " + item.getDBID());
	        }
	        
	        // Execute query
			stmt.executeBatch();
			connection.commit();
			
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

	public static void removeInventoryItem(SFSExtension extension, InventoryItem item)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();

	        PreparedStatement stmt = connection.prepareStatement(
	        			"DELETE FROM player_inventory WHERE uid = ?"
	        		);
	        stmt.setInt(1, item.getDBID());
	        // Execute query
			stmt.execute();
			
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

	public static void removeInventoryItems(SFSExtension extension, List<InventoryItem> items)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        Statement stmt = connection.createStatement();
	        connection.setAutoCommit(false);
	        
	        for (InventoryItem item : items)
	        {
		        stmt.addBatch("DELETE FROM player_inventory WHERE uid = " + item.getDBID());
	        }
	        
	        // Execute query
			stmt.executeBatch();
			connection.commit();
			
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

	public static void getPlayerInventory(SFSExtension extension, ActivePlayer player)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"SELECT * FROM player_inventory " +
	        			"WHERE PlayerID = ? " +
	        			"ORDER BY InventoryIndex ASC"
	        		);
	        stmt.setInt(1, player.getDBID());
	        // Execute query
			ResultSet res = stmt.executeQuery();
			
			while (res.next())
			{
				InventoryItem item = player.inventory.addItem(res.getInt("InventoryIndex"), true);
				item.setDBID(res.getInt("uid"));
				item.setTemplateID(res.getInt("TemplateID"));
				item.setUniqueID(res.getString("UniqueItemID"));
				//item.setInventoryIndex(res.getInt("InventoryIndex"));
				item.setQuantity(res.getInt("Quantity"));
				item.setNew(false); // this was retrieved from the DB. important later.
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
