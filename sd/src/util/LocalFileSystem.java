package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sistemas Distribuidos
 * 
 * @author Rui Queiros - 41717
 * @author Henrique Garces - 42077
 */
public class LocalFileSystem {

	final static File basePath = new File(".");

	public LocalFileSystem() {
	}

	public static List<String> dir(String path) throws InfoNotFoundException {
		List<String> result = new ArrayList<String>();
 		File[] f = new File( basePath, path).listFiles();
 		if(f == null)
 			throw new InfoNotFoundException( "Directory not found: " + path);
		for( File file : f)
			result.add(file.getName());
		return result;
	}

	public static boolean mkdir(String dir) throws RemoteException {
		File file = new File(dir);
		if(!file.mkdir()) {
			return false;
		}
		return true;
	}

	public static int remove(String path) throws RemoteException {
		File file = new File(path);
		if(file.isDirectory()) {
			if(file.delete())
				return 1;
		}
		else if(file.isFile()) {
			if(file.delete())
				return 2;
		}
		return 0;
	}

	public static byte[] getFile(String name)
			throws InfoNotFoundException, IOException {
		File file = new File(name);
		if(file.isFile())  {
			RandomAccessFile f = new RandomAccessFile(name, "r");
			long size = f.length();
			byte[] b = new byte[(int) size];
			f.readFully(b);
			f.close();
			return b; 
		}
		return null;
	}

	public static void sendFile(byte[] b, String name) throws IOException, InfoNotFoundException {
		if(b != null) {
			FileOutputStream f = new FileOutputStream(name); 
			f.write(b);
			f.close();
		}
		else
			throw new InfoNotFoundException("File not found!");
	}

	public static FileInfo getFileInfo(String path)
			throws RemoteException, InfoNotFoundException {
		File dir = new File(path);
		if (dir.exists()) {
			File f = new File(dir.getName());
			if (dir.isDirectory())
				return new FileInfo(f.getName(), folderSize(dir),
						new Date(dir.lastModified()), dir.isFile());
			else
				return new FileInfo(f.getName(), dir.length(),
						new Date(dir.lastModified()), dir.isFile());
		} else
			throw new InfoNotFoundException("Directory/File not found: " + dir);
	}
	
	private static long folderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile())
	            length += file.length();
	        else
	            length += folderSize(file);
	    }
	    return length;
	}
}