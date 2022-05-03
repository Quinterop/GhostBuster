import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    static String pseudo; //8 caractères max
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static int port;
    private static int maxReadTCP = 50;
    private static int maxReadUDP = 50;
    
    public static void main(String[] args) {
        connect();
        pseudo = args[0];
        port = Integer.parseInt(args[1]);
        //avant la game 
        System.out.println("AVANT PARTIE");
        
        //int ip = sc.nextInt();
        //AFFICHER PARTIES
        Scanner sc = new Scanner(System.in);
        System.out.println("AFFICHAGE DES PARTIES");
        String first = recieveTCPMessage();
        System.out.println(first);
        int nbgames = Integer.parseInt(first);
        
        for (int i = 0; i < nbgames; i++) {
            System.out.println(recieveTCPMessage());
        }
        System.out.println("2 : creer une partie");
        System.out.println("3 : rejoindre une partie");
        int choice = sc.nextInt();
        
        switch (choice){
            case 1:
                System.out.println("creation partie");
                System.out.println("choisir port");
                int newport = sc.nextInt();
                sendTCPMessage("NEWPL "+pseudo+" "+newport+"***");
                System.out.println(recieveTCPMessage());
            break;
            case 2:
                System.out.println("incription a une partie");
                System.out.println("choisir port");
                int newport2 = sc.nextInt();
                int numeropartie = sc.nextInt(); //mettre sur 1 octet
                sendTCPMessage("REGIS "+pseudo+" "+newport2+" "+numeropartie+"***");
                System.out.println(recieveTCPMessage());
            break;
        }
        sc.close();
        pregame();
    }
    
    
    public static void pregame(){
        Scanner sc = new Scanner(System.in);
        int choice = sc.nextInt();
        
        switch(choice){
            
            case 4:
            System.out.println("quitter partie");
            sendTCPMessage("UNREG***");
            System.out.println(recieveTCPMessage());
            pregame();
            break;
            case 5:
            System.out.println("taille du laby d'une partie");
            System.out.println("quelle partie");
            int numero = sc.nextInt();
            sendTCPMessage("SIZE? "+numero+"***");
            System.out.println(recieveTCPMessage());
            pregame();
            break;
            case 6:
            System.out.println("liste de joueurs");
            System.out.println("quelle partie");
            int numero2 = sc.nextInt();
            sendTCPMessage("LIST? "+numero2+"***");
            pregame();
            break;
            case 7:
            System.out.println("liste des parties nn vides");
            sendTCPMessage("GAME? ***");
            String first = recieveTCPMessage();
            int nbgames = Integer.valueOf(first);
            System.out.println(first);
            for (int i = 0; i < nbgames; i++) {
                System.out.println(recieveTCPMessage());
            }
            pregame();
            break;
            case 8:
            System.out.println("début de partie");
            sendTCPMessage("START***");
            //bloque
            break;
            default:
            System.out.println("erreur");
            break;
        }
        sc.close();
        //System.out.println("la partie a commencé");
    }
    
    public static void connect() {
        try {
            // Create a new socket
            socket = new Socket("localhost", port);
            // Create a new input stream
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Create a new output stream
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error: " + e);
            System.err.println("Could not connect to the server.");
            System.exit(1);
        }
    }
    
    public void disconnect() {
        try {
            // Close the input stream
            in.close();
            // Close the output stream
            out.close();
            // Close the socket
            socket.close();
        } catch (IOException e) {
            System.err.println("Could not disconnect from the server.");
            System.exit(1);
        }
    }
    
       //recieve tcp message
    public static String recieveTCPMessage() {
        try {
            //read from the input stream
            char[] buffer = new char[maxReadTCP];
            System.out.println("caracteres lus" + in.read(buffer,0,maxReadTCP));
            //convert the buffer to a string
            String line = new String(buffer);
            // Return the line
            return line;
        } catch (IOException e) {
            System.err.println("Could not read from the input stream.");
            System.exit(1);
        }
        return null;
    }


    public static void sendTCPMessage(String message) {
        out.println(message);
    }
    
    public static String recieveUdpMessage() {
        try{
            DatagramSocket dso=new DatagramSocket(port);
            byte[]data=new byte[maxReadUDP];
            DatagramPacket paquet=new DatagramPacket(data,data.length);
            dso.receive(paquet);
            String st=new String(paquet.getData(),0,paquet.getLength());
            dso.close();
            return st;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

 



    //find int in string after char
    public static int findInt(String str, char c) {
        int i = str.indexOf(c);
        int intsize = 0;
        for(int j = i; j<str.length(); i++){
            if(!charIsInt(str.charAt(i))){
                intsize=i-j;
                break;
            }
        }
        return Integer.parseInt(str.substring(i, i+intsize));
    }

    public static boolean charIsInt(char c){
        try {
            Integer.parseInt(String.valueOf(c));
            return true;
        } catch(NumberFormatException nfe) {
            return false;
        }
    }
}

