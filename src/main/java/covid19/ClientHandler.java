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
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		
		try {
			klijentInput = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			klijentOutput = new PrintStream(soketZaKomunikaciju.getOutputStream());
			podaciIspitanik = new JSONObject();
			podaciCovid = new JSONObject();
			
			String input;
			
			do {
				
				korisnickiMeni();
				
				input = klijentInput.readLine();

			} while (input.startsWith("GRESKA"));
			
			
			if(input.equals("1")) {
				if(nizIspitanika.isEmpty()) {
					klijentOutput.println("Unesite username: ");
					String username = klijentInput.readLine();
					
					klijentOutput.println("Unesite password: ");
					String password = klijentInput.readLine();
					podaciIspitanik.put("Password", password);
					
					klijentOutput.println("Unesite Vase ime: ");
					String ime = klijentInput.readLine();
					while(ime.matches("[0-9]+") && ime.length()<2) {
						klijentOutput.println("Uneli ste neispravno ime, pokusajte ponovo");
						klijentOutput.println("Unesite Vase ime: ");
						ime = klijentInput.readLine();
					}
					podaciIspitanik.put("Ime", ime);
			
					
					klijentOutput.println("Unesite Vase prezime: ");
					String prezime = klijentInput.readLine();
					while(prezime.matches("[0-9]+") && prezime.length()<2) {
						klijentOutput.println("Uneli ste neispravno prezime, pokusajte ponovo");
						klijentOutput.println("Unesite Vase prezime: ");
						prezime = klijentInput.readLine();
					}
					podaciIspitanik.put("Prezime", prezime);
					
					klijentOutput.println("Unesite Vas pol(M/Z): ");
					String pol = (klijentInput.readLine()).toUpperCase();
					while(!(pol.equals("M")) && !(pol.equals("Z"))) {
						klijentOutput.println("Uneli ste neispravnu oznaku pola, pokusajte ponovo");
						klijentOutput.println("Unesite Vase pol: ");
						pol = klijentInput.readLine();
					}
					podaciIspitanik.put("Pol", pol);
					
					klijentOutput.println("Unesite Vas email: ");
					String email = klijentInput.readLine();
					while(!(email.contains("@")) || !(email.contains("."))) {
						klijentOutput.println("Uneli ste neispravan email, pokusajte ponovo");
						klijentOutput.println("Unesite Vase email: ");
						email = klijentInput.readLine();
					}
					podaciIspitanik.put("Email", email);
					
					podaciCovid.put(username, podaciIspitanik);
					nizIspitanika.add(podaciCovid);
					
					fajlUpisivac.write(nizIspitanika.toJSONString());
					fajlUpisivac.flush();
				}
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
