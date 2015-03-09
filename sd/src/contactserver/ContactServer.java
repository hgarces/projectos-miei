package contactserver;

import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Sistemas Distribuidos
 * 
 * @author Rui Queiros - 41717
 * @author Henrique Garces - 42077
 */
public class ContactServer extends UnicastRemoteObject implements IContactServer {

	private static final long serialVersionUID = 1L;
	static String server;
	static String user; 
	static final Map<String, ServerInfo> servers = new HashMap<String, ServerInfo>(); 
	static final List<String> serversName = new LinkedList<String>(); 

	protected ContactServer() throws RemoteException {
		super();
	}

	@Override
	public boolean registerServer(String server, String url, String user)
			throws RemoteException {
		if(server != null && user != null) {
			if(!servers.containsKey(server)) { //se o servidor nao existe cria um novo
				servers.put(server, new ServerInfo(url, user));
				serversName.add(server);
				System.out.println("New server registered: "+server+"@"+user);
				Registry reg = LocateRegistry.getRegistry();
				String[] t = reg.list();
				for(int i = 0; i < t.length; i++) {
					System.out.println(t[i]);
				}
				return true; 
			}
		}
		return false;
	}

	@Override
	public boolean addPermission(String server, String user) {
		List<String> list = new LinkedList<String>();
		if(server != null && user != null) {
			if(servers.containsKey(server)) { 	
				list = servers.get(server).getAllowedUsers();
				for(int i = 0; i < list.size(); i++) {
					if(!list.get(i).equals(user)) { 
						//se o servidor existe, adiciona um novo utilizador
						list.add(user); 
						System.out.println(user + " added to the list of users "
								+ "with permissions on the server " + server);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean remPermission(String server, String user) {
		List<String> list = new LinkedList<String>(); 
		if(server != null && user != null) {
			if(servers.containsKey(server)) {		
				list = servers.get(server).getAllowedUsers();
				for(int i = 0; i < list.size(); i++) {
					if(list.get(i).equals(user)) {
						list.remove(user);  
						System.out.println(user + "removed from the list of users "
								+ "with permissions on the server" + server);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String getServerURL(String server) throws RemoteException {
		if(server != null) {
			if(servers.containsKey(server)) {
				return servers.get(server).getURL(); 
			}
		}
		return null;
	}

	@Override
	public Map<String, ServerInfo> getServers() throws RemoteException {
		return servers;
	}

	@Override
	public List<String> getServersList() throws RemoteException {
		return serversName;

	}

	@Override
	public boolean hasAccess(String server, String user) throws RemoteException {
		if(user != null) {
			if(servers.containsKey(server)) {
				ServerInfo l = servers.get(server);
				if(l.allowedUsers.contains(user)) 	
					return true; 
			}
		}
		return false; 
	}

	public static void main(String[] args) {
		try {
			System.out.println("Initializing..."); 

			System.getProperties().put( "java.security.policy", "util/policy.all");
			if( System.getSecurityManager() == null) {
				System.setSecurityManager( new RMISecurityManager());
			}
			try { // start rmiregistry
				LocateRegistry.createRegistry( 1099);
			} catch( RemoteException e) { 
				// if not start it
				// do nothing - already started with rmiregistry
			}
			ContactServer cs = new ContactServer();
			Naming.rebind("contactServer", cs);
			InetAddress localHost = InetAddress.getLocalHost();
			System.out.println( "Contact server bound in registry at //"+localHost.getHostAddress()+"/contactServer");
		} catch( Throwable th) {
			th.printStackTrace();
		}
	}
}
