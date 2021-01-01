package covid19;

import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.json.simple.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

public class ClientHandler extends Thread {
	
	Socket soketZaKomunikaciju;
	JSONArray nizIspitanika;
	
	FileWriter fajlUpisivac;
	BufferedReader klijentInput;
	PrintStream klijentOutput;
	JSONObject podaciIspitanik;
	JSONObject podaciCovid;
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	String username;
	
	public ClientHandler(Socket soketZaKomunikaciju, JSONArray nizIspitanika) {
		this.soketZaKomunikaciju = soketZaKomunikaciju;
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
						klijentOutput.println();
						
						input = "2";
				}
					
			}
			
			
			if(input.equals("2")) {
				
				boolean validacija = false;
				while (!validacija) {
					
					klijentOutput.println("Username:");
					username = klijentInput.readLine();

					klijentOutput.println("Password:");
					String password = klijentInput.readLine();

					for (Object korisnik : nizIspitanika) {
						podaciCovid = (JSONObject) korisnik;

						if (podaciCovid.get(username) != null) {

							podaciIspitanik = (JSONObject) podaciCovid.get(username);
							if (!password.equals(podaciIspitanik.get("Password"))) {
								klijentOutput.println("Pogresan password. Pokusajte ponovo");
							} else {
								validacija = true;

								boolean test = true;
								while (test) {
									klijentOutput.println();
									klijentOutput.println("1. Test samoprocene");
									klijentOutput.println("2. Rezultati testa");
									klijentOutput.println("Vas izbor:");
									String unos = klijentInput.readLine();

									if (unos.equals("1")) {
										/*long vremeSada = izracunajMilisekunde(new GregorianCalendar());
										long vremeTesta = izracunajMilisekunde(testSamoprocene());
										if ((vremeSada - vremeTesta) > TimeUnit.HOURS.toMillis(24)) {
											testSamoprocene();
										} else {
											klijentOutput.println("Vec ste testirali. Potrebno je sacekati 24h da biste uradili novi test.");
										}
										test = false;*/
									} else if (unos.equals("2")) {
										JSONObject podaciTestSamoprocene = (JSONObject) podaciIspitanik
												.get("Test samoprocene");
										if (podaciTestSamoprocene != null) {
											klijentOutput
													.println("---------------------------------------------------");
											klijentOutput.println("Rezultati testa samoprocene i brzog testa:");
											klijentOutput.println("Datum testiranja: "
													+ podaciTestSamoprocene.get("Datum brzog testa"));
											klijentOutput.println("Rezultat(status): "
													+ podaciTestSamoprocene.get("Status brzog testa"));
											klijentOutput
													.println("---------------------------------------------------");
											test = false;
										}
									} else {
										klijentOutput.println("Pogresan unos, pokusajte ponovo.");
									}
								}
								break;
							}

						}

					}
					
					klijentOutput.println("Uneli ste nepostojeci username! Pokusajte ponovo.");
				}
			}
			
			fajlUpisivac = new FileWriter("covid19.txt");
			fajlUpisivac.write(gson.toJson(nizIspitanika));
			fajlUpisivac.flush();
		
			
		} catch (IOException e) {
			// ovde je pokriveno ako klijent nije napisao ***quit ali je stisnuo iksic,
			// terminate, nestalo struje
			System.out.println("Klijent je iznenada napustio sistem.");
			try {
				fajlUpisivac = new FileWriter("covid19.txt");
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
		username = klijentInput.readLine();
		
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
	
	@SuppressWarnings("unchecked")
	public void testSamoprocene() throws IOException {
		
		klijentOutput.println();
		klijentOutput.println("---------------------------------------------------------------");
		klijentOutput.println("Dobrodosli na test samoprocene. Postavicemo vam kratka pitanja:");
		klijentOutput.println();

		String odgovor = "";
		
		klijentOutput.println("Da li ste putovali van Srbije u okviru 14 dana pre početka simptoma? (da/ne):");
		odgovor += klijentInput.readLine() + "_";
		
		klijentOutput.println("Da li ste bili u kontaku sa zaraženim osobama? (da/ne):");
		odgovor += klijentInput.readLine() + "_";
		
		klijentOutput.println("Šta imate od simptoma (da/ne):");
		klijentOutput.println("Povišena temperatura");
		odgovor += klijentInput.readLine() + "_";

		klijentOutput.println("Kasalj");
		odgovor += klijentInput.readLine() + "_";
		
		klijentOutput.println("Opsta slabost");
		odgovor += klijentInput.readLine() + "_";

		klijentOutput.println("Gubitak cula mirisa");
		odgovor += klijentInput.readLine() + "_";
		
		klijentOutput.println("Gubitak/promena cula ukusa");
		odgovor += klijentInput.readLine() + "_";
		
		GregorianCalendar datum = new GregorianCalendar();
		int brojacDa = 0;
		int brojacPitanja = 0;
		String [] odgovori = odgovor.split("_");
		for (String rezultat : odgovori) {
			if(rezultat.toLowerCase().equals("da")) {
				brojacDa++;
			}
			brojacPitanja++;
		}
		
		if(brojacDa >= 2) {
			String brziTestStatus = (new Random().nextBoolean()) ? "POZITIVAN" : "NEGATIVAN";
			
			JSONObject brziTest = new JSONObject();
			brziTest.put("Potvrdni odgovori", brojacDa);
			brziTest.put("Odricni odgovori", brojacPitanja - brojacDa);
			brziTest.put("Status brzog testa", brziTestStatus);
			brziTest.put("Datum brzog testa", datumBrzogTesta(datum));
			podaciIspitanik.put("Test samoprocene", brziTest);
			
			klijentOutput.println();
			klijentOutput.println("---------------------------------------------------------------");
			klijentOutput.println("Rezultat brzog testa: " + brziTestStatus);	
			
		} else {
			podaciIspitanik.put("Test samoprocene-nadzor", "Status pracenja(Pod nadzorom)");
			klijentOutput.println("Pod nadzorom ste. Potrebno je da ponovite test samoprocene u roku od 20 dana.");
		}
		
	
	}
	
	public String datumBrzogTesta(GregorianCalendar datum) {
		
		int godina = datum.get(GregorianCalendar.YEAR);
		int mesec = datum.get(GregorianCalendar.MONTH);
		int dan = datum.get(GregorianCalendar.DAY_OF_MONTH);
		int sat = datum.get(GregorianCalendar.HOUR_OF_DAY);
		int minut = datum.get(GregorianCalendar.MINUTE);
		int sekund = datum.get(GregorianCalendar.SECOND);
		
		
		return (dan+"-"+(mesec+1)+"-"+godina+" "+sat+":"+minut+":"+sekund);
	}
	
	public long izracunajMilisekunde(GregorianCalendar datum) {
		
		int sat = datum.get(GregorianCalendar.HOUR_OF_DAY);
		int minut = datum.get(GregorianCalendar.MINUTE);
		int sekund = datum.get(GregorianCalendar.SECOND);
		int milisekund = datum.get(GregorianCalendar.MILLISECOND);
		
		return (sat * minut * sekund * milisekund);
	}

}



