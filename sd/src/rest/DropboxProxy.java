package rest;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.DropBoxApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import contactserver.IContactServer;
import fileserver.IFileServer;
import util.FileInfo;
import util.InfoNotFoundException;

public class DropboxProxy extends UnicastRemoteObject implements IFileServer{

	private static final long serialVersionUID = -2830334345721775866L;
	private static final String API_KEY = "jhqt45fi2r46waf";
	private static final String API_SECRET = "0m3dowjnb2ttfqp";
	private static final String SCOPE = "sandbox";
	private static final String AUTHORIZE_URL = "https://www.dropbox.com/1/oauth/authorize?oauth_token=";
	private static final String TOKEN = "../tokens/dropbox_";

	private static OAuthService service;
	private static Token accessToken;

	public DropboxProxy() throws RemoteException{
		super();
		try {
			service = new ServiceBuilder().provider(DropBoxApi.class).apiKey(API_KEY)
				.apiSecret(API_SECRET).scope(SCOPE).build();
			// Obter Request token
			Token requestToken = service.getRequestToken();
			Scanner in = new Scanner(System.in);
		
			accessToken = loadToken();
			if(accessToken == null) {
				System.out.println("Tem de obter autorizacao para a aplicacao continuar acedendo ao link:");
				System.out.println(AUTHORIZE_URL + requestToken.getToken());
				System.out.println("E carregar em enter quando der autorizacao");
				System.out.print(">>");
				Verifier verifier = new Verifier(in.nextLine());
				verifier = new Verifier(requestToken.getSecret());
				accessToken = service.getAccessToken(requestToken, verifier);
				storeToken(accessToken);
			}
			
			in.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<String> dir(String path) throws InfoNotFoundException {
		if (path.equals("/") || path.equals("."))
			path = "";
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.dropbox.com/1/metadata/"+SCOPE+"/"+path+"?list=true");
		service.signRequest(accessToken, request);
		Response response = request.send();

		if (response.getCode() != 200)
			return null;

		List<String> ls = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		JSONObject res = null;
		try {
			res = (JSONObject) parser.parse(response.getBody());
		} catch (ParseException e) {

		}

		JSONArray items = (JSONArray) res.get("contents");
		@SuppressWarnings("rawtypes")
		Iterator it = items.iterator();
		while (it.hasNext()) {
			JSONObject file = (JSONObject) it.next();
			ls.add(file.get("path").toString());
		}
		return ls;
	}

	@Override
	public FileInfo getFileInfo(String path) throws InfoNotFoundException {
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.dropbox.com/1/metadata");
		request.addQuerystringParameter("root", SCOPE);
		request.addQuerystringParameter("path", path);
		service.signRequest(accessToken, request);
		Response response = request.send();
		if(response.getCode() != 200)
			return null;

		JSONParser parser = new JSONParser(); 
		JSONObject res = null; 
		try { 
			res = (JSONObject) parser.parse(response.getBody()); 
		} catch (ParseException e) { 

		} 

		String date = ((String) res.get("modified"));
		GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance(); 
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US); 
		try { 
			cal.setTime(sdf.parse(date)); 
		} catch (java.text.ParseException e) { 
			
		} 

		Date modified = cal.getTime(); 
		String[] s = res.get("path").toString().split("/"); 
		String name = s[s.length - 1]; 
		long length = ((Long) res.get("bytes")); 
		boolean isFile = !((Boolean) res.get("is_dir")); 
		return new FileInfo(name, length, modified, isFile);
	}

	@Override
	public byte[] getFile(String name) throws InfoNotFoundException, IOException {
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://api-content.dropbox.com/1/files");
		
		request.addQuerystringParameter("root", SCOPE);
		request.addQuerystringParameter("path", name);
		service.signRequest(accessToken, request);
		Response response = request.send();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = response.getStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = is.read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		is.close();
		baos.flush();
		baos.close();
		return baos.toByteArray();
	}

	@Override
	public boolean sendFile(byte[] b, String name) throws IOException, util.InfoNotFoundException {
		System.out.println("name: "+name);
		OAuthRequest request = new OAuthRequest(Verb.PUT, "https://api-content.dropbox.com/1/files_put");
		request.addQuerystringParameter("root", SCOPE);
		request.addQuerystringParameter("path", name);
		request.addHeader("Content-Type", "");
		request.addPayload(b);
		service.signRequest(accessToken, request);
		Response response = request.send();
		return response.getCode() == 200;

	}

	@Override
	public boolean mkdir(String dir) {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.dropbox.com/1/fileops/create_folder");
		request.addQuerystringParameter("root", SCOPE);
		request.addQuerystringParameter("path", dir);
		service.signRequest(accessToken, request);
		Response response = request.send();
		if(response.getCode() != 200) {
			System.out.println("Could not create directory ");
			return false;
		}
		return true;
	}

	@Override
	public boolean rmdir(String dir) {
		return this.rm(dir);
	}

	@Override
	public boolean rm(String path) {
		OAuthRequest request = new OAuthRequest(Verb.POST, "https://api.dropbox.com/1/fileops/delete");
		request.addQuerystringParameter("root", SCOPE);
		request.addQuerystringParameter("path", path);
		service.signRequest(accessToken, request);
		Response response = request.send();
		if(response.getCode() != 200) {
			System.out.println("Could not delete file ");
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		try {
			if( args.length != 3) {
				System.out.println( "Use: java DropboxProxy server_name contactServer_url owner");
				System.exit(0);
			}
			
			String serverName = args[0];
			String contactServer = args[1];
			String owner = args[2];			
			System.getProperties().put("java.security.policy", "policy.all");
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
			}
		
			String myIP = "";
			myIP = InetAddress.getLocalHost().getHostAddress();

			Registry reg = LocateRegistry.getRegistry(contactServer, 1099);
			IContactServer cserver = (IContactServer) Naming.lookup("//"+contactServer+"/contactServer");
			cserver.registerServer(serverName, myIP, owner);
			System.out.println("DropboxProxy bound in registry");
			reg.rebind(serverName, new DropboxProxy());
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

	private static Token loadToken() throws IOException, ClassNotFoundException {
		Token token = null;
		try {
			ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(TOKEN));
			token = (Token) inStream.readObject();
			inStream.close();
		} catch(Exception e) {
		
		}

		return token;
	}

	private static void storeToken(Token token) throws IOException {
		try {
			ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(TOKEN));
			outStream.writeObject(token);
			outStream.flush();
			outStream.close();
		} catch(Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
}
