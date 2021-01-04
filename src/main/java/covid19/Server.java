package covid19;

import java.io.*;
import java.net.*;


import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Server {

	public static JSONObject podaciAdmin = null;
	public static JSONObject podaciCovidAdmin = null;
	 

	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		int port = 4545;
		ServerSocket serverSoket = null;
		Socket soketZaKomunikaciju = null;
		JSONArray nizIspitanika = new JSONArray();
		JSONParser parser = null;
		FileReader reader = null;

		
		try {
			serverSoket = new ServerSocket(port);
			File fajl = new File("covid19.txt");
			
			if(!fajl.exists()) {
				podaciAdmin = new JSONObject();
				podaciCovidAdmin = new JSONObject();
				
				podaciAdmin.put("Password", "admin");
				podaciAdmin.put("Broj testiranja", 0);
				podaciAdmin.put("Broj pozitivnih testova", 0);
				podaciAdmin.put("Broj negativnih testova", 0);
				podaciAdmin.put("Broj ispitanika pod nadzorom", 0);
				podaciCovidAdmin.put("admin", podaciAdmin);
				nizIspitanika.add(podaciCovidAdmin);
				
			} else {
				parser = new JSONParser();
				reader = new FileReader(fajl);
				Object obj = parser.parse(reader);
				nizIspitanika = (JSONArray) obj;
				
				podaciCovidAdmin = (JSONObject) nizIspitanika.get(0);
				podaciAdmin = (JSONObject) podaciCovidAdmin.get("admin");
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
