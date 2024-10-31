package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ServerNit extends Thread {
	
    ArrayList<ServerNit> korisnici;
    public String ime, prezime, adresa, username, password, email,CVV, iznosUplate;
    public String brojKartice, JMBG;
    double ukupneUplate;
    public boolean prijavljen = false;
    
    BufferedReader ulazniTokOdKlijenta = null;
    PrintStream izlazniTokKaKlijentu = null;
    Socket soketZaKomunikaciju = null;

    public ServerNit(Socket soket, ArrayList<ServerNit> korisnici) {
        this.soketZaKomunikaciju = soket;
        this.korisnici = korisnici;
    }
    
    @Override
    public void run() {
        try {
            ulazniTokOdKlijenta = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
            izlazniTokKaKlijentu = new PrintStream(soketZaKomunikaciju.getOutputStream());
            
            izlazniTokKaKlijentu.println("DOBRODOSLI NA APLIKACIJU ***POMOZIMO ZAJEDNO*** ");
            izlazniTokKaKlijentu.println();
            do {
            	
                izlazniTokKaKlijentu.println("Unesite broj opcije koju zelite da izaberete. Za izlaz unesite quit");
                izlazniTokKaKlijentu.println("1. Izvrsi uplatu");
                izlazniTokKaKlijentu.println("2. Ukupna prikupljena sredstva");
                izlazniTokKaKlijentu.println("3. Registracija i prijava");
                izlazniTokKaKlijentu.println("4. Poslednje uplate");
                izlazniTokKaKlijentu.println("Unos: ");
                
                try {
                String opcija = ulazniTokOdKlijenta.readLine();
                
                if (opcija.startsWith("quit") || opcija==null) {
                    break;
                }
                    int opcijaBr = Integer.parseInt(opcija);

                    if (opcijaBr == 1) { //Uplata
                        if (prijavljen == false) {
                            izlazniTokKaKlijentu.println("Ime:");
                            ime = ulazniTokOdKlijenta.readLine();
                            if(ime.equals("quit")) break;
                            izlazniTokKaKlijentu.println("Prezime:");
                            prezime = ulazniTokOdKlijenta.readLine();
                            if(prezime.equals("quit")) break;
                            izlazniTokKaKlijentu.println("Adresa:");
                            adresa = ulazniTokOdKlijenta.readLine();
                            if(adresa.equals("quit")) break;
                            do {
                                izlazniTokKaKlijentu.println("Broj platne kartice:");
                                brojKartice = ulazniTokOdKlijenta.readLine();
                                if (brojKartice.startsWith("quit")) {
                                    break;
                                }
                                if (IspravanFormatKartice(brojKartice)==false) {
                                    izlazniTokKaKlijentu.println("Pogresan format kartice!Pokusaj ponovo. Za izlaz unesi quit");
                                }
                            } while (IspravanFormatKartice(brojKartice)==false);
                            
                            do {
                            izlazniTokKaKlijentu.println("CVV:");
                            CVV = ulazniTokOdKlijenta.readLine();
                            
                            if (CVV.startsWith("quit")) {
                                break;
                            }
                            if (trocifren(CVV)==false) {
                                izlazniTokKaKlijentu.println("Pogresan unos!Pokusaj ponovo. Za izlaz unesi quit");
                            } 
                            }while(trocifren(CVV)!=true);
                            do {
                                izlazniTokKaKlijentu.println("Unesite iznos uplate:");
                                iznosUplate = ulazniTokOdKlijenta.readLine();
                                if (iznosUplate.startsWith("quit")) {
                                    break;
                                }
                                if (minimalnaUplata(iznosUplate)==false) {
                                    izlazniTokKaKlijentu.println("Minimalna uplata je 200 dinara. Pokusaj ponovo. Za izlaz unesi quit");
                                }
                            } while (minimalnaUplata(iznosUplate)==false);
                            if(postojiUBazi(brojKartice, CVV)==true) { 
                            	ukupneUplate = Double.parseDouble(procitajUkupneUplate());
                            	ukupneUplate += Double.parseDouble(iznosUplate);
                            	upisiUFajl(Double.toString(ukupneUplate));
                            	String podaci = ime + " " + prezime + " " + adresa + " " + LocalDate.now() + " " + LocalTime.now() + " " + iznosUplate;
                            	dodajZapisOUplati(podaci);
                            	String podaci1 = generisiRacun(ime, prezime, adresa, LocalDate.now() ,  Double.parseDouble(iznosUplate));
                            	izdajFajlKorisniku(podaci1);
                            	izlazniTokKaKlijentu.println("HVALA NA DONACIJI!");
                            	}
                               else {
                            	   izlazniTokKaKlijentu.println("Uplata neuspesna! Kartica i/li cvv ne postoje u bazi.");
                               }
                        } else {
                            izlazniTokKaKlijentu.println("Ime:");
                            ime = ulazniTokOdKlijenta.readLine();
                            if(ime.equals("quit")) break;
                            izlazniTokKaKlijentu.println("Prezime:");
                            prezime = ulazniTokOdKlijenta.readLine();
                            if(prezime.equals("quit")) break;
                            izlazniTokKaKlijentu.println("Adresa:");
                            adresa = ulazniTokOdKlijenta.readLine();
                            if(adresa.equals("quit")) break;
                            izlazniTokKaKlijentu.println("CVV:");
                            CVV = ulazniTokOdKlijenta.readLine();
                             
                            if (CVV.startsWith("quit")) {
                                break;
                            }
                            if (!trocifren(CVV)) {
                                izlazniTokKaKlijentu.println("Pogresan unos!Pokusaj ponovo. Za izlaz unesi quit");
                            }
                            do {
                                izlazniTokKaKlijentu.println("Unesite iznos uplate:");
                                iznosUplate = ulazniTokOdKlijenta.readLine();
                                if (iznosUplate.startsWith("quit")) {
                                    break;
                                }
                                if (minimalnaUplata(iznosUplate)==false) {
                                    izlazniTokKaKlijentu.println("Minimalna uplata je 200 dinara. Pokusaj ponovo. Za izlaz unesi quit");
                                }
                            } while (minimalnaUplata(iznosUplate)==false);
                            if(postojiCVV(CVV)==true) { 
                            	ukupneUplate = Double.parseDouble(procitajUkupneUplate());
                            	ukupneUplate += Double.parseDouble(iznosUplate);
                            	upisiUFajl(Double.toString(ukupneUplate));
                            	String podaci = ime + " " + prezime + " " + adresa + " " + LocalDate.now() + " " + LocalTime.now() + " " + iznosUplate;
                            	dodajZapisOUplati(podaci);
                            	String podaci1 = generisiRacun(ime, prezime, adresa, LocalDate.now() ,  Double.parseDouble(iznosUplate));
                            	izdajFajlKorisniku(podaci1);
                            	izlazniTokKaKlijentu.println("HVALA NA DONACIJI!");
                                }
                               else {
                            	   izlazniTokKaKlijentu.println("Uplata neuspesna! Kartica i/li cvv ne postoje u bazi.");
                               }
                          }
                    } else if (opcijaBr == 2) { //Ukupna prikupljena sredstva
                    	 String ukupno = procitajUkupneUplate();
                    	 izlazniTokKaKlijentu.println("Ukupna prikupljena sredstva su: " + ukupno);
                 
                    } else if (opcijaBr == 3) { //Registracija i prijava
                        int broj;
                        izlazniTokKaKlijentu.println("Unesite broj opcije koju zelite da izaberete: 1. Registracija 2. Prijava");
                        broj = Integer.parseInt(ulazniTokOdKlijenta.readLine());
                        if (broj == 1) {
                        do {	
                        		izlazniTokKaKlijentu.println("Unesite ime:");
                        		ime = ulazniTokOdKlijenta.readLine();
                        		izlazniTokKaKlijentu.println("Unesite prezime");
                        		prezime = ulazniTokOdKlijenta.readLine();
                                izlazniTokKaKlijentu.println("Unesite username:");
                                username = ulazniTokOdKlijenta.readLine();
                                
                                if (postojiUsername(username) == true) {
                                    izlazniTokKaKlijentu.println("Username vec postoji. Pokusaj ponovo");
                                }
                            } while (postojiUsername(username) == true);
                        
                            izlazniTokKaKlijentu.println("Unesi password:");
                            password = ulazniTokOdKlijenta.readLine();
                            do {
                            izlazniTokKaKlijentu.println("Unesi jmbg:");
                            JMBG = ulazniTokOdKlijenta.readLine();
                            if(proveraJMBG(JMBG)==false) {
                            	izlazniTokKaKlijentu.println("Neispravno unet jmbg. Pokusaj ponovo.");
                            }
                            }while(proveraJMBG(JMBG)==false);
                            do {
                                izlazniTokKaKlijentu.println("Unesi broj platne kartice");
                                brojKartice = ulazniTokOdKlijenta.readLine();
                                if (IspravanFormatKartice(brojKartice)==false) {
                                    izlazniTokKaKlijentu.println("Neispravan format kartice. Pokusaj ponovo.");
                                }
                            } while (IspravanFormatKartice(brojKartice) != true);
                            
                            do {
                            izlazniTokKaKlijentu.println("Unesi e-mail:");
                            email = ulazniTokOdKlijenta.readLine();
                            if(proveraEmail(email)==false) {
                            	izlazniTokKaKlijentu.println("Neispravno unet email.Pokusaj ponovo.");                            }
                            
                            }while(proveraEmail(email)==false);
                            
                            String putanjaDoFajla1 = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\podaciOKorisnicima.txt";
                            upisiPodatkeOKorisniku(username, password, ime, prezime, JMBG, brojKartice, email, putanjaDoFajla1);
                            izlazniTokKaKlijentu.println("REGISTRACIJA USPESNA!");
                        }
                        if (broj == 2) {
                        	do {
                                izlazniTokKaKlijentu.println("Unesi username:");
                                username = ulazniTokOdKlijenta.readLine();
                                izlazniTokKaKlijentu.println("Unesi password:");
                                password = ulazniTokOdKlijenta.readLine();
                            
                            	if(prijaviSe(username, password)==true) {
                            		izlazniTokKaKlijentu.println("Prijava uspesna!");
                            		izlazniTokKaKlijentu.println();
                            		prijavljen=true;
                            	}
                            	else {
                            		izlazniTokKaKlijentu.println("Prijava nije uspela! Pogresno korisnicko ime ili sifra. Pokusajte ponovo");
                            		izlazniTokKaKlijentu.println();
                            	}
                            }while(prijaviSe(username, password)==false);
                        }
                    } else if (opcijaBr == 4) { //Poslednjih 10 uplata
                        if (prijavljen == true) {
                        	poslednjih10Uplata(izlazniTokKaKlijentu);
                        }
                        else {
                            izlazniTokKaKlijentu.println("Morate biti prijavljeni da biste videli poslednje uplate.");
                            izlazniTokKaKlijentu.println();
                        } 
                    }
                    else {
                    	izlazniTokKaKlijentu.println("Pogresan unos!Pokusajte ponovo.");
                    	izlazniTokKaKlijentu.println();
                    }
                } catch (NumberFormatException e) {
                    izlazniTokKaKlijentu.println("Pogresan unos. Molimo vas da unesete broj. Za izlaz unesite quit");
                    izlazniTokKaKlijentu.println();
                }catch(SocketException e) {
                	System.out.println("Kraj rada jednog klijenta. ");
                	break;
                }
            } while (true);
               System.out.println("Kraj rada jednog klijenta.");
        } catch (IOException e) {
            e.printStackTrace();
            izlazniTokKaKlijentu.println("IOException!");
            izlazniTokKaKlijentu.println();
        } finally {
            try {
                if (ulazniTokOdKlijenta != null) {
                    ulazniTokOdKlijenta.close();
                }
                if (izlazniTokKaKlijentu != null) {
                    izlazniTokKaKlijentu.close();
                }
                if (soketZaKomunikaciju != null) {
                    soketZaKomunikaciju.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean IspravanFormatKartice(String kartica) {
        if (kartica.length() == 19 && kartica.charAt(4) == '-' && kartica.charAt(9) == '-' && kartica.charAt(14) == '-') {
            String[] delovi = kartica.split("-");
            if (delovi.length == 4) {
                try {
                    for (String deo : delovi) {
                        Integer.parseInt(deo);
                    }
                    brojKartice = kartica;
                    return true;
                } catch (NumberFormatException e) {
                	e.printStackTrace();
                }
            }
        }
        return false;
    }
    public boolean trocifren(String brojString) {
    	try {
        int broj = Integer.parseInt(brojString);
        return broj>=100 && broj<=999;
        }
    	catch(NumberFormatException e) {
    		return false;
    	}
       
    }
    public boolean postojiUsername(String username) {
    	
    	String putanjaDoFajla = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\podaciOKorisnicima.txt"; 

         try (BufferedReader bufferedReader = new BufferedReader(new FileReader(putanjaDoFajla))) {
             String linija;
             while ((linija = bufferedReader.readLine()) != null) {
                 
                 String[] podaci = linija.split(" ");
                 String ime = podaci[0];
            
                 if (ime.equals(username)) {
                     return true;
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }

         return false;
     }
    public boolean prijaviSe(String username, String password) {
    	String putanjaDoFajla = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\podaciOKorisnicima.txt"; 
    	
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(putanjaDoFajla))) {
            String linija;
            while ((linija = bufferedReader.readLine()) != null) {
                
                String[] podaci = linija.split(" ");
                String ime = podaci[0];
                String lozinka = podaci[1];
                if (ime.equals(username) && lozinka.equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean minimalnaUplata(String uplataString) {
    	double uplata = Double.parseDouble(uplataString);
    	return uplata>=200;
    }
    public void upisiUFajl(String ukupneUplate) {
    	String putanjaDoFajla =  "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\ukupneUplate.txt";
    	
    	try {
            FileWriter fileWriter = new FileWriter(putanjaDoFajla, false);
            PrintWriter printWriter = new PrintWriter(fileWriter);

            printWriter.println(ukupneUplate);

            printWriter.close();
            fileWriter.close();

            System.out.println("Ukupne uplate uspesno upisane u fajl.");
        }catch (IOException e) {
            e.printStackTrace();
        }
}
    public String vratiPodatkeOUplati(String podaci) {
            return podaci;
}
    public void dodajZapisOUplati(String zapisOUplati) {
    	String putanjaDoFajla = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\spisakUplata.txt";

    	try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(putanjaDoFajla, true)))) {
    	    printWriter.println(zapisOUplati);
    	    System.out.println("Spisak uplata azuriran.");
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    }
    public static void izdajFajlKorisniku(String podaci) {
        String trenutnoVreme = Long.toString(System.currentTimeMillis());
        String nazivFajla = "uplata_" + trenutnoVreme + ".txt";
        String relativnaPutanja = "C:\\Users\\korisnk\\eclipse-workspace\\AKlijent\\" + nazivFajla;
        File fajl = new File(relativnaPutanja);

        try (PrintWriter printWriter = new PrintWriter(new FileWriter(fajl))) {
            printWriter.println(podaci);
            System.out.println("Fajl " + nazivFajla + " uspesno izdat korisniku.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String generisiRacun(String ime, String prezime, String adresa, LocalDate datum, double iznosUplate) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formatiraniDatum = dateFormat.format(datum);

        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String formatiraniIznosUplate = decimalFormat.format(iznosUplate);

        return "==================================\n" +
               "|         Racun za uplatu         |\n" +
               "==================================\n" +
               String.format("| Ime:          %-18s|\n", ime) +
               String.format("| Prezime:      %-18s|\n", prezime) +
               String.format("| Adresa:       %-18s|\n", adresa) +
               String.format("| Datum:        %-18s|\n", formatiraniDatum) +
               String.format("| Iznos uplate: %-18s|\n", formatiraniIznosUplate) +
               "==================================";
    }
    public String procitajUkupneUplate() {
        String putanjaDoFajla = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\ukupneUplate.txt";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(putanjaDoFajla))) {
            String linija = bufferedReader.readLine();
            if (linija != null) {
                return linija.trim();
            } else {
                return "0.0";
            }
        }
            catch (FileNotFoundException e) {
                System.out.println("Fajl nije pronadjen.");
                return "Nema fajla";
            }
        
        catch (IOException e) {
            e.printStackTrace();
            return "IOException";
        }
    }
    public boolean postojiUBazi(String kartica, String cvv) {
        String putanjaDoFajla = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\bazaKarticaCVV.txt"; 

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(putanjaDoFajla))) {
            String linija;
            while ((linija = bufferedReader.readLine()) != null) {
                
                String[] delovi = linija.split(" ");
                if (delovi.length == 2) {
                    String brojKartice = delovi[0];
                    String cvvIzFajla = delovi[1];
                    
                    if (brojKartice.equals(kartica) && cvvIzFajla.equals(cvv)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean postojiCVV(String cvv) {
        String putanjaDoFajla = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\bazaKarticaCVV.txt"; 

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(putanjaDoFajla))) {
            String linija;
            while ((linija = bufferedReader.readLine()) != null) {  
                String[] delovi = linija.split(" ");
                if (delovi.length == 2) {
                    String cvvIzFajla = delovi[1];
                    
                    if (cvvIzFajla.equals(cvv)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void upisiPodatkeOKorisniku(String username, String password, String ime, String prezime,
    		String jmbg, String kartica, String email, String putanjaDoFajla) {
    	
    	try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(putanjaDoFajla, true)))) {

    			printWriter.println(username + " " + password + " " + ime + " " + prezime + " " + jmbg + " " + kartica + " " + email);
    			System.out.println("Podaci o korisniku su uspesno upisani u fajl.");
    			
    	} catch (IOException e) {
    			e.printStackTrace();
    	}
    }
    public int izbroj() {
        String putanjaDoFajla = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\spisakUplata.txt";
        int brojac = 0;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(putanjaDoFajla))) {
            while (bufferedReader.readLine() != null) {
                brojac++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return brojac;
    }
    public void poslednjih10Uplata(PrintStream izlaz) {
    	 String putanjaDoFajla = "C:\\Users\\korisnk\\eclipse-workspace\\AaServer\\spisakUplata.txt";
         int brojac = izbroj();
         if(brojac==0) {
        	 izlaz.println("Nema uplata!");
        	 return;
         }
         try (BufferedReader bufferedReader = new BufferedReader(new FileReader(putanjaDoFajla))) {
             String linija;
             String[] sviRedovi = new String[brojac];
             int i = 0;
             while ((linija = bufferedReader.readLine()) != null) {
                 sviRedovi[i++] = linija;
             }

             int pocetak = Math.max(0, brojac - 10);
             for (i = brojac - 1; i >= pocetak; i--) {
                 izlaz.println(sviRedovi[i]);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
    }
    public boolean proveraJMBG(String jmbg) {
        try {
            Double.parseDouble(jmbg);
        } catch (NumberFormatException e) {
        	 return false;
        }
        if (jmbg.length() != 13) {
        	return false;
        }
        return true;
    }
    public static boolean proveraEmail(String email) {
        int atIndex = email.indexOf('@');
        int dotIndex = email.lastIndexOf('.');
        return atIndex > 0 && dotIndex > atIndex && dotIndex < email.length() - 1;
    }
}