package zoara.sfs2x.extension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.security.DefaultPermissionProfile;

public class LoginEventHandler extends BaseServerEventHandler
{
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException
	{
		// Grab parameters from client request
		String userName = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
		String cryptedPass = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);
		ISession session = (ISession) event.getParameter(SFSEventParam.SESSION);
		
		// Get password from DB
		IDBManager dbManager = getParentExtension().getParentZone().getDBManager();
		Connection connection;
		
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        // Build a prepared statement
	        PreparedStatement stmt = connection.prepareStatement(
	        			"SELECT Password, ID, ClanID, Zone FROM player_info WHERE Username = ?"
	        		);
	        stmt.setString(1, userName);
	        
	        // Execute query
			ResultSet res = stmt.executeQuery();
			
			// Verify that one record was found
			if (!res.first())
			{
				// This is the part that goes to the client
				SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
				errData.addParameter(userName);
				
				// This is logged on the server side
				throw new SFSLoginException("Bad username: " + userName, errData);
			}
			
			String dbpassword = res.getString("Password");
			int dbId = res.getInt("ID");
			//String zone = res.getString("Zone");
			int clanId = res.getInt("ClanID");
			String zone = res.getString("Zone");
			
			// Return connection to the DBManager connection pool
			connection.close();
			
			String thisZone = getParentExtension().getParentZone().getName();
			if ((zone.equals("Adult") && !zone.equals(thisZone)) || 
				(!zone.equals("Adult") && thisZone.equals("Adult")))
			{
				SFSErrorData data = new SFSErrorData(SFSErrorCode.JOIN_GAME_ACCESS_DENIED);
				data.addParameter(thisZone);
				
				throw new SFSLoginException("Login failed. User "  + userName + 
						" is not a member of Server " + thisZone, data);
			}
			
			World world = RoomHelper.getWorld(this);
			if (world.hasPlayer(userName))
			{
				SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_ALREADY_LOGGED);
				String[] params = { userName, thisZone };
				data.setParams(Arrays.asList(params));
				
				throw new SFSLoginException("Login failed: " + userName + 
						" is already logged in!", data);
			}
				
			// Verify the secure password
			if (!getApi().checkSecurePassword(session, dbpassword, cryptedPass))
			{
				if (dbId < 10) 
				{
					trace("Passwords did not match, but logging in anyway.");
				}
				else 
				{
					SFSErrorData data = new SFSErrorData(SFSErrorCode.LOGIN_BAD_PASSWORD);
					data.addParameter(userName);
					
					throw new SFSLoginException("Login failed for user: "  + userName, data);
				}
			}
			
			// Store the client dbId in the session
			session.setProperty(ZoaraExtension.DATABASE_ID, dbId);
			if (clanId != 0) {
				session.setProperty(ZoaraExtension.CLAN_ID, clanId);
			}
			
			session.setProperty("$permission", DefaultPermissionProfile.STANDARD);
        }
        catch (SQLException e) // User name was not found
        {
        	SFSErrorData errData = new SFSErrorData(SFSErrorCode.GENERIC_ERROR);
        	errData.addParameter("SQL Error: " + e.getMessage());
        	
        	throw new SFSLoginException("A SQL Error occurred: " + e.getMessage(), errData);
        }
	}
}
