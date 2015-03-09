package client;

import java.io.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import util.FileInfo;
import util.InfoNotFoundException;
import util.LocalFileSystem;
import contactserver.IContactServer;
import fileserver.IFileServer;

/**
 * Sistemas Distribuidos
 * 
 * @author Rui Queiros - 41717
 * @author Henrique Garces - 42077
 */
public class FileClient {
	private String username;
	private IContactServer cs;
	private IFileServer fserver;

	protected FileClient(String url, String username) throws NotBoundException {
		try {
			this.username = username;
			System.setProperty("java.security.policy", "util/policy.all");
			cs = (IContactServer) Naming.lookup("//"+url+"/contactServer");
		} catch (RemoteException e) { 
			System.err.println( "Erro: " + e.getMessage());
			System.err.println("\nNao foi possivel se conectar ao servidor.");
			System.exit(1);
		} catch (NotBoundException e) {
			System.err.println("Erro: " + e.getMessage());
			System.err
			.println("\nNao foi possivel obter uma referencia remota do servidor.");
		} catch (MalformedURLException e) {
			System.err.println( "Erro: " + e.getMessage());
		} 
	}

	/**
	 * Devolve um array com os servidores a que o utilizador tem acesso.
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 */
	protected String[] servers() throws MalformedURLException, RemoteException, NotBoundException {
		try {
			List<String> lservers = new ArrayList<String>();  
			for(int i = 0; i < cs.getServersList().size(); i++) {
				if(cs.hasAccess((String) cs.getServersList().get(i), username)) {
					if(cs.getServersList().get(i) != null) {
						lservers.add(cs.getServersList().get(i)); 
					}
				}
			}
			if(lservers.size() > 0) {
				String[] userAccess = new String[lservers.size()];
				for(int i = 0; i < lservers.size(); i++) {
					userAccess[i] = lservers.get(i); 
				}
				return userAccess; 
			}
		} catch (RemoteException e) { 
			System.err.println( "Erro: " + e.getMessage());
			System.err.println("\nNao foi possivel se conectar ao servidor.");
			System.exit(1);
		} 
		return null; 
	}

	/**
	 * Adiciona o utilizador user a lista de utilizadores com autorizao para aceder ao servidor
	 * server.
	 * Devolve false em caso de erro.
	 * NOTA: n‹o deve lan�ar excepcao. 
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 */
	protected boolean addPermission( String server, String user) throws MalformedURLException, RemoteException, NotBoundException {
		try {
			if(cs.hasAccess(server, username)){
				if(cs.addPermission(server, user))
					return true;
			}
		} catch (RemoteException e) { 
			System.err.println( "Erro: " + e.getMessage());
			System.err.println("\nNao foi possivel se conectar ao servidor.");
			System.exit(1);
		}
		System.err.println( "exec: addPermission in server " + server + " for user " + user);
		return false;
	}

	/**
	 * Remove o utilizador user da lista de utilizadores com autoriza�‹o para aceder ao servidor
	 * server.
	 * Devolve false em caso de erro.
	 * NOTA: n‹o deve lan�ar excepcao. 
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 * @throws MalformedURLException 
	 */
	protected boolean remPermission( String server, String user) throws MalformedURLException, RemoteException, NotBoundException {
		try {
			if(cs.hasAccess(server, username)){
				if(cs.remPermission(server, user))
					return true;
			}
		} catch (RemoteException e) { 
			System.err.println( "Erro: " + e.getMessage());
			System.err.println("\nNao foi possivel se conectar ao servidor.");
			System.exit(1);
		}
		System.err.println( "exec: remPermission in server " + server + " for user " + user);
		return false; 
	}

