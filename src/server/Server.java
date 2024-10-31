package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

	public class Server {
	    static ArrayList<ServerNit> korisnici = new ArrayList<>();
	    static ServerSocket serverSoket;

	    public static void main(String[] args) {
	        int port = 5000;
	        Socket klijentSoket = null;

	        try {
	            serverSoket = new ServerSocket(port);
	            while (true) {
	            	System.out.println("Cekam na vezu...");
	                klijentSoket = serverSoket.accept();
	                System.out.println("Veza uspostavljena!");

	                ServerNit klijentNit = new ServerNit(klijentSoket, korisnici);
	                korisnici.add(klijentNit);
	                klijentNit.start();
	            }
	        }
	        catch(BindException e) {
	        	System.out.println("Server vec pokrenut.");
	        }
	        catch (IOException e) {
	            System.out.println("Jedan od klijenata je zavrsio sa radom.");
	        } finally {
	            try {
	                if (serverSoket != null && !serverSoket.isClosed()) {
	                    serverSoket.close();
	                }
	            }catch(Exception e) {
	            	e.printStackTrace();
	            }
	         }
	    }
	}