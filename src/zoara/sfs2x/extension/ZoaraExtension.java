package zoara.sfs2x.extension;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import zoara.sfs2x.extension.handlers.*;
import zoara.sfs2x.extension.handlers.group.*;
import zoara.sfs2x.extension.handlers.items.*;
import zoara.sfs2x.extension.handlers.player.*;
import zoara.sfs2x.extension.handlers.quests.*;
import zoara.sfs2x.extension.simulation.World;
import zoara.sfs2x.extension.social.*;
import zoara.sfs2x.extension.utils.UserHelper;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.SFSRoomRemoveMode;
import com.smartfoxserver.v2.entities.SFSRoomSettings;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class ZoaraExtension extends SFSExtension
{
	public static final String DATABASE_ID = "dbID";
	public static final String CLAN_ID = "clanID";
	
	private World world;
	private List<Chat> chatLog = new ArrayList<Chat>();
	private List<Chat> pmLog = new ArrayList<Chat>();
	
	@SuppressWarnings("unused")
	private ScheduledFuture<?> taskHandle;
	
	@Override
	public void init()
	{
		trace("Initializing extension! So far so good.");
		
		world = new World(this);
		
		addEventHandler(SFSEventType.SERVER_READY, ServerReadyEventHandler.class);
		addEventHandler(SFSEventType.USER_LOGIN, LoginEventHandler.class);
		addEventHandler(SFSEventType.USER_JOIN_ZONE, ZoneJoinEventHandler.class);
		
		addRequestHandler("useUDP", UserUDPHandler.class);
		
		addRequestHandler("newPlayer", NewPlayerHandler.class);
		addRequestHandler("updateLevelEXP", PlayerLevelXPHandler.class);
		addRequestHandler("setSkill", PlayerSkillHandler.class);
		addRequestHandler("toClanHQ", PlayerClanHQHandler.class);
		addRequestHandler("backToGameWorld", BackToGameWorldHandler.class);

		addRequestHandler("updateTransform", PlayerTransformHandler.class);
		addRequestHandler("updateAnimation", PlayerAnimationHandler.class);
		addRequestHandler("updateConfig", PlayerConfigurationHandler.class);
		addRequestHandler("setInClan", PlayerInClanHandler.class);
		
		addRequestHandler("addInventoryItem", NewInventoryItemHandler.class);
		addRequestHandler("updateInventoryItem", UpdateInventoryItemHandler.class);
		addRequestHandler("removeInventoryItem", RemoveInventoryItemHandler.class);
		
		addRequestHandler("addWorldItem", NewWorldItemHandler.class);
		addRequestHandler("updateWorldItem", UpdateWorldItemHandler.class);
		addRequestHandler("destroyWorldItem", DestroyWorldItemHandler.class);
		
		addRequestHandler("addQuest", NewQuestHandler.class);
		addRequestHandler("updateQuest", UpdateQuestHandler.class);
		addRequestHandler("removeQuest", RemoveQuestHandler.class);
		
		addRequestHandler("newGroupRequest", NewGroupRequestHandler.class);
		addRequestHandler("approveGroupRequest", ApproveGroupRequestHandler.class);
		addRequestHandler("rejectGroupRequest", RejectGroupRequestHandler.class);
		addRequestHandler("leaveGroupRequest", LeaveGroupRequestHandler.class);
		addRequestHandler("recipeVoteRequest", RecipeVoteRequestHandler.class);
		addRequestHandler("recipeVoteRevokeRequest", RecipeVoteRevokeHandler.class);
		addRequestHandler("buildInfrastructureRequest", AddInfrastructureHandler.class);
		
		addEventHandler(SFSEventType.PUBLIC_MESSAGE, PublicMessageEventHandler.class);
		addEventHandler(SFSEventType.PRIVATE_MESSAGE, PrivateMessageEventHandler.class);

		addEventHandler(SFSEventType.USER_DISCONNECT, OnUserGoneHandler.class);
		addEventHandler(SFSEventType.USER_LOGOUT, OnUserGoneHandler.class);
		
		SmartFoxServer sfs = SmartFoxServer.getInstance();
		taskHandle = sfs.getTaskScheduler().scheduleAtFixedRate(new TimedTasks(this), 
				5, 10, TimeUnit.MINUTES);
	}
	
	@Override
	public void destroy()
	{
	    super.destroy();
	    world = null;
	}
	
	public World getWorld()
	{
		return world;
	}
	
	public Room createRoom(String roomName, boolean isGroup) throws SFSCreateRoomException
	{
		CreateRoomSettings params = new CreateRoomSettings();
		params.setName(roomName);
		params.setGame(false);
		if (isGroup)
			params.setGroupId("groups");
		else
			params.setGroupId("default");
		params.setMaxUsers(100);
		params.setAutoRemoveMode(SFSRoomRemoveMode.WHEN_EMPTY);
		params.setHidden(true);
		params.setUseWordsFilter(true);
		params.setRoomSettings(EnumSet.of(
				SFSRoomSettings.PUBLIC_MESSAGES, SFSRoomSettings.USER_COUNT_CHANGE_EVENT, 
				SFSRoomSettings.USER_ENTER_EVENT, SFSRoomSettings.USER_EXIT_EVENT));
		Room room = getApi().createRoom(getParentZone(), params, null);
		return room;
	}
	
	public Room createGroupRoom()
	{
		Random rand = new Random();
		String roomName = "Group " + rand.nextInt(1000);
		while (getParentZone().getRoomByName(roomName) != null) {
			roomName = "Group " + rand.nextInt(1000);
		}
		try {
			Room room = createRoom(roomName, true);
			return room;
		}
		catch (SFSCreateRoomException e) {
			trace(e.getMessage());
		}
		return null;
	}
	
	public void send(String cmdName, ISFSObject params, List<User> recipients)
	{
		for (User user : recipients)
		{
			if (user == null || !user.isConnected()) continue;

			boolean useUDP = false;
			if (user.containsVariable("useUDP"))
				useUDP = user.getVariable("useUDP").getBoolValue();
			
			send(cmdName, params, user, useUDP);
		}
	}
	
	public void sendAll(String cmdName, ISFSObject params)
	{
		List<User> recipients = UserHelper.getAllUsersList(getParentZone());
		if (recipients != null && recipients.size() > 0)
			send(cmdName, params, recipients);
	}
	
	public void sendAll(String cmdName, ISFSObject params, User fromUser)
	{
		List<User> recipients = UserHelper.getAllUsersList(getParentZone(), fromUser);
		if (recipients != null && recipients.size() > 0)
			send(cmdName, params, recipients);
	}
	
	public void addToChatLog(Chat newChat)
	{
		chatLog.add(newChat);
	}
	
	public void addToPMLog(Chat newChat)
	{
		pmLog.add(newChat);
	}
	
	public List<Chat> getChatLog()
	{
		return chatLog;
	}
	
	public List<Chat> getPMLog()
	{
		return pmLog;
	}
	
	public void resetChatLog()
	{
		chatLog.clear();
	}
	
	public void resetPMLog()
	{
		pmLog.clear();
	}
}
