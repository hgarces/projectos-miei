package fileserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.*;
import java.util.List;

import util.FileInfo;
import util.InfoNotFoundException;

public interface IFileServer extends Remote
{
	/**
	 * Lista o counteudo de um directorio
	 */
	public List<String> dir( String path) throws RemoteException, InfoNotFoundException;

	/**
	 * Devolve os atributos de um ficheiro/directoria
	 */
	public FileInfo getFileInfo( String path) throws RemoteException, InfoNotFoundException;

	public byte[] getFile(String name) throws InfoNotFoundException, IOException;

	public boolean sendFile(byte[] b, String name) throws FileNotFoundException, IOException, InfoNotFoundException; 

	/**
	 * Cria uma directoria 

	 */
	public boolean mkdir(String dir) throws RemoteException;

	/**
	 * Remove uma directoria
	 */
	public boolean rmdir(String dir) throws RemoteException;

	/**
	 * Remove o ficheiro path
	 */
	public boolean rm(String path) throws RemoteException;
}