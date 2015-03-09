package rest; 

import java.io.ByteArrayOutputStream; 
import java.io.FileInputStream;
import java.io.FileNotFoundException; 
import java.io.FileOutputStream;
import java.io.IOException; 
import java.io.InputStream; 
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress; 
import java.net.MalformedURLException; 
import java.net.UnknownHostException; 
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
import java.util.Scanner; 








import org.json.simple.JSONArray; 
import org.json.simple.JSONObject; 
import org.json.simple.parser.JSONParser; 
import org.json.simple.parser.ParseException; 
import org.scribe.builder.ServiceBuilder; 
import org.scribe.model.OAuthRequest; 
import org.scribe.model.Response; 
import org.scribe.model.Token; 
import org.scribe.model.Verb; 
import org.scribe.model.Verifier; 
import org.scribe.oauth.OAuthService; 








import contactserver.IContactServer; 
import util.FileInfo; 
import util.InfoNotFoundException; 
import fileserver.IFileServer; 

public class GoogleDriveProxy extends UnicastRemoteObject implements IFileServer { 

	private static final String API_KEY = "189140002069-t8ribt26vhr7g527f06l1mr25lkuguav.apps.googleusercontent.com"; 
	private static final String API_SECRET = "XjI98MD9dQ0tF3maQ9Yup0Fs"; 
	private static final String SCOPE = "https://www.googleapis.com/auth/drive"; 
	private static final String FOLDER = "application/vnd.google-apps.folder"; 
	private static final Token EMPTY_TOKEN = null; 
	private static final String TOKEN = "../tokens/drive_";
	private static Token accessToken; 
	private static OAuthService service;  

	private static final long serialVersionUID = 1L; 

