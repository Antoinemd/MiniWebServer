package serveur;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;


//import org.tpserver.HtmlPage;


public class AfficheRequetesHttp implements Runnable{
	
	private Thread t;
	private String threadName;
	
	public AfficheRequetesHttp(String name) {
		threadName = name;
		System.out.println("Cration du thread: " + threadName);
	}
	
//	@Override
	public void run() {
	      System.out.println("Lancement " +  threadName );
	      try {
	    	  for(int i = 4; i > 0; i--) {
	              System.out.println("Thread: " + threadName + ", " + i);
	              // Let the thread sleep for a while.
	              Thread.sleep(50);
	           }			
		} catch (InterruptedException e) {
	         System.out.println("Thread " +  threadName + " interrompue.");		
	    }
	      System.out.println("Thread " +  threadName + " en cours d'arret.");
	}

	public void start () {
		System.out.println("Lancement " +  threadName );
	    if (t == null) {
	    	t = new Thread (this, threadName);
	        t.start ();
	    }
	}
	
		
	/**
	 * Retourne l'heure pour headers HTTP
	 * 
	 * @return
	 */
	private String getServerTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.FRANCE);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(calendar.getTime());
	}
	
	private String typeDeRequete(String line){
		return line.substring(0, line.indexOf(" ")).toLowerCase(); 
	}
	
	private String pathToGet(String line, int lengthOfRequest){
		//TODO modifer avec un resolvePath()
		return line.substring(lengthOfRequest+1,line.length()-9);
	}
	
	private String envoyerHeaderHTML (String mimeType, String codeRequest) {
		//		w.println("Content-Type: text/html; charset=UTF-8");
		return ("HTTP/1.1 " + codeRequest + " <br/>" + "Date: " + getServerTime() + 
						"Content-Type: " + mimeType + "; charset=UTF-8" + "Expires: -1" + "\n");
	}
	
	private String listingFilesOfDirectory(File f){
		// Listing du contenu du répertoire
		File[] listOfFiles = f.listFiles();
		StringBuilder contenuRep = new StringBuilder();
		contenuRep.append("<a href ='" + f.getParent() + "'>" + " <- Dossier Parent </a> <br/> <br/>");
		for (File file : listOfFiles) {
			if (file.isFile()) {
				contenuRep.append(("<a href ='" + file.getAbsolutePath() + "'>" + file.getName() + "</a><br/>"));
			}
			if (file.isDirectory()) {
				contenuRep.append(("<a href ='" + file.getAbsolutePath() + "'>" + file.getName() + "</a><br/>"));
			}
		}// end of: forEach		
		return contenuRep.toString();
	}
	
	private String pathUtf8 ( Path p ) {
		String decodedUrl = null;
		try {
			decodedUrl = URLDecoder.decode(p.toString(), "UTF-8"); //use this instead
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decodedUrl;
	}
	
	private void traitementFichiers ( File f, Path ressPath,
										PrintWriter pw, Socket socket ) throws IOException {
    	if(f.exists()) {			    		
    		if (f.isDirectory()) {
    			String mimeType = Files.probeContentType(ressPath);
	    		pw.println(envoyerHeaderHTML(mimeType, "200 OK"));
				pw.println(listingFilesOfDirectory(f));
			} else {														// sinon c'est un fichier
				if (Files.isRegularFile(ressPath)) {
					// marche que sur Chrome ...
					Files.copy(ressPath, socket.getOutputStream());
				} else { System.out.println("File isn't regular."); }
			}																// enf of: if(isDirectory / isFile)
    	} else { pw.println(envoyerHeaderHTML("text/html", "404 NOT FOUND")); }// erreur 404
	}
	
	
	private void traitementRequeteGet(String line, String typeDeRequete, 
										PrintWriter pw, Socket socket) throws IOException {
		
			pathToGet(line, typeDeRequete.length());			// récupération du path
	    	Path ressPath =  Paths.get(pathToGet(line, typeDeRequete.length()));	
	    	String ressStr = pathUtf8(ressPath);			    	
	    	File f = new File(ressStr);
	    	traitementFichiers(f, ressPath, pw, socket);
	}
	
	
	private void traitementDesRequetes(String line, String typeDeRequete,
										PrintWriter pw, Socket socket) throws IOException {
		if (typeDeRequete.equals("GET".toLowerCase())){
			traitementRequeteGet(line, typeDeRequete, pw, socket);   	
		} else { System.out.println("Autre type de requete ");} // autre type de requete que GET
	}
	
	
	public void receptionRequete(String line, BufferedReader br, 
									PrintWriter pw, Socket socket) throws IOException {
		System.out.println("----DEBUT REQUETE----");
		
		line = br.readLine();						// On arrête à la fin des entête qui est définie comme une ligne vide	
		
		while(!"".equals(line) && line != null){	// != null pour éviter les plantages
			System.out.println(line); 				// First line of the header request
			String typeRequest = typeDeRequete(line);
			traitementDesRequetes(line, typeRequest, pw, socket);
			line = br.readLine();					// next line of the header request		
		}
		
		System.out.println("----FIN REQUETE----");
		System.out.println();
	}
	
	
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(8080);
		
		while( true ) { 
			Socket socket = serverSocket.accept();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			String line = null;
			AfficheRequetesHttp objRequest= new AfficheRequetesHttp("Thread-1");
			
			objRequest.receptionRequete(line, br, pw, socket);
//			System.out.println("----DEBUT REQUETE----");
//			
//			line = br.readLine();						// On arrête à la fin des entête qui est définie comme une ligne vide	
//			
//			while(!"".equals(line) && line != null){	// != null pour éviter les plantages
//				System.out.println(line); 				// First line of the header request
//				String typeRequest = objRequest.typeDeRequete(line);
//				objRequest.traitementDesRequetes(line, typeRequest, w, socket);
//				line = br.readLine();					// next line of the header request		
//			}
//			
//			System.out.println("----FIN REQUETE----");
//			System.out.println();
			
			
			br.close();
    		pw.close();
			socket.close();
			}
		}


	}
