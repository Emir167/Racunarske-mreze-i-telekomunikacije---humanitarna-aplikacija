package klijent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;

public class Klijent implements Runnable {

    private BufferedReader ulazniTokOdServera;
    private Socket soketZaKomunikaciju;

    public Klijent(Socket soket, BufferedReader ulazniTokOdServera) {
        this.soketZaKomunikaciju = soket;
        this.ulazniTokOdServera = ulazniTokOdServera;
    }
   
    public static void main(String[] args) {
        Socket soketZaKomunikaciju = null;
        PrintStream izlazniTokKaServeru = null;
        BufferedReader ulazniTokOdServera = null;
        BufferedReader ulazKonzola = null;
        boolean kraj = false;

        try {
            int port = 5000;

            soketZaKomunikaciju = new Socket("localhost", port);
            ulazKonzola = new BufferedReader(new InputStreamReader(System.in));
            izlazniTokKaServeru = new PrintStream(soketZaKomunikaciju.getOutputStream());
            ulazniTokOdServera = new BufferedReader(new InputStreamReader(soketZaKomunikaciju.getInputStream()));

            new Thread(new Klijent(soketZaKomunikaciju, ulazniTokOdServera)).start();

            String unos;
		
			  while (!kraj && (unos = ulazKonzola.readLine()) != null) {
				  izlazniTokKaServeru.println(unos);
				  if (unos.equals("quit")) { kraj = true; } }
			 
        	} catch (IOException e) {
            e.printStackTrace();
        	} finally {
        	try {
                if (soketZaKomunikaciju != null) {
                    soketZaKomunikaciju.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void run() {
        try {
            String odgovorOdServera;
            while ((odgovorOdServera = ulazniTokOdServera.readLine()) != null) {
                System.out.println(odgovorOdServera);
                if (odgovorOdServera.startsWith("quit")) {
                    break;
                }
            }
        } catch (SocketException e) {
            System.out.println("***KRAJ***");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                soketZaKomunikaciju.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}