package covid19;

import java.io.*;
import java.net.*;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.json.simple.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ClientHandler extends Thread {

	Socket soketZaKomunikaciju;
	JSONArray nizIspitanika;

	FileWriter fajlUpisivac;
	BufferedReader klijentInput;
	PrintStream klijentOutput;
	JSONObject podaciIspitanik;
	JSONObject podaciCovid;
	JSONObject brziTest;
	JSONObject pcrTest;
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

				if (!input.equals("1") && !input.equals("2") && !input.equals("3")) {
					klijentOutput.println("Nepostojeca opcija, pokusajte ponovo.");
					klijentOutput.println();
				}

			} while (!input.equals("1") && !input.equals("2") && !input.equals("3"));

			while (input.equals("1")) {

				if (registracija()) {

					klijentOutput.println("Uspesno ste se registrovali. Sada se mozete prijaviti na sistem.");
					klijentOutput.println();

					input = "2";
				} else {
					klijentOutput.println();
					korisnickiMeni();
					input = klijentInput.readLine();
				}
			}

			if (input.equals("2")) {
				int brojacPokusaja = 0;

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

							if ((podaciCovid.get(username) == podaciCovid.get("admin")) && password.equals("admin")) {
								adminPanel();
								validacija = true;
								break;
							}

							if (!password.equals(podaciIspitanik.get("Password"))) {
								klijentOutput.println("Pogresan password. Pokusajte ponovo");
								break;
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

										JSONObject podaciTestSamoprocene = (JSONObject) podaciIspitanik
												.get("Test samoprocene");
																			 
										if (podaciTestSamoprocene != null) {
											long vremeSada = izracunajMilisekunde(zapisDatuma(new GregorianCalendar()));
											long vremeTesta = 0;
											
											String brziTestDatum = (String) podaciTestSamoprocene.get("Datum brzog testa");
											
											if (brziTestDatum != null) {
												vremeTesta = izracunajMilisekunde((String) podaciTestSamoprocene.get("Datum brzog testa"));
											} else {
												vremeTesta = izracunajMilisekunde((String) podaciTestSamoprocene.get("Datum PCR testa"));
											}
											
											
											if ((vremeSada - vremeTesta) > TimeUnit.HOURS.toMillis(24)) {

												testSamoprocene();

											} else {
												klijentOutput.println(
														"Vec ste testirali. Potrebno je sacekati 24h da biste uradili novi test."
																+ " Mozete pregledati rezultate prethodnog testa.");
												continue;
											}
										} else if (podaciIspitanik.get("Test samoprocene - Nadzor") != null) {
											podaciTestSamoprocene = (JSONObject) podaciIspitanik
													.get("Test samoprocene - Nadzor");
											long vremeSada = izracunajMilisekunde(zapisDatuma(new GregorianCalendar()));
											long vremeTesta = izracunajMilisekunde(
													(String) podaciTestSamoprocene.get("Datum testa samoprocene"));
											if ((vremeSada - vremeTesta) > TimeUnit.HOURS.toMillis(24)) {

												testSamoprocene();

											} else {

												klijentOutput.println(
														"Vec ste testirali. Potrebno je sacekati 24h da biste uradili novi test."
																+ " Mozete pregledati rezultate prethodnog testa.");
												continue;
											}

										} else {

											testSamoprocene();

										}

										klijentOutput.println("Mozete izvrsiti detaljniji uvid u rezultate testa.");

									} else if (unos.equals("2")) {
										
										JSONObject podaciTestSamoprocene = (JSONObject) podaciIspitanik
												.get("Test samoprocene");
								
										
										if (podaciTestSamoprocene != null) {
											
											String brziTest = (String) podaciTestSamoprocene.get("Status brzog testa");

											if (brziTest != null) {
												klijentOutput
														.println("---------------------------------------------------");
												klijentOutput.println("Rezultati testa samoprocene i brzog testa:");
												klijentOutput.println("Potvrdni odgovori na testu samoprocene: "
														+ podaciTestSamoprocene
																.get("Potvrdni odgovori na testu samoprocene"));
												klijentOutput.println("Odricni odgovori na testu samoprocene: "
														+ podaciTestSamoprocene
																.get("Odricni odgovori na testu samoprocene"));
												klijentOutput.println("Datum testiranja: "
														+ podaciTestSamoprocene.get("Datum brzog testa"));
												klijentOutput.println("Rezultat(status): "
														+ podaciTestSamoprocene.get("Status brzog testa"));
												klijentOutput
														.println("---------------------------------------------------");
											} else {
												klijentOutput
														.println("---------------------------------------------------");
												klijentOutput.println("Rezultati testa samoprocene i PCR testa:");
												klijentOutput.println("Potvrdni odgovori na testu samoprocene: "
														+ podaciTestSamoprocene
																.get("Potvrdni odgovori na testu samoprocene"));
												klijentOutput.println("Odricni odgovori na testu samoprocene: "
														+ podaciTestSamoprocene
																.get("Odricni odgovori na testu samoprocene"));
												klijentOutput.println("Datum testiranja: "
														+ podaciTestSamoprocene.get("Datum PCR testa"));
												klijentOutput.println("Rezultat(status): "
														+ podaciTestSamoprocene.get("Status PCR testa"));
												klijentOutput
														.println("---------------------------------------------------");

											}
											
											test = false;
											
										} 

										podaciTestSamoprocene = (JSONObject) podaciIspitanik
												.get("Test samoprocene - Nadzor");
										if (podaciTestSamoprocene != null) {
											klijentOutput
													.println("---------------------------------------------------");
											klijentOutput.println("Datum testa samoprocene: "
													+ podaciTestSamoprocene.get("Datum testa samoprocene"));
											klijentOutput.println("Status: " + podaciTestSamoprocene.get("Status"));
											klijentOutput
													.println("---------------------------------------------------");
											test = false;
										}

										if (test == true) {
											klijentOutput.println(
													"U bazi ne postoje rezultati sa Vasim kredencijalima. Pristupite testu samoprocene.");
										}
										
										
									} else {
										klijentOutput.println("Pogresan unos, pokusajte ponovo.");
									}
									
									
								}
								break;
							}

						}

					}

					if (podaciCovid.get(username) == null) {
						klijentOutput.println("Uneli ste nepostojeci username! Pokusajte ponovo.");
						brojacPokusaja++;
						if(brojacPokusaja > 5) {
							klijentOutput.println("Iskoristili ste sve pokusaje da se prijavite. "
							+ "Ponovo pristupite sistemu nakon 30 minuta da biste se registrovali.");
							return;
						}
					}
				}
				
				while(!input.equals("3")) {
					klijentOutput.println("Za odjavu sa sistema posaljite broj 3");
					input = klijentInput.readLine();
				}
			}

			if(input.equals("3")) {
				klijentOutput.println("Dovidjenja");
			}
			
			fajlUpisivac = new FileWriter("covid19.txt");
			fajlUpisivac.write(gson.toJson(nizIspitanika));
			fajlUpisivac.flush();

		} catch (IOException e) {
			// ovde je pokriveno ako klijent nije napisao 3 ali je stisnuo iksic,
			// terminate, nestalo struje
			if(podaciIspitanik.get("Ime") != null) {
				System.out.println("Klijent " + podaciIspitanik.get("Ime") + " " + podaciIspitanik.get("Prezime") + " je iznenada napustio sistem.");
			} else {
				System.out.println("Nepoznati klijent je iznenada napustio sistem.");
			}
				
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

		for (Object korisnik : nizIspitanika) {
			podaciCovid = (JSONObject) korisnik;
			if (podaciCovid.get(username) != null) {
				klijentOutput.println(
						"Vec postoji registrovani korisnik sa datim korisnickim imenom! Unesite drugaciji username.");
				return false;
			}
		}

		klijentOutput.println("Unesite password: ");
		String password = klijentInput.readLine();
		podaciIspitanik.put("Password", password);

		klijentOutput.println("Unesite Vase ime: ");
		String ime = klijentInput.readLine();
		while (ime.equals("") || !ime.matches("^[a-zA-Z]*$") || ime.length() < 1) {
			klijentOutput.println("Uneli ste neispravno ime, pokusajte ponovo");
			klijentOutput.println("Unesite Vase ime: ");
			ime = klijentInput.readLine();
		}
		podaciIspitanik.put("Ime", ime);

		klijentOutput.println("Unesite Vase prezime: ");
		String prezime = klijentInput.readLine();
		while (prezime.equals("") || !prezime.matches("^[a-zA-Z]*$") || prezime.length() < 1) {
			klijentOutput.println("Uneli ste neispravno prezime, pokusajte ponovo");
			klijentOutput.println("Unesite Vase prezime: ");
			prezime = klijentInput.readLine();
		}
		podaciIspitanik.put("Prezime", prezime);

		klijentOutput.println("Unesite Vas pol(M/Z): ");
		String pol = (klijentInput.readLine()).toUpperCase();
		while (!(pol.equals("M")) && !(pol.equals("Z"))) {
			klijentOutput.println("Uneli ste neispravnu oznaku pola, pokusajte ponovo");
			klijentOutput.println("Unesite Vase pol: ");
			pol = klijentInput.readLine();
		}
		podaciIspitanik.put("Pol", pol);

		klijentOutput.println("Unesite Vas email: ");
		String email = klijentInput.readLine();
		while (!(email.contains("@")) || !(email.contains("."))) {
			klijentOutput.println("Uneli ste neispravan email, pokusajte ponovo");
			klijentOutput.println("Unesite Vase email: ");
			email = klijentInput.readLine();
		}
		podaciIspitanik.put("Email", email);

		podaciCovid.put(username, podaciIspitanik);		

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

		int brojacDa = 0;
		int brojacPitanja = 0;
		String[] odgovori = odgovor.split("_");
		for (String rezultat : odgovori) {
			if (rezultat.toLowerCase().equals("da")) {
				brojacDa++;
			}
			brojacPitanja++;
		}

		Server.podaciAdmin.put("Broj testiranja", ( ((Number) Server.podaciAdmin.get("Broj testiranja")).intValue() + 1));

		if (brojacDa >= 2) {
			
			while (true) {
				klijentOutput.println();
				klijentOutput.println("Potrebno je da se testirate. Odaberite test:");
				klijentOutput.println("1. Brzi test");
				klijentOutput.println("2. PCR test");
				klijentOutput.println("Vas izbor:");
				String unos = klijentInput.readLine();
				
				if(unos.equals("1")) {
					brziTest(brojacPitanja, brojacDa);
					break;
				} else if(unos.equals("2")) {
					PCRTest(brojacPitanja, brojacDa);
					break;
				} 
				
				klijentOutput.println("Pogresan unos, pokusajte ponovo.");
			}
			

		} else {
			Server.podaciAdmin.put("Broj ispitanika pod nadzorom",
					( ((Number) Server.podaciAdmin.get("Broj ispitanika pod nadzorom")).intValue() + 1));

			GregorianCalendar datum = new GregorianCalendar();
			JSONObject testNadzor = new JSONObject();
			testNadzor.put("Status", "POD NADZOROM");
			testNadzor.put("Datum testa samoprocene", zapisDatuma(datum));
			podaciIspitanik.put("Test samoprocene - Nadzor", testNadzor);
			klijentOutput.println("Pod nadzorom ste. Potrebno je da ponovite test samoprocene u roku od 7 dana.");
			klijentOutput.println();

		}
		
		

	}
	
	
	
	@SuppressWarnings("unchecked")
	public void brziTest(int brojacPitanja, int brojacDa) {
		
		String brziTestStatus = (new Random().nextBoolean()) ? "POZITIVAN" : "NEGATIVAN";
		
		GregorianCalendar datum = new GregorianCalendar();

		if (brziTestStatus.equals("POZITIVAN")) {
			Server.podaciAdmin.put("Broj pozitivnih testova",
					( ((Number) Server.podaciAdmin.get("Broj pozitivnih testova")).intValue() + 1));
		} else {
			Server.podaciAdmin.put("Broj negativnih testova",
					( ((Number) Server.podaciAdmin.get("Broj negativnih testova")).intValue() + 1));
		}

		brziTest = new JSONObject();
		brziTest.put("Potvrdni odgovori na testu samoprocene", brojacDa);
		brziTest.put("Odricni odgovori na testu samoprocene", brojacPitanja - brojacDa);
		brziTest.put("Status brzog testa", brziTestStatus);
		brziTest.put("Datum brzog testa", zapisDatuma(datum));
		podaciIspitanik.put("Test samoprocene", brziTest);

		klijentOutput.println("\nRezultat brzog testa: " + brziTestStatus);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public void PCRTest(int brojacPitanja, int brojacDa) {
		
		try {
			
			klijentOutput.println("Vas PCR test je na cekanju.");
			Thread.sleep(60000);
	
			String pcrTestStatus = (new Random().nextBoolean()) ? "POZITIVAN" : "NEGATIVAN";
			
			GregorianCalendar datum = new GregorianCalendar();

			
			klijentOutput.println("\nVas PCR test je u obradi...");
			Thread.sleep(60000);

			
			if (pcrTestStatus.equals("POZITIVAN")) {
				Server.podaciAdmin.put("Broj pozitivnih testova",
						( ((Number) Server.podaciAdmin.get("Broj pozitivnih testova")).intValue() + 1));
			} else {
				Server.podaciAdmin.put("Broj negativnih testova",
						( ((Number) Server.podaciAdmin.get("Broj negativnih testova")).intValue() + 1));
			}
			
			klijentOutput.println("\nVas PCR test je gotov i uskoro cete dobiti rezultate");
			Thread.sleep(3000);
			
			pcrTest = new JSONObject();

			pcrTest.put("Potvrdni odgovori na testu samoprocene", brojacDa);
			pcrTest.put("Odricni odgovori na testu samoprocene", brojacPitanja - brojacDa);
			pcrTest.put("Status PCR testa", pcrTestStatus);
			pcrTest.put("Datum PCR testa", zapisDatuma(datum));
			podaciIspitanik.put("Test samoprocene", pcrTest);
	
			
			klijentOutput.println("\nRezultati PCR testa: " + pcrTestStatus);
		} catch (InterruptedException e) {
			System.out.println("GRESKA");
		}
			
	}

	
	
	public String zapisDatuma(GregorianCalendar datum) {

		int godina = datum.get(GregorianCalendar.YEAR);
		int mesec = datum.get(GregorianCalendar.MONTH);
		int dan = datum.get(GregorianCalendar.DAY_OF_MONTH);
		int sat = datum.get(GregorianCalendar.HOUR_OF_DAY);
		int minut = datum.get(GregorianCalendar.MINUTE);
		int sekund = datum.get(GregorianCalendar.SECOND);

		return (dan + "/" + (mesec + 1) + "/" + godina + " " + sat + ":" + minut + ":" + sekund);
	}

	
	
	public long izracunajMilisekunde(String datum) {

		DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
		DateTime parsiranDatum = DateTime.parse(datum, formatter);
		long milisekunde = parsiranDatum.getMillis();
		return milisekunde;
	}

	
	
	private void adminPanel() {
		
		klijentOutput.println("\nADMINISTRATORSKI PANEL");
		
		klijentOutput.println("\nPregled statistike o virusu COVID19:");
		klijentOutput.println("---------------------------------------------------------");
		klijentOutput.println("Ukupan broj testiranja: " +  Server.podaciAdmin.get("Broj testiranja"));
		klijentOutput.println("Ukupan broj pozitivnih testova: " + Server.podaciAdmin.get("Broj pozitivnih testova"));
		klijentOutput.println("Ukupan broj negativnih testova: " + Server.podaciAdmin.get("Broj negativnih testova"));
		klijentOutput.println("Ukupan broj ispitanika pod nadzorom: " + Server.podaciAdmin.get("Broj ispitanika pod nadzorom"));
		klijentOutput.println("---------------------------------------------------");
		
		
		LinkedList<String> listaPozitivnih = new LinkedList<String>();
		LinkedList<String> listaNegativnih = new LinkedList<String>();
		LinkedList<String> listaNadzor = new LinkedList<String>();
		
		klijentOutput.println("\nLista svih korisnika i njihovo trenutno stanje:");
		klijentOutput.println("--------------------------------------------------------------------------------");

		for(Object kljuc : podaciCovid.keySet()) {
			String korisnik = (String) kljuc;
			
			if(korisnik.equals("admin")) {
				continue;
			}
			
			podaciIspitanik = (JSONObject) podaciCovid.get(korisnik);
			
			klijentOutput.print(podaciIspitanik.get("Ime") + "   " + podaciIspitanik.get("Prezime") + "   " + podaciIspitanik.get("Email"));
			
			JSONObject podaciBrziIliPcr = (JSONObject) podaciIspitanik.get("Test samoprocene");
			
			if(podaciBrziIliPcr != null) {
				String brziTest = (String) podaciBrziIliPcr.get("Status brzog testa");
				
				if(brziTest != null) {
					klijentOutput.print("   " + "Trenutno stanje: " + podaciBrziIliPcr.get("Status brzog testa"));
					
					boolean status = (podaciBrziIliPcr.get("Status brzog testa")).equals("POZITIVAN") ? true : false;
					
					if(status) {
						listaPozitivnih.add(podaciIspitanik.get("Ime") + "   " + podaciIspitanik.get("Prezime") + "   " + podaciIspitanik.get("Email"));
					} else {
						listaNegativnih.add(podaciIspitanik.get("Ime") + "   " + podaciIspitanik.get("Prezime") + "   " + podaciIspitanik.get("Email"));
					}
				} else {
					klijentOutput.print("   " + "Trenutno stanje: " + podaciBrziIliPcr.get("Status PCR testa"));
					
					boolean status = (podaciBrziIliPcr.get("Status PCR testa")).equals("POZITIVAN") ? true : false;
					
					if(status) {
						listaPozitivnih.add(podaciIspitanik.get("Ime") + "   " + podaciIspitanik.get("Prezime") + "   " + podaciIspitanik.get("Email"));
					} else {
						listaNegativnih.add(podaciIspitanik.get("Ime") + "   " + podaciIspitanik.get("Prezime") + "   " + podaciIspitanik.get("Email"));
					}
				}
				
			} else {
				JSONObject podaciNadzor = (JSONObject) podaciIspitanik.get("Test samoprocene - Nadzor");
				
				klijentOutput.print("   " + "Trenutno stanje: " + podaciNadzor.get("Status"));
				
				listaNadzor.add(podaciIspitanik.get("Ime") + "   " + podaciIspitanik.get("Prezime") + "   " + podaciIspitanik.get("Email"));
			}
			
			klijentOutput.println();
		}
		
		klijentOutput.println("--------------------------------------------------------------------------------");
		
		
		klijentOutput.println("\nLista svih pozitivnih korisnika:");
		klijentOutput.println("--------------------------------------------------------------------------------");
		
		for(String pozitivan : listaPozitivnih) {
			klijentOutput.println(pozitivan);
		}
		klijentOutput.println("--------------------------------------------------------------------------------");

		
		klijentOutput.println("\nLista svih negativnih korisnika:");
		klijentOutput.println("--------------------------------------------------------------------------------");
		
		for(String negativan : listaNegativnih) {
			klijentOutput.println(negativan);
		}
		klijentOutput.println("--------------------------------------------------------------------------------");
		
		
		klijentOutput.println("\nLista svih korisnika pod nadzorom:");
		klijentOutput.println("--------------------------------------------------------------------------------");
		
		for(String nadzor : listaNadzor) {
			klijentOutput.println(nadzor);
		}
		klijentOutput.println("--------------------------------------------------------------------------------");
		
		
		klijentOutput.println();

	}
}
