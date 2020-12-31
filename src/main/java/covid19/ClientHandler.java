package covid19;

import java.io.*;
import java.net.*;
import org.json.simple.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ClientHandler extends Thread {
	
	Socket soketZaKomunikaciju;
	FileWriter fajlUpisivac;
	JSONArray nizIspitanika;
	
	BufferedReader klijentInput;
	PrintStream klijentOutput;
	JSONObject podaciIspitanik;
	JSONObject podaciCovid;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	
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
				
				if(!input.equals("1") && !input.equals("2") && !input.equals("3")) {
					klijentOutput.println("Nepostojeca opcija, pokusajte ponovo.");
					klijentOutput.println();
				}

			} while (!input.equals("1") && !input.equals("2") && !input.equals("3"));
			
			
			if(input.equals("1")) {
					if(registracija()) {
						klijentOutput.println("Uspesno ste se registrovali. Sada se mozete prijaviti na sistem.");
						
						Server.podaciAdmin.put("Broj ispitanika", 1);
						fajlUpisivac.write(gson.toJson(nizIspitanika));
						fajlUpisivac.flush();
						
				}
				
				
			}
			
		
			
			
		} catch (IOException e) {
			// ovde je pokriveno ako klijent nije napisao ***quit ali je stisnuo iksic,
			// terminate, nestalo struje
			System.out.println("Klijent je iznenada napustio sistem.");
			try {
				fajlUpisivac.write(gson.toJson(nizIspitanika));
				fajlUpisivac.flush();
			} catch (IOException e1) {
				System.out.println("Greska prilikom upisivanja u bazu ispitanika.");
			}
		}
		
	}
	
	public void korisnickiMeni() {
		klijentOutput.println("KORISNICKI MENI:");
		klijentOutput.println("1. Registracija");
		klijentOutput.println("2. Prijava");
		klijentOutput.println("3. Izlaz");
		klijentOutput.println("Vas izbor:");
		
	}
	
	@SuppressWarnings("unchecked")
	public boolean registracija() throws IOException {
		klijentOutput.println("Unesite username: ");
		String username = klijentInput.readLine();
		
		klijentOutput.println("Unesite password: ");
		String password = klijentInput.readLine();
		podaciIspitanik.put("Password", password);
		
		klijentOutput.println("Unesite Vase ime: ");
		String ime = klijentInput.readLine();
		while(ime.equals("") || !ime.matches("^[a-zA-Z]*$") || ime.length()<1) {
			klijentOutput.println("Uneli ste neispravno ime, pokusajte ponovo");
			klijentOutput.println("Unesite Vase ime: ");
			ime = klijentInput.readLine();
		}
		podaciIspitanik.put("Ime", ime);

		
		klijentOutput.println("Unesite Vase prezime: ");
		String prezime = klijentInput.readLine();
		while(prezime.equals("") || !prezime.matches("^[a-zA-Z]*$") || prezime.length()<1) {
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
		
		return true;
	}

}
