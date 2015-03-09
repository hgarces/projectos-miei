package contactserver;

import java.util.LinkedList;
import java.util.List;

public class ServerInfo {

	String url;
	List<String> allowedUsers;

	public ServerInfo(String url, String owner) {
		this.url = url;
		allowedUsers = new LinkedList<String>();
		allowedUsers.add(owner);
	}

	public String getURL() {
		return url;
	}

	public String getOwner() {
		return allowedUsers.get(0);
	}

	public List<String> getAllowedUsers() {
		return allowedUsers;
	}
}
