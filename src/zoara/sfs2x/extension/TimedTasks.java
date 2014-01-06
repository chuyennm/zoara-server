package zoara.sfs2x.extension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;

import zoara.sfs2x.extension.social.Chat;

public class TimedTasks implements Runnable
{
    private ZoaraExtension extension;
    
    public TimedTasks(ZoaraExtension _extension)
    {
    	extension = _extension;
    }
     
    public void run()
    {
    	extension.trace("Running scheduled tasks...");
    	
        extension.getWorld().checkIfPlayersStillConnected();
        
        boolean pingedDB = false;
        
        List<Chat> chatLog = extension.getChatLog();
        if (chatLog.size() > 0)
        {
        	processChatLog(chatLog);
        	pingedDB = true;
        }
        extension.resetChatLog();
        
        List<Chat> pmLog = extension.getPMLog();
        if (pmLog.size() > 0)
        {
        	processPMLog(pmLog);
        	pingedDB = true;
        }
        extension.resetPMLog();
        
        if (!pingedDB)
        	pingDatabase();
    }
    
    private void processChatLog(List<Chat> chatLog)
    {
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        Statement stmt = connection.createStatement();
	        connection.setAutoCommit(false);
	        
	        for (Chat chat : chatLog)
	        {
		        stmt.addBatch("INSERT INTO public_messages " +
	        			"(Server, Room, Player, Message, Time) VALUES ('" + 
	        			extension.getParentZone().getName() +
	        			"', '" + chat.getRoom() +
	        			"', '" + chat.getPlayer() +
	        			"', '" + chat.getMessage() +
	        			"', '" + chat.getTime() +
    					"')");
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
    
    private void processPMLog(List<Chat> pmLog)
    {
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        Statement stmt = connection.createStatement();
	        connection.setAutoCommit(false);
	        
	        for (Chat pm : pmLog)
	        {
		        stmt.addBatch("INSERT INTO private_messages " +
	        			"(Server, Player, Recipient, Message, Time) VALUES ('" + 
	        			extension.getParentZone().getName() +
	        			"', '" + pm.getPlayer() +
	        			"', '" + pm.getRecipient() +
	        			"', '" + pm.getMessage() +
	        			"', '" + pm.getTime() +
	        			"')");
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
    
    @SuppressWarnings("unused")
	private void pingDatabase()
    {
		IDBManager dbManager = extension.getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        PreparedStatement stmt = connection.prepareStatement(
	        			"SELECT COUNT(*) FROM muppets"
	        		);
	        // Execute query
			ResultSet res = stmt.executeQuery();
			
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
