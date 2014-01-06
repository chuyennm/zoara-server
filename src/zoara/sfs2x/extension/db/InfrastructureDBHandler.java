package zoara.sfs2x.extension.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import zoara.sfs2x.extension.simulation.Recipe;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.simulation.item.Infrastructure;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class InfrastructureDBHandler 
{
	public static void addInfrastructure(SFSExtension extension, Infrastructure infrastructure)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"INSERT INTO world_infrastructure " +
	        			"(TemplateID, UniqueItemID, WorldPositionX, WorldPositionY, WorldPositionZ, " +
	        			"WorldRotationX, WorldRotationY, WorldRotationZ, Zone) " + 
	        			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
	        		);
	        stmt.setInt(1, infrastructure.getTemplateID());
	        stmt.setString(2, infrastructure.getUniqueID());
	        stmt.setFloat(3, infrastructure.transform.getPosition()[0]);
	        stmt.setFloat(4, infrastructure.transform.getPosition()[1]);
	        stmt.setFloat(5, infrastructure.transform.getPosition()[2]);
	        stmt.setFloat(6, infrastructure.transform.getRotation()[0]);
	        stmt.setFloat(7, infrastructure.transform.getRotation()[1]);
	        stmt.setFloat(8, infrastructure.transform.getRotation()[2]);
	        stmt.setString(9, extension.getParentZone().getName());
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

	public static void updateInfrastructure(SFSExtension extension, Infrastructure item)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"UPDATE world_infrastructure SET " +
	        			"WorldPositionX = ?, WorldPositionY = ?, WorldPositionZ = ?, " +
	        			"WorldRotationX = ?, WorldRotationY = ?, WorldRotationZ = ? " +
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

	public static void getInfrastructure(SFSExtension extension, World world)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"SELECT * FROM world_infrastructure " +
	        			"WHERE Zone = ? " +
	        			"ORDER BY uid ASC"
	        		);
	        stmt.setString(1, extension.getParentZone().getName());
	        // Execute query
			ResultSet res = stmt.executeQuery();
			
			while (res.next())
			{
				int templateID = res.getInt("TemplateID");
				Infrastructure infrastructure = world.addInfrastructure(templateID);
				infrastructure.setDBID(res.getInt("uid"));
				infrastructure.setUniqueID(res.getString("UniqueItemID"));
				infrastructure.transform.setPositionX(res.getFloat("WorldPositionX"));
				infrastructure.transform.setPositionY(res.getFloat("WorldPositionY"));
				infrastructure.transform.setPositionZ(res.getFloat("WorldPositionZ"));
				infrastructure.transform.setRotationX(res.getFloat("WorldRotationX"));
				infrastructure.transform.setRotationY(res.getFloat("WorldRotationY"));
				infrastructure.transform.setRotationZ(res.getFloat("WorldRotationZ"));
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

	public static void getRecipes(SFSExtension extension, World world)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"SELECT * FROM multiplayer_recipes ORDER BY TemplateID ASC"
	        		);
	        // Execute query
			ResultSet res = stmt.executeQuery();
			
			while (res.next())
			{
				Recipe task = world.addRecipe();
				task.setDBID(res.getInt("uid"));
				task.setTemplateID(res.getInt("TemplateID"));
				task.setName(res.getString("Name"));
				task.setDescription(res.getString("Description"));
				task.addResource(res.getInt("Item1Skill"), res.getString("Item1UniqueID"), 
						res.getInt("Item1Quantity"));
				task.addResource(res.getInt("Item2Skill"), res.getString("Item2UniqueID"), 
						res.getInt("Item2Quantity"));
				if (res.getString("Item3UniqueID") != null) 
				{
					task.addResource(res.getInt("Item3Skill"), res.getString("Item3UniqueID"), 
							res.getInt("Item3Quantity"));
				}
				if (res.getString("Item4UniqueID") != null) 
				{
					task.addResource(res.getInt("Item4Skill"), res.getString("Item4UniqueID"), 
							res.getInt("Item4Quantity"));
				}
				if (res.getString("Item5UniqueID") != null) 
				{
					task.addResource(res.getInt("Item5Skill"), res.getString("Item5UniqueID"), 
							res.getInt("Item5Quantity"));
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
