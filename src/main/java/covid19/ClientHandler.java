package covid19;

import java.io.*;
import java.net.*;
import org.json.simple.*;

public class ClientHandler extends Thread {
	
	Socket soketZaKomunikaciju;
	FileWriter fajlUpisivac;
	JSONArray nizIspitanika;
	
	BufferedReader klijentInput;
	PrintStream klijentOutput;
	JSONObject podaciIspitanik;
	JSONObject podaciCovid;
	
	
	public ClientHandler(Socket soketZaKomunikaciju, FileWriter fajlUpisivac, JSONArray nizIspitanika) {
		this.soketZaKomunikaciju = soketZaKomunikaciju;
		this.fajlUpisivac = fajlUpisivac;
		this.nizIspitanika = nizIspitanika;
	}
	
	
	@Override
	public void run() {
		
		try {
			klijentInput = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			klijentOutput = new PrintStream(soketZaKomunikaciju.getOutputStream());
			
			String input;
			
			do {
				
				korisnickiMeni();
				
				input = klijentInput.readLine();

			} while (input.startsWith("GRESKA"));
			
			if(input.equals("1") || input.equals("2")) {
				klijentOutput.println("Izabrali ste registraciju ili prijavu.");
				
			} else if (input.equals("3")) {
				klijentOutput.println("Dovidjenja");
				
			}
			
		
			
			
		} catch (IOException e) {
			// ovde je pokriveno ako klijent nije napisao ***quit ali je stisnuo iksic,
			// terminate, nestalo struje
			e.printStackTrace();
		}
		
	}
	
	public void korisnickiMeni() {
		klijentOutput.println("KORISNICKI MENI:");
		klijentOutput.println("1. Registracija");
		klijentOutput.println("2. Prijava");
		klijentOutput.println("3. Izlaz");
		klijentOutput.println("Vas izbor:");
		
	}

}
