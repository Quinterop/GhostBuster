import java.io.BufferedReader;
import java.io.DataOutputStream;
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
    private static DataOutputStream outB;
    private static int port;
    private static int maxReadUDP = 50;
    private static String portUdp = "5656";
    private static Scanner sc = new Scanner(System.in);
    
    
    public static void main(String[] args) {
        
        pseudo = args[0];
        port = Integer.parseInt(args[1]);
        connect();
        
        System.out.println("AVANT PARTIE");
        System.out.println("AFFICHAGE DES PARTIES");
        
        char[] first = recieveTCPMessage(10);
        int nbgames = first[6];
        System.out.println("nbgames"+nbgames);
        
        for (int i = 0; i < nbgames; i++) {
            
            char[] gamei = recieveTCPMessage(12);
            System.out.println("game"+gamei[6]+": "+gamei[8]+" joueurs");
        }
        System.out.println("1 : creer une partie");
        System.out.println("2 : rejoindre une partie");
        int choice = sc.nextInt();
        
        switch (choice){
            case 1:
            System.out.println("creation partie");
            
            
            String mess = "NEWPL " + pseudo + " " + portUdp + "***";
            sendTCPMessage(mess);
            System.out.println("Message envoyé : " + mess);
            
            char[] reg = (recieveTCPMessage(10));
            if(reg[3]=='N'){
                System.out.println("echec creer partie");
            }else{
                System.out.println("partie créee "+reg[6]);
            }
            break;
            case 2:
            System.out.println("incription a une partie");
            //sc.nextLine();
            System.out.println("choisir partie");
            // int numeropartie = sc.nextInt(); //mettre sur 1 octet
            Byte a = 1;
            int numeropartie = a & 0xFF;
            String message = ("REGIS "+pseudo+" "+portUdp+" "+"X"+"***");
            byte[] messageByte = message.getBytes();
            
            
            
            
            messageByte[20] = (byte) numeropartie;
            sendTCPMessage(messageByte);
            
            char[] reg2 = (recieveTCPMessage(10));
            if(reg2[3]=='N'){
                System.out.println("echec rejoindre partie");
            }else{
                System.out.println("enregistré dans la partie"+reg2[6]);
            }
            break;
        }
        System.out.println("salut les reufs");
        pregame();
    }
    
    
    public static void pregame(){
        //scan int
        int choice = sc.nextInt();
        sc.nextLine();
        
        switch(choice){
            
            case 4:
            System.out.println("quitter partie");
            sendTCPMessage("UNREG***");
            char[] prem = recieveTCPMessage(1);
            if(prem[0]=='D'){
                System.out.println("D"+new String(recieveTCPMessage(8)));
            }else{
                char[] ok = recieveTCPMessage(9);
                int partie = ok[5];
                System.out.println("ok quitter "+partie);
            }
            pregame();
            break;
            case 5:
            System.out.println("taille du laby d'une partie");
            System.out.println("quelle partie");
            int numero = sc.nextInt();
            sendTCPMessage("SIZE? "+numero+"***");
            /* String test;
            try {
                test = reqTCP();
                System.out.println(test);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            */
            char[] prem2 = (recieveTCPMessage(1));
            if(prem2[0]=='D'){
                System.out.println("D"+new String(recieveTCPMessage(8)));
            }else{
                char[] ok = recieveTCPMessage(15);
                for(int i = 0; i < ok.length; i++)
                {
                    System.out.println("char " + i + " = " + ok[i]);
                }
                int partie = ok[5];
                int i = ok[8] * 256 + ok[7];
                System.out.println("taille de la partie "+partie+" : hauteur"+ i);
            }
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
            sendTCPMessage("GAME?***");
            /*String first = recieveTCPMessage();
            int nbgames = Integer.valueOf(first);
            System.out.println(first);
            for (int i = 0; i < nbgames; i++) {
                System.out.println(recieveTCPMessage());
            }
            */ pregame();
            break;
            case 8:
            System.out.println("début de partie");
            sendTCPMessage("START***");
            
            //[WELCO␣m␣h␣w␣f␣ip␣port***]
            char[] start = recieveTCPMessage(24);
            String mess = new String(start);
            System.out.println(new String(mess));
            int partie = start[6];
            //LIRE HAUTEUR LARGEUR
            int nbFant = start[14]; //A TESTER
            String ip = mess.substring(13,28);
            String port = mess.substring(29,33);
            inGame();
            break;
            default:
            System.out.println("erreur");
            break;
        }
        sc.close();
        //System.out.println("la partie a commencé");
    }
    
    public static void inGame(){
        //POSIT␣id␣x␣y***
        char[] pos = recieveTCPMessage(25);
        String mess = new String(pos);
        System.out.println(mess);
        String id = mess.substring(6,14);
        int x = Integer.valueOf(mess.substring(15,18));
        int y = Integer.valueOf(mess.substring(19,22));
        System.out.println("id : "+id+"position : x : "+x+" y : "+y);
        //System.out.println("posit : "+posit);
        
        
        System.out.println("0 points");
        System.out.println("se déplacer ? HBGD-> 0123. 5 pour quitter");
        System.out.println("liste des joueurs -> 6. chat : privé -> 7. global ->8");
        Scanner sc = new Scanner(System.in);
        int choice = sc.nextInt();
        sc.nextLine();
        System.out.println("combien de cases ?");
        int nb = sc.nextInt();


        switch(choice){
            //MOUVEMENTS
            case 0:{
                System.out.println("mouvement haut de "+nb+" cases");
                String out = "UPMOV "+nb+"***";
                sendTCPMessage(out);
                
                //MOVEF␣x␣y␣p***
                char[] prem = recieveTCPMessage(5);
                if(prem[4]=='E'){
                    char[] suite = recieveTCPMessage(3);
                    String full = new String(prem)+new String(suite);
                    System.out.println(full);
                    System.out.println("partie terminée");
                    return;
                }
                else if(prem[4]=='F'){
                    System.out.println("fantome attrapé");
                    char[] suite = recieveTCPMessage(15);
                    String full = new String(prem)+new String(suite);
                    
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    int p = Integer.valueOf(full.substring(14,17));
                    System.out.println(full);
                    System.out.println("fantome attrapé points : "+p+"position : x : "+x2+" y : "+y2);
                    
                }else {
                    //MOVE!␣x␣y***
                    char[] suite = recieveTCPMessage(11);
                    String full = new String(prem)+new String(suite);
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    System.out.println(full);
                    System.out.println("position : x : "+x2+" y : "+y2);
                    
                }
            }
            case 1:{
                System.out.println("mouvement bas de "+nb+" cases");
                String out = "DOMOV "+nb+"***";
                sendTCPMessage(out);
                
                //MOVEF␣x␣y␣p***
                char[] prem = recieveTCPMessage(5);

                if(prem[4]=='E'){
                    char[] suite = recieveTCPMessage(3);
                    String full = new String(prem)+new String(suite);
                    System.out.println(full);
                    System.out.println("partie terminée");
                    return;
                }
                else if(prem[4]=='F'){
                    System.out.println("fantome attrapé");
                    char[] suite = recieveTCPMessage(15);
                    String full = new String(prem)+new String(suite);
                    
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    int p = Integer.valueOf(full.substring(14,17));
                    System.out.println(full);
                    System.out.println("fantome attrapé points : "+p+"position : x : "+x2+" y : "+y2);
                    
                }else {
                    //MOVE!␣x␣y***
                    char[] suite = recieveTCPMessage(11);
                    String full = new String(prem)+new String(suite);
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    System.out.println(full);
                    System.out.println("position : x : "+x2+" y : "+y2);
                    
                }
            }
            case 2:{
                System.out.println("mouvement gauche de "+nb+" cases");
                String out = "LEMOV "+nb+"***";
                sendTCPMessage(out);
                
                //MOVEF␣x␣y␣p***
                char[] prem = recieveTCPMessage(5);

                if(prem[4]=='E'){
                    char[] suite = recieveTCPMessage(3);
                    String full = new String(prem)+new String(suite);
                    System.out.println(full);
                    System.out.println("partie terminée");
                    return;
                }
                else if(prem[4]=='F'){
                    System.out.println("fantome attrapé");
                    char[] suite = recieveTCPMessage(15);
                    String full = new String(prem)+new String(suite);
                    
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    int p = Integer.valueOf(full.substring(14,17));
                    System.out.println(full);
                    System.out.println("fantome attrapé points : "+p+"position : x : "+x2+" y : "+y2);
                    
                }else {
                    //MOVE!␣x␣y***
                    char[] suite = recieveTCPMessage(11);
                    String full = new String(prem)+new String(suite);
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    System.out.println(full);
                    System.out.println("position : x : "+x2+" y : "+y2);
                    
                }
            }
            case 3:{
                System.out.println("mouvement droite de "+nb+" cases");
                String out = "RIMOV "+nb+"***";
                sendTCPMessage(out);
                
                //MOVEF␣x␣y␣p***
                char[] prem = recieveTCPMessage(5);
                if(prem[4]=='E'){
                    char[] suite = recieveTCPMessage(3);
                    String full = new String(prem)+new String(suite);
                    System.out.println(full);
                    System.out.println("partie terminée");
                    return;
                }
                else if(prem[4]=='F'){
                    System.out.println("fantome attrapé");
                    char[] suite = recieveTCPMessage(15);
                    String full = new String(prem)+new String(suite);
                    
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    int p = Integer.valueOf(full.substring(14,17));
                    System.out.println(full);
                    System.out.println("fantome attrapé points : "+p+"position : x : "+x2+" y : "+y2);
                    
                }else {
                    //MOVE!␣x␣y***
                    char[] suite = recieveTCPMessage(11);
                    String full = new String(prem)+new String(suite);
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    System.out.println(full);
                    System.out.println("position : x : "+x2+" y : "+y2);
                    
                }
            }


            case 5:{
                System.out.println("quitter partie");
                sendTCPMessage("IQUIT***");
                char[] prem = recieveTCPMessage(8);
                System.out.println(new String(prem));
                return;
            }
            case 6:{
                System.out.println("liste des joueurs");
                sendTCPMessage("GLIS?***");
                char[] first = recieveTCPMessage(10);

                if(first[4]=='E'){
                    System.out.println(first);
                    System.out.println("partie terminée");
                    return;
                }
                 
                int nbplayers = first[7];
                System.out.println(new String(first));
                
                for (int i = 0; i < nbplayers; i++) {
                    //GPLYR␣id␣x␣y␣p***
                    char[] player = recieveTCPMessage(29);
                    String mess2 = new String(player);
                    String id2 = mess2.substring(6,14);
                    int x2 = Integer.valueOf(mess2.substring(15,18));
                    int y2 = Integer.valueOf(mess2.substring(19,22));
                    int p = Integer.valueOf(mess2.substring(23,26));
                    System.out.println(mess2);
                    System.out.println("Joueur "+id2+"position : x : "+x2+" y : "+y2+ " points : "+p);
                }
                
                System.out.println("ok liste des joueurs");
                
            }
            case 7:{
                System.out.println("chat privé");
                System.out.println("qui voulez vous envoyer un message ?");
                String dest = sc.nextLine();
                System.out.println("message ?");
                String msg = sc.nextLine();
                
            }
            case 8:{
                System.out.println("chat global");
                System.out.println("message ?");
                String msg = sc.nextLine();
            }
        }
        inGame();
        
    }
    
    public static void connect() {
        try {
            // Create a new socket
            socket = new Socket("127.0.0.1", port);
            // Create a new input stream
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Create a new output stream
            out = new PrintWriter(socket.getOutputStream(), true);
            outB = new DataOutputStream(socket.getOutputStream());
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
    public static char[] recieveTCPMessage(int size) {
        try {
            //read from the input stream
            char[] buffer = new char[size];
            System.out.println("caracteres lus" + in.read(buffer,0,size));
            
            for(int i = 0; i < size; i++) System.out.print(buffer[i]);
            
            //convert the buffer to a string
            // String line = new String(buffer);
            // Return the line
            //return line;
            return buffer;
        } catch (IOException e) {
            System.err.println("Could not read from the input stream.");
            System.exit(1);
        }
        return null;
    }
    
    
    public static void sendTCPMessage(String message) {
        out.print(message);
        out.flush();
    }
    
    public static void sendTCPMessage(byte[] message) {
        try {
            outB.write(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
    
    public static String reqTCP() throws IOException{
        String message = "";
        char tmp;
        boolean isEndRequest = false;
        int compteur=0;
        System.out.println("entrez votre message");
        while(!isEndRequest) {
            System.out.println("test read");
            tmp=(char)in.read();
            System.out.println("lu: "+tmp);
            System.out.println("int: " + (int) tmp);
            message+=tmp;
            if(tmp=='*') {
                compteur++;
            }
            if(compteur==3) {
                isEndRequest=true;
            }
        }
        System.out.println("Message reçu: "+message);
        return message;
    }
}

