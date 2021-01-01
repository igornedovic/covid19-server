package covid19;

import java.io.*;
import java.net.*;


import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Server {

	public static JSONObject podaciAdmin = new JSONObject();
	public static JSONObject podaciCovidAdmin = new JSONObject();
	 

	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		int port = 4545;
		ServerSocket serverSoket = null;
		Socket soketZaKomunikaciju = null;
		JSONArray nizIspitanika = new JSONArray();
		JSONParser parser = null;
		FileReader reader = null;
		FileWriter fajlUpisivac = null;

		
		try {
			serverSoket = new ServerSocket(port);
			File fajl = new File("covid19.txt");
			
			if(fajl.exists()) {
				parser = new JSONParser();
				reader = new FileReader(fajl);
				Object obj = parser.parse(reader);
				nizIspitanika = (JSONArray) obj;
			} 
			
				
			if(nizIspitanika.isEmpty()) {
				podaciAdmin.put("Password", "admin");
				podaciCovidAdmin.put("admin", podaciAdmin);
				nizIspitanika.add(podaciCovidAdmin);
			}
			
			
			while(true) {
				System.out.println("Cekam na konekciju...");
				soketZaKomunikaciju = serverSoket.accept();
				System.out.println("Doslo je do konekcije!");
				ClientHandler klijent = new ClientHandler(soketZaKomunikaciju, nizIspitanika);
				klijent.start();
			}
				
			
		} catch (IOException e) {
			System.out.println("Greska prilikom pokretanja servera!");
		} catch (ParseException e) {
			System.out.println("Greska prilikom ucitavanja covid podataka!");

		}
		
		
	}

}