	/**
	 * Devolve um array com os ficheiros/directoria na directoria dir no servidor server@user
	 * (ou no sistema de ficheiros do cliente caso server == null).
	 * Devolve null em caso de erro.
	 * NOTA: n‹o deve lan�ar excepcao. 
	 * @throws RemoteException 
	 * @throws NotBoundException 
	 * @throws InfoNotFoundException 
	 */
	protected List<String> dir( String server, String user, String dir) throws RemoteException, NotBoundException, InfoNotFoundException {
		if(dir != null) {
			if(server == null) {
				try {
					return LocalFileSystem.dir(dir);
				} catch (InfoNotFoundException e) {
					System.err.println( "Erro: " + e.getMessage());
				}
			}

			if(cs.hasAccess(server, username) && cs.hasAccess(server, user)){
				try {
					getServer(server);
					return fserver.dir(dir);

				} catch (RemoteException e) { 
					System.err.println( "Erro: " + e.getMessage());
					System.err.println("\nNao foi possivel se conectar ao servidor de ficheiros.");
				} catch( Exception e) {
					System.err.println( "Erro: " + e.getMessage());
				}
			}
		}
		System.err.println( "exec: ls " + dir + " no servidor " + server + "@" + user);
		return null; 
	}

	/**
	 * Cria a directoria dir no servidor server@user
	 * (ou no sistema de ficheiros do cliente caso server == null).
	 * Devolve false em caso de erro.
	 * NOTA: nao deve lancar excepcao. 
	 * @throws RemoteException 
	 */
	protected boolean mkdir( String server, String user, String dir) throws RemoteException {
		if(server == null) {
			return LocalFileSystem.mkdir(dir);
		}else {
			try {
				if(cs.hasAccess(server, user)) {
					try {
						getServer(server);
						return fserver.mkdir(dir);
					} catch (RemoteException | NotBoundException e) {
						System.err.println( "Erro: " + e.getMessage());
						System.err.println("\nNao foi possivel se conectar ao servidor de ficheiros.");
					}
				}
			} catch (RemoteException e) { 
				System.err.println( "Erro: " + e.getMessage());
				System.err.println("\nNao foi possivel se conectar ao servidor.");
			} 
		}
		System.err.println( "exec: mkdir " + dir + " no servidor " + server + "@" + user);
		return false;
	}

	/**
	 * Remove a directoria dir no servidor server@user
	 * (ou no sistema de ficheiros do cliente caso server == null).
	 * Devolve false em caso de erro.
	 * NOTA: nao deve lancar excepcao. 
	 * @throws RemoteException 
	 */
	protected boolean rmdir( String server, String user, String dir) throws RemoteException {
		if(server == null) {
			if(LocalFileSystem.remove(dir) == 1)
				return true;
		}else {
			try {
				if(cs.hasAccess(server, user)) {
					try {
						getServer(server);
						return fserver.rmdir(dir);
					} catch (RemoteException | NotBoundException e) { 
						System.err.println( "Erro: " + e.getMessage());
						System.err.println("\nNao foi possivel se conectar ao servidor de ficheiros.");
					}
				}
			} catch (RemoteException e) { 
				System.err.println( "Erro: " + e.getMessage());
				System.err.println("\nNao foi possivel se conectar ao servidor.");
			}
		}
		System.err.println( "exec: rmdir " + dir + " no servidor " + server + "@" + user);
		return false;
	}

	/**
	 * Remove o ficheiro path no servidor server@user.
	 * (ou no sistema de ficheiros do cliente caso server == null).
	 * Devolve false em caso de erro.
	 * NOTA: n‹o deve lan�ar excepcao. 
	 * @throws RemoteException 
	 */
	protected boolean rm( String server, String user, String path) throws RemoteException {

		if(server == null) {
			if(LocalFileSystem.remove(path) == 2)
				return true;
		}else {
			try {
				if(cs.hasAccess(server, user)) {
					try {
						getServer(server);
						return fserver.rm(path);
					} catch (RemoteException | NotBoundException e) { 
						System.err.println( "Erro: " + e.getMessage());
						System.err.println("\nNao foi possivel se conectar ao servidor de ficheiros.");
					}
				}
			} catch (RemoteException e) { 
				System.err.println( "Erro: " + e.getMessage());
				System.err.println("\nNao foi possivel se conectar ao servidor.");
			}
		}
		System.err.println( "exec: rm " + path + " no servidor " + server + "@" + user);
		return false;
	}

