package zoara.sfs2x.extension.social;

import java.sql.Timestamp;

public class Chat 
{
	private String roomName = null;
	private String playerName = "";
	private String recipientName = "";
	private String message = "";
	private Timestamp time = null;
	private boolean PM = false;
	
	public Chat(String _roomName, String _playerName, String _message, Timestamp _time)
	{
		roomName = _roomName;
		playerName = _playerName;
		message = _message.replaceAll("'", "''");
		message = message.substring(0, Math.min(_message.length(), 255));
		time = _time;
	}
	
	public Chat(String _playerName, String _recipientName, String _message, Timestamp _time, boolean _PM)
	{
		playerName = _playerName;
		recipientName = _recipientName;
		message = _message;
		time = _time;
		PM = _PM;
	}

	public String getRoom()
	{
		return roomName;
	}
	
	public String getPlayer()
	{
		return playerName;
	}
	
	public String getRecipient()
	{
		return recipientName;
	}
	
	public String getMessage()
	{
		return message;
	}
	
	public Timestamp getTime()
	{
		return time;
	}
	
	public boolean isPM()
	{
		return PM;
	}
}
