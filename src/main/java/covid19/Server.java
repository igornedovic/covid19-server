package covid19;

import java.io.*;
import java.net.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Server {

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
			
			if(fajl.exists()) {
				parser = new JSONParser();
				reader = new FileReader(fajl);
				Object obj = parser.parse(reader);
				nizIspitanika = (JSONArray) obj;
			}
			
			while(true) {
				System.out.println("Cekam na konekciju...");
				soketZaKomunikaciju = serverSoket.accept();
				System.out.println("Doslo je do konekcije!");
				FileWriter fajlUpisivac = new FileWriter("covid19.txt");
				
				ClientHandler klijent = new ClientHandler(soketZaKomunikaciju, fajlUpisivac, nizIspitanika);
				klijent.start();
			}
			
			
			
		} catch (IOException e) {
			System.out.println("Greska prilikom pokretanja servera!");
		} catch (ParseException e) {
			System.out.println("Greska prilikom ucitavanja covid podataka!");

		}
		
		
	}

}