	protected GoogleDriveProxy() throws RemoteException { 
		super(); 
		try {
			service = new ServiceBuilder().provider(Google2Api.class).apiKey(API_KEY) 
					.apiSecret(API_SECRET).scope(SCOPE).build(); 
			String authorizationUrl = service.getAuthorizationUrl(EMPTY_TOKEN); 
			Scanner in = new Scanner(System.in); 

			accessToken = loadToken();
			if(accessToken == null) {
				System.out.println("Tem de obter autorizacao para a aplicacao continuar acedendo ao link:"); 
				System.out.println(authorizationUrl); 
				System.out.println("Copiar e colar o codigo forneciso na pagina"); 
				System.out.print(">>"); 
				Verifier verifier = new Verifier(in.nextLine()); 
				accessToken = service.getAccessToken(EMPTY_TOKEN, verifier); 
				storeToken(accessToken);
			}
			in.close(); 
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	} 

	@Override
	public List<String> dir(String path) throws RemoteException, 
	InfoNotFoundException { 
		OAuthRequest request;
		if(path.equals(".") || path.equals("/"))
			request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/drive/v2/files/root/children");    
		else
			request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/drive/v2/files/"+getID(path)+"/children"); 
		request.addQuerystringParameter("q", "trashed=false");
		service.signRequest(accessToken, request); 
		Response response = request.send(); 

		if (response.getCode() != 200) {
			return null; 
		}
		List<String> ls = new ArrayList<String>(); 
		JSONParser parser = new JSONParser(); 
		JSONObject res = null; 
		try { 
			res = (JSONObject) parser.parse(response.getBody()); 
		} catch (ParseException e) { 

		} 
		JSONArray items = (JSONArray) res.get("items"); 

		@SuppressWarnings("rawtypes") 
		Iterator it = items.iterator();
		while (it.hasNext()) {
			JSONObject file = (JSONObject) it.next();
			ls.add(getTitle((String) file.get("id")));
		} 
		return ls; 
	} 

	@Override
	public FileInfo getFileInfo(String path) throws RemoteException, 
	InfoNotFoundException { 
		String[] aux = path(path);
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/drive/v2/files/"+getID(aux[aux.length-1]));
		service.signRequest(accessToken, request);
		Response response = request.send();

		if(response.getCode() != 200)
			return null;

		JSONParser parser = new JSONParser();
		JSONObject res = null;
		try {
			res = (JSONObject) parser.parse(response.getBody());
		} catch (ParseException e1) {

		}

		String name = (String) res.get("title");
		long length = (Long.parseLong(res.get("quotaBytesUsed").toString()));
		String date = (String) res.get("modifiedDate");
		GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");
		try {
			cal.setTime(sdf.parse(date));
		} catch (java.text.ParseException e) {

		}
		Date modified = cal.getTime();
		boolean isFolder = !res.get("mimeType").toString().equals(FOLDER);

		return new FileInfo(name, length, modified, isFolder);
	} 

	@Override
	public byte[] getFile(String name) throws InfoNotFoundException, 
	IOException { 
		String[] aux = path(name);
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/drive/v2/files/"+getID(aux[aux.length-1])); 
		service.signRequest(accessToken, request); 
		Response response = request.send(); 

		JSONParser parser = new JSONParser();
		JSONObject res = null;
		try {
			res = (JSONObject) parser.parse(response.getBody());
		} catch (ParseException e1) {

		}
		String downloadUrl = (String) res.get("downloadUrl");
		OAuthRequest newRequest = new OAuthRequest(Verb.GET, downloadUrl);
		service.signRequest(accessToken, newRequest);
		Response reply = newRequest.send();

		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		InputStream is = reply.getStream(); 
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
	@SuppressWarnings("unchecked")
	public boolean sendFile(byte[] b, String name) 
			throws FileNotFoundException, IOException, InfoNotFoundException { 
		String[] aux = path(name);
		OAuthRequest request = new OAuthRequest(Verb.POST, 
				"https://www.googleapis.com/upload/drive/v2/files?uploadType=media");
		request.addHeader("Content-Length", b.length + "");
		request.addHeader("Content-Type", "");
		request.addPayload(b); 
		service.signRequest(accessToken, request);
		Response response = request.send();

		JSONParser parser = new JSONParser();
		JSONObject res = null;
		try {
			res = (JSONObject) parser.parse(response.getBody());
		} catch (ParseException e) {

		}

		String id = res.get("id").toString();
		OAuthRequest newRequest = new OAuthRequest(Verb.PUT, "https://www.googleapis.com/drive/v2/files/"+id);
		newRequest.addHeader("Content-Type", "application/json");
		JSONObject jo = new JSONObject();
		jo.put("title", aux[aux.length-1]);
		if(aux.length > 1) { 
			JSONObject parent = new JSONObject(); 
			JSONArray parents = new JSONArray(); 
			parent.put("id", getID(aux[aux.length-2])); 
			parents.add(parent); 
			jo.put("parents", parents);
		}
		jo.put("originalFilename", aux[aux.length-1]);
		jo.put("fileExtension", "");

		newRequest.addPayload(jo.toJSONString());
		service.signRequest(accessToken, newRequest);
		response = newRequest.send();

		return response.getCode() == 200;
	} 

	@Override
	@SuppressWarnings("unchecked") 
	public boolean mkdir(String dir) throws RemoteException { 
		String[] aux = path(dir); 
		String title = aux[aux.length-1]; 

		OAuthRequest request = new OAuthRequest(Verb.POST, "https://www.googleapis.com/drive/v2/files"); 
		request.addHeader("Content-Type", "application/json");
		JSONObject res = new JSONObject();
		res.put("title", title); 
		res.put("mimeType", "application/vnd.google-apps.folder"); 

		if(aux.length > 1)  { 
			JSONObject parent = new JSONObject(); 
			JSONArray parents = new JSONArray(); 
			parent.put("id", getID(aux[aux.length-2])); 
			parents.add(parent); 
			res.put("parents", parents);
		}

		request.addPayload(res.toJSONString()); 
		service.signRequest(accessToken, request); 
		Response response = request.send(); 

		if(response.getCode() != 200) { 
			System.out.println("Could not create directory "); 
			return false; 
		} 
		return true; 
	} 

	@Override
	public boolean rmdir(String dir) throws RemoteException {
		return rm(dir);  
	} 

	@Override
	public boolean rm(String path) throws RemoteException { 
		String[] aux = this.path(path); 
		OAuthRequest request = new OAuthRequest(Verb.DELETE, "https://www.googleapis.com/drive/v2/files/"+getID(aux[aux.length-1])); 
		service.signRequest(accessToken, request); 
		Response response = request.send(); 
		if(!response.getBody().isEmpty()) { 
			System.out.println("Could not delete file "); 
			return false; 
		} 
		return true; 
	} 

	private String[] path(String path) { 
		String[] pathSplitter = path.replace("\\","/").split("/"); 
		return pathSplitter;     
	} 

	private String getID(String path) { 
		String[] pathSplitter = path(path);  
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/drive/v2/files"); 
		service.signRequest(accessToken, request); 
		Response response = request.send(); 

		if (response.getCode() != 200) 
			throw new RuntimeException("Error: "+ response.getCode()); 

		JSONParser parser = new JSONParser();
		JSONObject res = null;
		try { 
			res = (JSONObject) parser.parse(response.getBody()); 
		} catch (Exception e) { 

		} 
		JSONArray items = (JSONArray) res.get("items"); 

		@SuppressWarnings("rawtypes")
		Iterator it = items.iterator(); 
		while (it.hasNext()) { 
			JSONObject file = (JSONObject) it.next(); 
			if(file.get("title").toString().equals(pathSplitter[pathSplitter.length-1])) 
				return (String) file.get("id"); 
		} 

		return null; 
	} 

	private String getTitle(String id) {
		OAuthRequest request = new OAuthRequest(Verb.GET, "https://www.googleapis.com/drive/v2/files");
		service.signRequest(accessToken, request);
		Response response = request.send();

		if (response.getCode() != 200)
			throw new RuntimeException("Error: " + response.getCode());

		JSONParser parser = new JSONParser();
		JSONObject res = null;
		try { 
			res = (JSONObject) parser.parse(response.getBody()); 
		} catch (Exception e) { 

		} 
		JSONArray items = (JSONArray) res.get("items"); 

		@SuppressWarnings("rawtypes")
		Iterator it = items.iterator();
		while (it.hasNext()) {
			JSONObject file = (JSONObject) it.next();
			if(file.get("id").equals(id))
				return (String) file.get("title");
		}
		return null;
	}


	public static void main(String[] args) { 
		try { 
			if( args.length != 3) { 
				System.out.println( "Use: java GoogleDriveProxy server_name contactServer_url owner"); 
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
			try { 
				myIP = InetAddress.getLocalHost().getHostAddress(); 
			} catch (UnknownHostException e) { 
				System.err.println("Error: " + e.getMessage()); 
			} 
			Registry reg = LocateRegistry.getRegistry(contactServer, 1099); 
			IContactServer cserver = (IContactServer) Naming.lookup("//"+contactServer+"/contactServer"); 
			cserver.registerServer(serverName, myIP, owner); 
			System.out.println("GoogleDriveProxy bound in registry"); 
			reg.rebind(serverName, new GoogleDriveProxy()); 

		} catch (RemoteException e) {  
			System.err.println("Error: " + e.getMessage()); 
			System.err 
			.println("\nNao foi possivel se conectar ao servidor."); 
		} catch (NotBoundException e) { 
			System.err.println("Error: " + e.getMessage()); 
			System.err 
			.println("\nNao foi possivel obter uma referencia remota do servidor."); 
		} catch (MalformedURLException e) { 
			System.err.println("Error: " + e.getMessage()); 
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