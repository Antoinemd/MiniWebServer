package serveur;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;


//import org.tpserver.HtmlPage;


public class AfficheRequetesHttp {
		
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
	
	public String typeOfRequest(String line){
		return line.substring(0, line.indexOf(" ")).toLowerCase(); 
	}
	
	public String sendHeaderHTML (String mimeType, int codeRequest) {
		//		w.println("Content-Type: text/html; charset=UTF-8");
		String header = "HTTP/1.1 " + codeRequest + " <br/>" + "Date: " + getServerTime() + 
						"Content-Type: " + mimeType + "; charset=UTF-8" + "Expires: -1" + "\n";
		return header;
	}
	
	public String listingFilesOfDirectory(File f){
		// Listing du contenu du répertoire
		File[] listOfFiles = f.listFiles();
		StringBuilder contenuRep = new StringBuilder();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				contenuRep.append(("<a href ='" + file.getAbsolutePath() + "'>" + file.getName() + "</a><br/>"));
			}
			if (file.isDirectory()) {
				contenuRep.append(("<a href ='" + file.getAbsolutePath() + "'>" + file.getName() + "</a><br/>"));
			}
		}	// end of: forEach		
		return contenuRep.toString();
	}
	
	public void displayFileContent() {
		
	}

	
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(8080);
		
		while( true ) { 
			Socket socket = serverSocket.accept();
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter w = new PrintWriter(socket.getOutputStream(), true);
			String line = null;
			AfficheRequetesHttp objRequest= new AfficheRequetesHttp();
			
			System.out.println("----DEBUT REQUETE----");
			// On arrête à la fin des entête qui est définie comme une ligne vide
			line = br.readLine();			
			while(!"".equals(line) && line != null) { // != null pour éviter les plantages
				// First line of the header request
				System.out.println(line); 	
				String typeRequest = objRequest.typeOfRequest(line);
				
				if (typeRequest.equals("GET".toLowerCase())){
					// récupération du path
			    	Path ressourceRequest =  Paths.get(line.substring(typeRequest.length()+1, line.length()-9));	
			    	File f = new File(ressourceRequest.toString());
			    				    	
			    	if(f.exists()) {			    		
			    		if (f.isDirectory()) {
			    			String mimeType = Files.probeContentType(ressourceRequest);
			    			
				    		w.println(objRequest.sendHeaderHTML(mimeType, 200));
							w.println(objRequest.listingFilesOfDirectory(f));
						} else {
							if (Files.isRegularFile(ressourceRequest)) {
								// marche que sur Chrome ...
								Files.copy(ressourceRequest, socket.getOutputStream());
							} else {
								System.out.println("File isn't regular.");
							}
						}	// enf of: if(isDirectory / isFile)
			    	} else {
			    		System.out.println("la ressource n'existe pas"); // erreur 404
			    		
			    		w.println(objRequest.sendHeaderHTML("text/html", 404));
						w.println("Contenu introuvable / indisponible 404 Not Found :/ ");
			    	}
					
				} else {	// autre type de requete que GET
					System.out.println("Autre type de requete ");
				}
				// next line of the header request
				line = br.readLine();		
			}
			
			System.out.println("----FIN REQUETE----");
			System.out.println();
			
			
			br.close();
    		w.close();
			socket.close();
			}
		}
	}