	/**
	 * Devolve informacao sobre o ficheiro/directoria path no servidor server@user.
	 * (ou no sistema de ficheiros do cliente caso server == null).
	 * Devolve false em caso de erro.
	 * NOTA: n‹o deve lan�ar excepcao. 
	 */
	protected FileInfo getAttr( String server, String user, String path) {
		FileInfo info = null;
		if(server == null) {
			try {
				info = LocalFileSystem.getFileInfo(path);
			} catch (RemoteException e) {
				System.err.println( "Erro: " + e.getMessage());
			} catch (InfoNotFoundException e) {
				System.err.println( "Erro: " + e.getMessage());
				System.err.println( "exec: getattr " + path +  " no servidor " + server + "@" + user);
			}
		} 
		else {
			try {
				if(cs.hasAccess(server, user)) {
					try {
						getServer(server);
						info = fserver.getFileInfo(path);
					} catch (RemoteException | NotBoundException e) { 
						System.err.println( "Erro: " + e.getMessage());
						System.err.println("\nNao foi possivel se conectar ao servidor de ficheiros.");
					} catch( Exception e) {
						System.err.println( "Erro: " + e.getMessage());
						System.err.println( "exec: getattr " + path +  " no servidor " + server + "@" + user);
					}
				}
			} catch (RemoteException e) { 
				System.err.println( "Erro: " + e.getMessage());
				System.err.println("\nNao foi possivel se conectar ao servidor.");
			}
		}
		return info;
	}

	/**
	 * Copia ficheiro de fromPath no servidor fromServer@fromUser para o ficheiro 
	 * toPath no servidor toServer@toUser.
	 * (caso fromServer/toServer == local, corresponde ao sistema de ficheiros do cliente).
	 * Devolve false em caso de erro.
	 * NOTA: n‹o deve lan�ar excepcao. 
	 * @throws RemoteException 
	 * @throws NotBoundException 
	 */
	protected boolean cp( String fromServer, String fromUser, String fromPath,
			String toServer, String toUser, String toPath) throws RemoteException, NotBoundException {

		byte[] f = null; 
		String dest = toPath;//+"/"+(new File(fromPath).getName());
		if(fromServer == null && toServer == null) {
			//copia de fromPath para toPath no sistema de fich do user
			try {
				f = LocalFileSystem.getFile(fromPath);
				LocalFileSystem.sendFile(f, dest);
				return true;
			} catch (InfoNotFoundException e) {
				System.err.println( "Erro: " + e.getMessage());
			} catch (IOException e) {
				System.err.println( "Erro: " + e.getMessage());
			}

		}
		else if(fromServer == null && cs.hasAccess(toServer, username)) {
			//copia de fromPath para toServer@toServer
			try {
				f = LocalFileSystem.getFile(fromPath);
				getServer(toServer);
				System.out.println("name: "+dest);
				fserver.sendFile(f, dest);
				return true;
			} catch (InfoNotFoundException | IOException e) {
				System.err.println( "Erro: " + e.getMessage());
			} catch (NotBoundException e) { 
				System.err.println( "Erro: " + e.getMessage());
				System.err.println("\nNao foi possivel se conectar ao servidor de ficheiros.");
			}

		}
		else if(toServer == null && cs.hasAccess(fromServer, username)) {
			//copia de fromServer@fromUser:fromPath para toPath
			try {
				getServer(fromServer);
				f = fserver.getFile(fromPath);
				LocalFileSystem.sendFile(f, dest);
				return true;
			} catch (IOException | InfoNotFoundException e1) {
				System.err.println( "Erro: " + e1.getMessage());
			} catch (NotBoundException e) { 
				System.err.println( "Erro: " + e.getMessage());
				System.err.println("\nNao foi possivel se conectar ao servidor de ficheiros.");
			}
		}
		else if(cs.hasAccess(fromServer, fromUser) && cs.hasAccess(toServer, fromUser)) {
			//copia de fromServer@fromUser:fromPath para toServer@toUser
			try {
				getServer(fromServer);
				f = fserver.getFile(fromPath); 

				getServer(toServer);
				fserver.sendFile(f, dest);
			} catch( Exception e) {
				System.err.println( "Erro: " + e.getMessage());
			}
			return true; 
		}
		else {
			System.err.println( "exec: cp " + fromPath + " no servidor " + fromServer +"@" + fromUser + " para " +
					toPath + " no servidor " + toServer +"@" + toUser);
		}
		return false;
	}

