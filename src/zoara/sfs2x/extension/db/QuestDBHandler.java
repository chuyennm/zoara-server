package zoara.sfs2x.extension.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import zoara.sfs2x.extension.simulation.ActivePlayer;
import zoara.sfs2x.extension.simulation.Quest;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class QuestDBHandler 
{
	public static void addQuest(SFSExtension extension, Quest quest)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"INSERT INTO player_quests (PlayerID, " +
	        			"TemplateID, UniqueQuestID, Active, Completed, Failed, CurrentStepNumber) " + 
	        			"VALUES (?, ?, ?, ?, ?, ?, ?)"
	        		);
	        stmt.setInt(1, quest.getPlayer().getDBID());
	        stmt.setInt(2, quest.getTemplateID());
	        stmt.setString(3, quest.getUniqueID());
	        stmt.setBoolean(4, quest.isActive());
	        stmt.setBoolean(5, quest.isCompleted());
	        stmt.setBoolean(6, quest.isFailed());
	        stmt.setInt(7, quest.getCurrentStepNumber());
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

	public static void updateQuest(SFSExtension extension, Quest quest)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"UPDATE player_quests SET Active = ?, Completed = ?, Failed = ?, " +
	        			"CurrentStepNumber = ? WHERE uid = ?"
	        		);
	        stmt.setBoolean(1, quest.isActive());
	        stmt.setBoolean(2, quest.isCompleted());
	        stmt.setBoolean(3, quest.isFailed());
	        stmt.setInt(4, quest.getCurrentStepNumber());
	        stmt.setInt(5, quest.getDBID());
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

	public static void updateQuests(SFSExtension extension, List<Quest> quests)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        Statement stmt = connection.createStatement();
	        connection.setAutoCommit(false);
	        
	        for (Quest quest : quests)
	        {
		        stmt.addBatch("UPDATE player_quests " + 
		        		"SET Active = " + quest.isActive() + ", Completed = " + quest.isCompleted() +
		        		", Failed = " + quest.isFailed() + ", CurrentStepNumber = " + 
		        		quest.getCurrentStepNumber() + " WHERE uid = " + quest.getDBID()
		        );
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

	public static void removeQuest(SFSExtension extension, Quest quest)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();

	        PreparedStatement stmt = connection.prepareStatement(
	        			"DELETE FROM player_quests WHERE uid = ?"
	        		);
	        stmt.setInt(1, quest.getDBID());
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

	public static void removeQuests(SFSExtension extension, List<Quest> quests)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        Statement stmt = connection.createStatement();
	        connection.setAutoCommit(false);
	        
	        for (Quest quest : quests)
	        {
		        stmt.addBatch("DELETE FROM player_quests WHERE uid = " + quest.getDBID());
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

	public static void getPlayerQuests(SFSExtension extension, ActivePlayer player)
	{
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"SELECT * FROM player_quests " +
	        			"WHERE PlayerID = ? " +
	        			"ORDER BY TemplateID ASC"
	        		);
	        stmt.setInt(1, player.getDBID());
	        // Execute query
			ResultSet res = stmt.executeQuery();
			
			while (res.next())
			{
				Quest quest = player.quests.addQuest(res.getString("UniqueQuestID"), true);
				quest.setDBID(res.getInt("uid"));
				quest.setTemplateID(res.getInt("TemplateID"));
				quest.setUniqueID(res.getString("UniqueQuestID"));
				quest.setActive(res.getBoolean("Active"));
				quest.setCompleted(res.getBoolean("Completed"));
				quest.setFailed(res.getBoolean("Failed"));
				quest.setCurrentStepNumber(res.getInt("CurrentStepNumber"));
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