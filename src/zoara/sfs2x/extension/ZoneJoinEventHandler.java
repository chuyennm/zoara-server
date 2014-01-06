package zoara.sfs2x.extension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import zoara.sfs2x.extension.handlers.PlayerClanHQHandler;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.utils.RoomHelper;

import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.db.IDBManager;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.SFSRoomSettings;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

@SuppressWarnings("unused")
public class ZoneJoinEventHandler extends BaseServerEventHandler
{
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException
	{
		World world = RoomHelper.getWorld(this);
		if (world.size() == 0) {
			world.loadItems();
		}
		
		User user = (User) event.getParameter(SFSEventParam.USER);

		// dbid is a hidden UserVariable, available only server side
		UserVariable uv_dbId = new SFSUserVariable("dbid", 
				user.getSession().getProperty(ZoaraExtension.DATABASE_ID), false);
		int dbId = uv_dbId.getIntValue();
		
		//Arrays.asList(uv_dbId)
		ArrayList<UserVariable> vars = new ArrayList<UserVariable>();
		vars.add(uv_dbId);

		IDBManager dbManager = getParentExtension().getParentZone().getDBManager();
		Connection connection;
        try
        {
        	// Grab a connection from the DBManager connection pool
	        connection = dbManager.getConnection();
	        
	        // Second query: player_data table
	        PreparedStatement stmt = connection.prepareStatement(
	        			"SELECT Configuration, Level, EXP, Skill, " +
	        			"PositionX, PositionY, PositionZ, RotationX, RotationY, RotationZ " +
	        			"FROM player_data WHERE ID = ?"
	        		);
	        stmt.setInt(1, dbId);
	        // Execute query
			ResultSet res = stmt.executeQuery();
			
			// If no record was found, this was their first time logging in to the game
			if (!res.first())
			{
				PreparedStatement stmt2 = connection.prepareStatement(
	        			"SELECT Gender FROM player_info WHERE ID = ?"
	        		);
		        stmt2.setInt(1, dbId);
				ResultSet res2 = stmt2.executeQuery();
				String gender = null;
				if (res2.first()) { gender = res2.getString("Gender"); }
	        
				if (gender != null)
				{
					PreparedStatement stmt3 = connection.prepareStatement(
		        			"INSERT INTO player_data (ID, Configuration) VALUES (?, ?)"
			        	);
			        stmt3.setInt(1, dbId);
			        stmt3.setString(2, gender.toLowerCase());
			        // Execute query
					stmt3.execute();
					
					UserVariable uv_config = new SFSUserVariable("config", gender.toLowerCase(), false);
					vars.add(uv_config);
					
					/*SFSObject data = new SFSObject();
					data.putUtfString("config", gender.toLowerCase());
					updateUser(user, data);*/
				}
				else
				{
					PreparedStatement stmt3 = connection.prepareStatement(
		        			"INSERT INTO player_data (ID) VALUES (?)"
			        	);
			        stmt3.setInt(1, dbId);
			        // Execute query
					stmt3.execute();
				}
				UserVariable uv_firstLogin = new SFSUserVariable("firstLogin", true, false);
				vars.add(uv_firstLogin);
			}
			else
			{
				String config = res.getString("Configuration");
				int level = res.getInt("Level");
				int exp = res.getInt("EXP");
				int skill = res.getInt("Skill");
				float posx = res.getFloat("PositionX");
				float posy = res.getFloat("PositionY");
				float posz = res.getFloat("PositionZ");
				float rotx = res.getFloat("RotationX");
				float roty = res.getFloat("RotationY");
				float rotz = res.getFloat("RotationZ");
				
				if (config != null)
				{
					UserVariable uv_config = new SFSUserVariable("config", config, false);
					vars.add(uv_config);
				}
				if (level != 0)
				{
					UserVariable uv_level = new SFSUserVariable("level", level, false);
					vars.add(uv_level);
				}
				if (posx != 0 && posy != 0 && posz != 0)
				{
					//List<Float> position = Arrays.asList(posx, posy, posz);
					SFSArray posa = new SFSArray();
					posa.addFloat(posx);
					posa.addFloat(posy);
					posa.addFloat(posz);
					//posa.addFloatArray(position);
					UserVariable uv_pos = new SFSUserVariable("position", posa, false);
					vars.add(uv_pos);
				}
				if (rotx != 0 || roty != 0 || rotz != 0)
				{
					//List<Float> rotation = Arrays.asList(rotx, roty, rotz);
					SFSArray rota = new SFSArray();
					rota.addFloat(rotx);
					rota.addFloat(roty);
					rota.addFloat(rotz);
					//rota.addFloatArray(rotation);
					UserVariable uv_rot = new SFSUserVariable("rotation", rota, false);
					vars.add(uv_rot);
				}
				UserVariable uv_exp = new SFSUserVariable("EXP", exp, false);
				vars.add(uv_exp);
				UserVariable uv_skill = new SFSUserVariable("skill", skill, false);
				vars.add(uv_skill);
				
				/*SFSObject data = SFSObject.newFromResultSet(res);
				data.putInt("id", user.getId());
				updateUser(user, data);*/
			}
			
			if (user.getSession().getProperty(ZoaraExtension.CLAN_ID) != null)
			{
				UserVariable uv_clanId = new SFSUserVariable("clanID", 
						user.getSession().getProperty(ZoaraExtension.CLAN_ID), false);
				int clanId = uv_clanId.getIntValue();
				
				// get clan data
		        PreparedStatement stmt4 = connection.prepareStatement(
		        			"SELECT CONCAT(Name, ' ', NameSuffix) AS ClanName " +
		        			"FROM clan_info WHERE ID = ?"
		        		);
		        stmt4.setInt(1, clanId);
		        // Execute query
				ResultSet res4 = stmt4.executeQuery();
				
				if (res4.first())
				{
					String clanName = res4.getString("ClanName");

					vars.add(uv_clanId);
					UserVariable uv_clanName = new SFSUserVariable("clanName", clanName, false);
					vars.add(uv_clanName);
					
					Room clanRoom = getParentExtension().getParentZone().getRoomByName(clanName);
					if (clanRoom == null)
					{
						trace("We need to create a new room for " + clanName);
						clanRoom = PlayerClanHQHandler.createClanRoom(getApi(), 
								getParentExtension().getParentZone(), clanName);
					} else {
						trace("Room for " + clanName + " exists!"); 
					}
					user.subscribeGroup(clanName);

					/*SFSObject clanData = new SFSObject();
					clanData.putInt("id", clanId);
					clanData.putUtfString("name", clanName);
					this.send("retrievedClanData", clanData, user, true);*/
					trace("Found a clan. Sent information back to user.");
				}
			}
			
			// Return connection to the DBManager connection pool
			connection.close();
        }
        catch (SQLException e)
        {
        	SFSErrorData errData = new SFSErrorData(SFSErrorCode.GENERIC_ERROR);
        	errData.addParameter("SQL Error: " + e.getMessage());
        	trace("A SQL Error occurred: " + e.getMessage());
        }
	
		// Set the variables
		getApi().setUserVariables(user, vars);
		
		SFSObject data = new SFSObject();
		data.putInt("id", user.getId());
		updateUser(user, data);
		
		// Join the user
		Room lobby = getParentExtension().getParentZone().getRoomByName("Global Chat");
		
		if (lobby == null)
			throw new SFSException("The Global Chat Room was not found!");
		
		getApi().joinRoom(user, lobby);
		trace("Auto-joining Global Chat...");
		
		user.setPrivilegeId((short) 1);
	}

	// Send the transform to all the clients
	private void updateUser(User user, SFSObject data) 
	{
		this.send("zoneJoinResponse", data, user, true); // Use UDP = true
		
		trace("Sent user back information retrieved from database!");
	}
}
