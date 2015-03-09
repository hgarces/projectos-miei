package contactserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface IContactServer extends Remote {

	public boolean registerServer(String server, String serverURL, String user) throws RemoteException;

	public boolean addPermission(String server, String user) throws RemoteException;

	public boolean remPermission(String server, String user) throws RemoteException;

	public String getServerURL(String server) throws RemoteException;

	public Map<String, ServerInfo> getServers() throws RemoteException;

	boolean hasAccess(String server, String user) throws RemoteException;

	List<String> getServersList() throws RemoteException; 

}