	protected boolean sync(String pathFrom, String server, String user, String pathTo) throws RemoteException, InfoNotFoundException {
		if(cs.hasAccess(server, user)) {
			Synchronizer sync = new Synchronizer();
			return sync.areInSync(pathFrom, server, pathTo);
		}
		return false;
	}

	protected void doit() throws IOException, NotBoundException, InfoNotFoundException {
		BufferedReader reader = new BufferedReader( new InputStreamReader( System.in));
		for( ; ; ) {
			String line = reader.readLine();
			if( line == null)
				break;
			String[] cmd = line.split(" ");
			if( cmd[0].equalsIgnoreCase("servers")) {
				String[] s = servers();
				if( s == null)
					System.out.println( "error");
				else {
					System.out.println( s.length);
					for( int i = 0; i < s.length; i++)
						System.out.println( s[i]);
				}
			} else if( cmd[0].equalsIgnoreCase("addPermission")) {
				if(cmd.length == 3){
					String server = cmd[1];
					String user = cmd[2];
					boolean b = addPermission( server, user);

					if(b)
						System.out.println( "success");
					else
						System.out.println( "error");
				} else {
					System.out.println( "Please insert: addPermission server user");
				}
			} else if( cmd[0].equalsIgnoreCase("remPermission")) {
				if(cmd.length == 3){
					String server = cmd[1];
					String user = cmd[2];

					boolean b = remPermission( server, user);

					if( b)
						System.out.println( "success");
					else
						System.out.println( "error");
				}else {
					System.out.println( "Please insert: remPermission server user");
				}
			} else if( cmd[0].equalsIgnoreCase("ls")) {
				if(cmd.length == 2) {
					String[] dirserver = cmd[1].split(":");
					String[] serveruser = dirserver[0].split("@");

					String server = dirserver.length == 1 ? null : serveruser[0];
					String user = dirserver.length == 1 || serveruser.length == 1 ? null : serveruser[1];
					String dir = dirserver.length == 1 ? dirserver[0] : dirserver[1];

					List<String> res = dir( server, user, dir);

					if( res != null) {
						System.out.println( res.size());
						for( String s : res)
							System.out.println(s);
					} else
						System.out.println( "error");
				} else {
					System.out.println("Please insert: ls path");
				}
			} else if( cmd[0].equalsIgnoreCase("mkdir")) {
				if(cmd.length == 2) {
					String[] dirserver = cmd[1].split(":");
					String[] serveruser = dirserver[0].split("@");

					String server = dirserver.length == 1 ? null : serveruser[0];
					String user = dirserver.length == 1 || serveruser.length == 1 ? null : serveruser[1];
					String dir = dirserver.length == 1 ? dirserver[0] : dirserver[1];

					boolean b = mkdir( server, user, dir);
					if( b)
						System.out.println( "success");
					else
						System.out.println( "error");
				} else {
					System.out.println( "Please insert: mkdir server@user:path");
				}
			} else if( cmd[0].equalsIgnoreCase("rmdir")) {
				if(cmd.length == 2) {
					String[] dirserver = cmd[1].split(":");
					String[] serveruser = dirserver[0].split("@");

					String server = dirserver.length == 1 ? null : serveruser[0];
					String user = dirserver.length == 1 || serveruser.length == 1 ? null : serveruser[1];
					String dir = dirserver.length == 1 ? dirserver[0] : dirserver[1];

					boolean b = rmdir( server, user, dir);
					if( b)
						System.out.println( "success");
					else
						System.out.println( "error");
				}	else {
					System.out.println( "Please insert: rmdir server@user:path");
				}
			} else if( cmd[0].equalsIgnoreCase("rm")) {
				if(cmd.length == 2){
					String[] dirserver = cmd[1].split(":");
					String[] serveruser = dirserver[0].split("@");

					String server = dirserver.length == 1 ? null : serveruser[0];
					String user = dirserver.length == 1 || serveruser.length == 1 ? null : serveruser[1];
					String path = dirserver.length == 1 ? dirserver[0] : dirserver[1];

					boolean b = rm( server, user, path);
					if( b)
						System.out.println( "success");
					else
						System.out.println( "error");
				}else {
					System.out.println( "Please insert: rm path");
				}
			} else if( cmd[0].equalsIgnoreCase("getattr")) {
				if(cmd.length == 2) {
					String[] dirserver = cmd[1].split(":");
					String[] serveruser = dirserver[0].split("@");

					String server = dirserver.length == 1 ? null : serveruser[0];
					String user = dirserver.length == 1 || serveruser.length == 1 ? null : serveruser[1];
					String path = dirserver.length == 1 ? dirserver[0] : dirserver[1];

					FileInfo info = getAttr( server, user, path);
					if( info != null) {
						System.out.println( info);
						System.out.println( "success");
					} else
						System.out.println( "error"); 
				} else {
					System.out.println("Please insert: getattr path"); 
				}
			} else if( cmd[0].equalsIgnoreCase("cp")) {
				if(cmd.length == 3){
					String[] dirserver1 = cmd[1].split(":");
					String[] serveruser1 = dirserver1[0].split("@");

					String fromServer = dirserver1.length == 1 ? null : serveruser1[0];
					String fromUser = dirserver1.length == 1 || serveruser1.length == 1 ? null : serveruser1[1];
					String fromPath = dirserver1.length == 1 ? dirserver1[0] : dirserver1[1];

					String[] dirserver2 = cmd[2].split(":");
					String[] serveruser2 = dirserver2[0].split("@");

					String toServer = dirserver2.length == 1 ? null : serveruser2[0];
					String toUser = dirserver2.length == 1 || serveruser2.length == 1 ? null : serveruser2[1];
					String toPath = dirserver2.length == 1 ? dirserver2[0] : dirserver2[1];

					boolean b = cp( fromServer, fromUser, fromPath, toServer, toUser, toPath);
					if( b)
						System.out.println( "success");
					else
						System.out.println( "error");
				}
				else {
					System.out.println( "Please insert: cp path path");
				}
			} else if(cmd[0].equalsIgnoreCase("sync")) {
				if(cmd.length == 3){
					String[] dirserver = cmd[2].split(":");
					String[] serveruser = dirserver[0].split("@");

					String local = cmd[1];
					String server = dirserver.length == 1 ? null : serveruser[0];
					String user = dirserver.length == 1 || serveruser.length == 1 ? null : serveruser[1];
					String dir = dirserver.length == 1 ? dirserver[0] : dirserver[1];

					boolean b = sync(local, server, user, dir);
					if( b)
						System.out.println( "success");
					else
						System.out.println( "error");
				}
				else {
					System.out.println( "Please insert: sync dir server@user:dir");
				}
			} else if( cmd[0].equalsIgnoreCase("help")) {
				System.out.println("servers - lista URLs dos servidores a que tem acesso");
				System.out.println("addPermission server user - adiciona user a lista de utilizadores com permissoes para aceder a server");
				System.out.println("remPermission server user - remove user da lista de utilizadores com permissoes para aceder a server");
				System.out.println("ls server@user:dir - lista ficheiros/directorias presentes na directoria dir (. e .. tem o significado habitual), caso existam ficheiros com o mesmo nome devem ser apresentados como nome@server;");
				System.out.println("mkdir server@user:dir - cria a directoria dir no servidor server@user");
				System.out.println("rmdir server@user:dir - remove a directoria dir no servidor server@user");
				System.out.println("cp path1 path2 - copia o ficheiro path1 para path2; quando path representa um ficheiro num servidor deve ter a forma server@user:path, quando representa um ficheiro local deve ter a forma path");
				System.out.println("rm path - remove o ficheiro path");
				System.out.println("getattr path - apresenta informa�‹o sobre o ficheiro/directoria path, incluindo: nome, boolean indicando se Ž ficheiro, data da cria�‹o, data da œltima modificacao");
			} else if( cmd[0].equalsIgnoreCase("exit"))
				break;
		}
	}

