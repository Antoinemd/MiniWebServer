package serveur;

import java.io.*;
import java.net.*;

class ServerThreadManager implements Runnable {
	
    BufferedReader br;
    PrintWriter pw;
//    PrintStream os;
    Socket clientSocket;
    int id;
    ServerMultiThread server;

    public ServerThreadManager(Socket clientSocket, int id, ServerMultiThread server) {
    	
		this.clientSocket = clientSocket;	// socket
		this.id = id;						// id de la connection
		this.server = server;				// server
		
		System.out.println( "Connexion " + id + " établie avec: " + clientSocket );
		
		try {
			// instanciation des flux I/O
		    br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			pw = new PrintWriter(clientSocket.getOutputStream(), true);
			// os = new PrintStream(clientSocket.getOutputStream());
		} catch (IOException e) {
		    System.out.println(e);
		}
    }

    
    public void run() {
    	
    	AfficheRequetesHttp Thread = new AfficheRequetesHttp();
    	String line = null;
    	
		try {
		    boolean serverStop = false;
	
		    while (true) {
		    	
		    	System.out.println("----DEBUT REQUETE----");
				line = br.readLine();						// On arrête à la fin des entête qui est définie comme une ligne vide
				
		    	System.out.println( "Received " + line + " from Connection " + id + "." );
		    	
		    	while(!"".equals(line) && line != null){	// != null pour éviter les plantages
					System.out.println(line); 				// première ligne du header
					String typeRequest = Thread.typeDeRequete(line);
					Thread.traitementDesRequetes(line, typeRequest, pw, clientSocket);
					line = br.readLine();					// ligne suivante du header		
				}
				
				System.out.println("----FIN REQUETE----");
		    	
//	            int n = Integer.parseInt(line);
	            
//	            if ( n == -1 ) {
//	            	serverStop = true;
//	            	break;
//	           	}
//	            if ( n == 0 ){
//	            	break;
//	            }
//	        os.println("" + n*n ); 
		    
	
			    System.out.println( "Connection " + id + " fermé." );
			    br.close();
			    pw.close();
			    // os.close();
		        clientSocket.close();
				System.out.println();

			    if ( serverStop ){ 
			    	server.stopServer();
			    }
		    }
		    
		} catch (IOException e) {
		    System.out.println(e);
		}
    }
}