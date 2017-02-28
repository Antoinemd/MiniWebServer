package serveur;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;


public class ServerMultiThread {
	
	// Déclare un socket client/server pour le le serveur
    // Déclare un nombre de connections

    ServerSocket getRequestServer = null;
    Socket clientSocket = null;
    int numConnections = 0;
    int port;
	public Path pathProject = null;

    public Path getterPathProject() {
    	return this.pathProject;
	}
	
    public ServerMultiThread( int port, Path pathProject ) {
    	this.port = port;
    	this.pathProject = pathProject;
    }
    
    public void stopServer() {
	System.out.println( "Le serveur va s'arrêter." );
	System.exit(0);
    }
    
    public void startServer() {
    	// Essaye d'ouvrir un serveur de socket sur le port donné
    	// les ports doivent être > 1024 ou bien avoir les privileges root
    	try {
            getRequestServer = new ServerSocket(port);
        } catch (IOException e) {
        	System.out.println(e);
        }   
    	
    	System.out.println( "Le serveur démmaré, en attente de connections.." );
    	System.out.println( "Multi-threading et connection simultannées autorisés." );
    	//System.out.println( "Un client peut envoyer -1 pour stopper le serveur." );
    	System.out.println();
    	
    	// A chaque fois qu'une connection est reçue, on démarre un nouveau thread pour procéder à la connection
    	// et attendre la prochaine connections
    	
    	while ( true ) {
    		try {
    			clientSocket = getRequestServer.accept();	// accepter la connection du client
    			numConnections ++;							// incrémentation du nombre de conections au serveur
    			ServerThreadManager oneConnexion = new ServerThreadManager(clientSocket, numConnections, this);
    			new Thread(oneConnexion).start();
    		} catch (IOException e) {
    	    	System.out.println(e);
    	    }
    	}
    }
    

    public static void main(String args[]) {
    	// TODO path à modifier selon utilisateur
		Path pathProject = Paths.get("/home/antoine/workspaces/workspace2/MiniWebServer/src/serveur/");
		int port = 8080;														// Port d'écoute
		ServerMultiThread server = new ServerMultiThread( port, pathProject );	// Instanciation de l'objet ServerMultiThread
		server.startServer();													// démarrage du serveur
    }
}
