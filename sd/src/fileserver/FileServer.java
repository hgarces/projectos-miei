package fileserver;

import java.net.InetAddress;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.List;
import java.io.*;

import contactserver.IContactServer;
import util.FileInfo;
import util.InfoNotFoundException;
import util.LocalFileSystem;

/**
 * Sistemas Distribuidos
 * 
 * @author Rui Queiros - 41717
 * @author Henrique Garces - 42077
 */
public class FileServer extends UnicastRemoteObject implements IFileServer {

	private static final long serialVersionUID = 1L;

	public FileServer() throws RemoteException {
		super();
	}

	@Override
	public List<String> dir(String path) throws RemoteException,
	InfoNotFoundException {
		return LocalFileSystem.dir(path);
	}

	@Override
	public FileInfo getFileInfo(String path)
			throws RemoteException, InfoNotFoundException {
		return LocalFileSystem.getFileInfo(path);
	}

	@Override
	public byte[] getFile(String name)
			throws InfoNotFoundException, IOException {
		return LocalFileSystem.getFile(name);
	}

	@Override
	public boolean sendFile(byte[] b, String name) throws IOException, InfoNotFoundException {
		if(b.length > 0) {
			FileOutputStream f = new FileOutputStream(name); 
			f.write(b);
			f.close();
		}
		else
			throw new InfoNotFoundException("File not found!");
		return true;
	}

	@Override
	public boolean mkdir(String dir) throws RemoteException {
		if(!LocalFileSystem.mkdir(dir)) {
			System.out.println("Could not create directory ");
			return false;
		}
		return true;
	}

	@Override
	public boolean rmdir(String dir) throws RemoteException {
		if(LocalFileSystem.remove(dir) != 1) { 
			System.out.println("Could not delete directory ");
			return false;
		}
		return true;
	}

	@Override
	public boolean rm(String path) throws RemoteException {
		if(LocalFileSystem.remove(path) != 2) {
			System.out.println("Could not delete file");
			return false;
		}
		return true;
	}

	public static void main(String args[]) throws Exception {
		try {
			if( args.length != 3) {
				System.out.println( "Use: java FileServer server_name contactServer_url owner_name");
				System.exit(0);
			}
			String serverName = args[0];
			String contactServer = args[1];
			String ownerName = args[2];

			System.getProperties().put("java.security.policy", "util/policy.all");
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
			}

			String myIP = InetAddress.getLocalHost().getHostAddress();
			Registry reg = LocateRegistry.getRegistry(contactServer, 1099);

			IContactServer cserver = (IContactServer) Naming.lookup("//"+contactServer+"/contactServer");
			cserver.registerServer(serverName, myIP, ownerName);
			System.out.println("File Server bound in registry");
			reg.rebind(serverName, new FileServer());
		} catch (RemoteException e) { 
			System.err.println("Error: " + e.getMessage());
			System.err
			.println("\nNao foi possivel se conectar ao servidor.");
		} catch (NotBoundException e) {
			System.err.println("Error: " + e.getMessage());
			System.err
			.println("\nNao foi possivel obter uma referencia remota do servidor.");
		} catch (Throwable th) {
			th.printStackTrace();
		}
	}
}