	public static void main( String[] args) throws NotBoundException, MalformedURLException, RemoteException, InfoNotFoundException {
		if( args.length != 2) {
			System.out.println("Use: java trab1.FileClient URL nome_utilizador");
			return;
		}
		try {
			new FileClient( args[0], args[1]).doit();
		} catch (IOException e) {
			System.err.println("Error:" + e.getMessage());
			e.printStackTrace();
		}
	}

	private void getServer(String server) throws RemoteException, NotBoundException {
		try {
			String serverIP = cs.getServerURL(server);
			fserver = (IFileServer) Naming.lookup("//"+serverIP+"/"+server);
		} catch (RemoteException e) { 
			System.err.println("Error: " + e.getMessage());
			System.err
			.println("\nNao foi possivel se conectar ao servidor de ficheiros.");
		} catch (NotBoundException e) {
			System.err.println("Error: " + e.getMessage());
			System.err
			.println("\nNao foi possivel obter uma referencia remota do servidor de ficheiros.");
		} catch (MalformedURLException e) {
			System.err.println("Error: " + e.getMessage());;
		}
	}

	private class Synchronizer {

		public Synchronizer() {

		}

		public boolean areInSync(String pathFrom, String server, String pathTo) {

			try {
				List<String> localLs = dir(null, username, pathFrom);
				List<String> serverLs = dir(server, username, pathTo);
				for (int i = 0; i < localLs.size(); i++) {
					String fileLocal = localLs.get(i); 
					FileInfo fileInfoL = getAttr(null, username, pathFrom + "/" + fileLocal);
					boolean exists = false;
					for(int j = 0; j < serverLs.size(); j++) {
						String fileServer = serverLs.get(j);
						String[] vec = path(fileServer);
						FileInfo fileInfoS = getAttr(server, username, pathTo + "/" +vec[vec.length-1]);
						if(fileLocal.equals(vec[vec.length-1])){
							if(		//	file_aux.modified == file_aux2.modified &&
									fileLocal.equals(fileInfoS.name) &&
									fileInfoL.isFile == fileInfoS.isFile) {
								if(fileInfoL.modified != fileInfoS.modified && 
										fileInfoL.length != fileInfoS.length)
								{
									cp(server, username, pathTo+"/"+fileInfoS.name, null, username,  pathFrom+"/"+"local_"+fileInfoS.name);
								}
									exists = true;
									break; 
							}
						}
					}

					if(!exists) {
						if(!fileInfoL.isFile){
							mkdir(server, username, pathTo+"/"+fileInfoL.name);
						}
						else {
							cp(null, username, pathFrom+"/"+fileInfoL.name, server, username, pathTo+"/"+fileInfoL.name );
						}
					}

				}
			} catch (RemoteException | NotBoundException
					| InfoNotFoundException e) {
			}
			return true;
		}

		private String[] path(String path) 
		{ 
			String[] pathSplitter = path.replace("\\","/").split("/"); 
			return pathSplitter;     
		} 
	}
}