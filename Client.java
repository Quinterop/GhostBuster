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
    private static String portUdp;
    private static Scanner sc = new Scanner(System.in);
    private static Communication communication;
    private static CommMulticast commMulticast;
    private static Thread t = new Thread(communication);
    private static Thread t2 = new Thread(commMulticast);
    private static boolean isOver = false;
    
    
    public static void main(String[] args) {
        
        pseudo = args[0];
        port = Integer.parseInt(args[1]);
        connect();
        launcher();
        

    }
    
    
    /*public static void pregame(){
        System.out.print(
            "Sélectionnez un choix :\n" +
            "4/ Quitter la partie en cours\n" +
            "5/ Demander la taille d'un lobby\n" +
            "6/ Lister les joueurs d'un lobby\n" +
            "7/ Lister les lobbys rejoignables\n" +
            "8 Commencer la partie\n" +
            "Votre choix : ");
        int choice = sc.nextInt();
        sc.nextLine();
        
        switch(choice){
            case 4:
                System.out.println("quitter partie");
                sendTCPMessage("UNREG***");
                byte[] prem = receiveTCPMessage(1);
                if(prem[0]=='D'){
                    System.out.println("D"+new String(receiveTCPMessage(8)));
                }else{
                    byte[] ok = receiveTCPMessage(9);
                    int partie = ok[5];
                    System.out.println("ok quitter "+partie);
                }
                pregame();
                break;
            case 5:
                System.out.print("Sélectionnez le numéro de la partie : ");
                int user_m = sc.nextInt();
                sendTCPMessage("SIZE? " + (char) user_m + "***");

                String requete = new String(receiveTCPMessage(6));
                if(requete.equals("DUNNO ")) 
                {
                    System.out.println("La partie demandée n'existe pas.");
                    pregame();
                }
                else if(!requete.equals("SIZE! "))
                {
                    System.out.println("Requête reçue inattendue.");
                    pregame();
                }

                // Réponse attendue : [SIZE!_m_h_w***]
                byte[] m = receiveTCPMessage(1); // m
                receiveTCPMessage(1); // _
                byte[] h = receiveTCPMessage(2); // h
                receiveTCPMessage(1); // _
                byte[] w = receiveTCPMessage(2); // w
                receiveTCPMessage(3); // ***
                int test2 = (w[0] & 0xff) + (w[1] & 0xff) * 0x100;
                System.out.println(test2);
                System.out.println("La partie " + (int) m[0] + " a pour hauteur " + (int) ((h[0] & 0xff) + (h[1] & 0xff) * 0x100) + " cases et pour largeur " + (int) ((w[0] & 0xff) + (w[1] & 0xff) * 0x100) + " cases.");
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
            /*String first = receiveTCPMessage();
            int nbgames = Integer.valueOf(first);
            System.out.println(first);
            for (int i = 0; i < nbgames; i++) {
                System.out.println(receiveTCPMessage());
            }
             pregame();
            break;
            case 8:
            System.out.println("début de partie");
            sendTCPMessage("START***");
            
            //[WELCO␣m␣h␣w␣f␣ip␣port***]
            /*byte[] start = receiveTCPMessage(24);
            String mess = new String(start);
            System.out.println(new String(mess));
            int partie = start[6];
            //LIRE HAUTEUR LARGEUR
            int hauteur = (int) ((start[8] & 0xff) + (start[9] & 0xff) * 0x100);
            int largeur = (int) ((start[11] & 0xff) + (start[12] & 0xff) * 0x100);
            int nbFant = start[14]; //A TESTER
            String ip = mess.substring(13,28);
            String portString = mess.substring(29,33);
            int port=Integer.parseInt(portString);
            String[] start=welc();
            String ip=start[0];
            System.out.println("adresse multicast: "+ip);
            int port=Integer.parseInt(start[1]);
            System.out.println("port multicast: "+port);
            communication=new Communication(Integer.parseInt(portUdp));
            commMulticast=new CommMulticast(ip,port);
            Thread t = new Thread(communication);
            Thread t2 = new Thread(commMulticast);
            t.start();
            t2.start();
            //inGame();
            enJeu();
            break;
            default:
            System.out.println("erreur");
            break;
        }
        sc.close();
        //System.out.println("la partie a commencé");
    }*/
    
    /*public static void inGame(){
        //POSIT␣id␣x␣y***
        byte[] pos = receiveTCPMessage(25);
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
                byte[] prem = receiveTCPMessage(5);
                if(prem[4]=='E'){
                    byte[] suite = receiveTCPMessage(3);
                    String full = new String(prem)+new String(suite);
                    System.out.println(full);
                    System.out.println("partie terminée");
                    return;
                }
                else if(prem[4]=='F'){
                    System.out.println("fantome attrapé");
                    byte[] suite = receiveTCPMessage(15);
                    String full = new String(prem)+new String(suite);
                    
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    int p = Integer.valueOf(full.substring(14,17));
                    System.out.println(full);
                    System.out.println("fantome attrapé points : "+p+"position : x : "+x2+" y : "+y2);
                    
                }else {
                    //MOVE!␣x␣y***
                    byte[] suite = receiveTCPMessage(11);
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
                byte[] prem = receiveTCPMessage(5);

                if(prem[4]=='E'){
                    byte[] suite = receiveTCPMessage(3);
                    String full = new String(prem)+new String(suite);
                    System.out.println(full);
                    System.out.println("partie terminée");
                    return;
                }
                else if(prem[4]=='F'){
                    System.out.println("fantome attrapé");
                    byte[] suite = receiveTCPMessage(15);
                    String full = new String(prem)+new String(suite);
                    
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    int p = Integer.valueOf(full.substring(14,17));
                    System.out.println(full);
                    System.out.println("fantome attrapé points : "+p+"position : x : "+x2+" y : "+y2);
                    
                }else {
                    //MOVE!␣x␣y***
                    byte[] suite = receiveTCPMessage(11);
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
                byte[] prem = receiveTCPMessage(5);

                if(prem[4]=='E'){
                    byte[] suite = receiveTCPMessage(3);
                    String full = new String(prem)+new String(suite);
                    System.out.println(full);
                    System.out.println("partie terminée");
                    return;
                }
                else if(prem[4]=='F'){
                    System.out.println("fantome attrapé");
                    byte[] suite = receiveTCPMessage(15);
                    String full = new String(prem)+new String(suite);
                    
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    int p = Integer.valueOf(full.substring(14,17));
                    System.out.println(full);
                    System.out.println("fantome attrapé points : "+p+"position : x : "+x2+" y : "+y2);
                    
                }else {
                    //MOVE!␣x␣y***
                    byte[] suite = receiveTCPMessage(11);
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
                byte[] prem = receiveTCPMessage(5);
                if(prem[4]=='E'){
                    byte[] suite = receiveTCPMessage(3);
                    String full = new String(prem)+new String(suite);
                    System.out.println(full);
                    System.out.println("partie terminée");
                    return;
                }
                else if(prem[4]=='F'){
                    System.out.println("fantome attrapé");
                    byte[] suite = receiveTCPMessage(15);
                    String full = new String(prem)+new String(suite);
                    
                    int x2 = Integer.valueOf(full.substring(6,9));
                    int y2 = Integer.valueOf(full.substring(10,13));
                    int p = Integer.valueOf(full.substring(14,17));
                    System.out.println(full);
                    System.out.println("fantome attrapé points : "+p+"position : x : "+x2+" y : "+y2);
                    
                }else {
                    //MOVE!␣x␣y***
                    byte[] suite = receiveTCPMessage(11);
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
                byte[] prem = receiveTCPMessage(8);
                System.out.println(new String(prem));
                return;
            }
            case 6:{
                System.out.println("liste des joueurs");
                sendTCPMessage("GLIS?***");
                byte[] first = receiveTCPMessage(10);

                if(first[4]=='E'){
                    System.out.println(first);
                    System.out.println("partie terminée");
                    return;
                }
                 
                int nbplayers = first[7];
                System.out.println(new String(first));
                
                for (int i = 0; i < nbplayers; i++) {
                    //GPLYR␣id␣x␣y␣p***
                    byte[] player = receiveTCPMessage(29);
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
                System.out.println("a qui voulez vous envoyer un message ?");
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
        
    }*/
    

    public static void launcher(){
        boolean ok = false;
        System.out.println("AVANT PARTIE");
        System.out.println("AFFICHAGE DES PARTIES");
        
        byte[] first = receiveTCPMessage(10);
        int nbgames = first[6];
        System.out.println("nbgames"+nbgames);
        
        for (int i = 0; i < nbgames; i++) {
            
            byte[] gamei = receiveTCPMessage(12);
            System.out.println("game"+gamei[6]+": "+gamei[8]+" joueurs");
        }
        while(!ok){
            try {
                System.out.println("1 : creer une partie");
                System.out.println("2 : rejoindre une partie");
                int choice = sc.nextInt();
                sc.nextLine();
                switch (choice){
                    case 1:
                        System.out.println("Veuillez entrer un port UDP");
                        portUdp = sc.nextLine();
                        if(Integer.parseInt(portUdp)<1024 || Integer.parseInt(portUdp)>8191){
                            System.out.println("port incorrect");
                            break;
                        }
                        System.out.println("creation partie"); 
                        String mess = "NEWPL " + pseudo + " " + portUdp + "***";
                        sendTCPMessage(mess);
                        System.out.println("Message envoyé : " + mess);

                        byte[] reg = (receiveTCPMessage(10));
                        if(reg[3]=='N'){
                            System.out.println("echec creer partie");
                        }else{
                            ok=true;
                            System.out.println("partie créee "+reg[6]);
                        }
                    break;
                    case 2:
                        System.out.println("Veuillez entrer un port UDP");
                        portUdp = sc.nextLine();
                        if(Integer.parseInt(portUdp)<1024 || Integer.parseInt(portUdp)>8191){
                            System.out.println("port incorrect");
                            break;
                        }
                        System.out.println("incription a une partie");
                        System.out.println("choisir partie");
                        int n = sc.nextInt(); //mettre sur 1 octet
                        byte numeropartie = (byte) (n & 0xFF);
                        String message = ("REGIS "+pseudo+" "+portUdp+" ");
                        //byte[] messageByte = message.getBytes();
                        //messageByte[20] = (byte) numeropartie;
                        sendTCPMessage(message);
                        sendTCPMessage(new byte[]{numeropartie});
                        sendTCPMessage("***");

                        byte[] reg2 = (receiveTCPMessage(10));
                        if(reg2[3]=='N'){
                            System.out.println("echec rejoindre partie");
                        }else{
                            ok=true;
                            System.out.println("enregistré dans la partie"+reg2[6]);
                        }
                    break;
                    default:
                        System.out.println("Veuillez suivre les instructions");
                    break;
                }  
            } catch (Exception e) {
                System.out.println("Veuillez suivre les instructions");
                continue;
            }
        }
        avantPartie();
    }

    public static void avantPartie(){
        boolean isOk = false;
        System.out.println("AVANT PARTIE");
        while(!isOk){
            System.out.print(
                "Sélectionnez un choix :\n" +
                "1/ Quitter la partie en cours\n" +
                "2/ Demander la taille d'un lobby\n" +
                "3/ Lister les joueurs d'un lobby\n" +
                "4/ Lister les lobbys rejoignables\n" +
                "5/ Commencer la partie\n" +
            "Votre choix : ");
            int choice = sc.nextInt();
            sc.nextLine();
            try {
                switch(choice){
                case 1:
                    System.out.println("quitter partie");
                    sendTCPMessage("UNREG***");
                    byte[] prem = receiveTCPMessage(1);
                    if(prem[0]=='D'){
                        System.out.println("D"+new String(receiveTCPMessage(8)));
                    }else{
                        byte[] ok = receiveTCPMessage(9);
                        int partie = ok[5];
                        System.out.println("ok quitter "+partie);
                        isOk=true;
                        launcher();
                    }
                    break;
                case 2:
                    System.out.print("Sélectionnez le numéro de la partie : ");
                    int user_m = sc.nextInt();
                    sendTCPMessage("SIZE? " + (char) user_m + "***");

                    String requete = new String(receiveTCPMessage(6));
                    if(requete.equals("DUNNO ")) 
                    {
                        System.out.println("La partie demandée n'existe pas.");
                        break;
                    }
                    else if(!requete.equals("SIZE! "))
                    {
                        System.out.println("Requête reçue inattendue.");
                        break;
                    }

                    // Réponse attendue : [SIZE!_m_h_w***]
                    byte[] m = receiveTCPMessage(1); // m
                    receiveTCPMessage(1); // _
                    byte[] h = receiveTCPMessage(2); // h
                    receiveTCPMessage(1); // _
                    byte[] w = receiveTCPMessage(2); // w
                    receiveTCPMessage(3); // ***
                    int test2 = (w[0] & 0xff) + (w[1] & 0xff) * 0x100;
                    System.out.println(test2);
                    System.out.println("La partie " + (int) m[0] + " a pour hauteur " + (int) ((h[0] & 0xff) + (h[1] & 0xff) * 0x100) + " cases et pour largeur " + (int) ((w[0] & 0xff) + (w[1] & 0xff) * 0x100) + " cases.");
                    
                break;
                case 3:
                    System.out.println("liste de joueurs");
                    System.out.println("quelle partie");
                    int numero2 = sc.nextInt();
                    byte b = (byte) (numero2 & 0xff);
                    //System.out.println(Integer.toBinaryString(numero2));
                    sc.nextLine();
                    sendTCPMessage("LIST? ");
                    sendTCPMessage(new byte[]{ b });
                    sendTCPMessage("***");

                break;
                case 4:
                    System.out.println("liste des parties non vides");
                    sendTCPMessage("GAME?***");
                    /*String first = receiveTCPMessage();
                    int nbgames = Integer.valueOf(first);
                    System.out.println(first);
                    for (int i = 0; i < nbgames; i++) {
                        System.out.println(receiveTCPMessage());
                    }
                    */
                    break;
                case 5:
                    System.out.println("début de partie");
                    sendTCPMessage("START***");
                    String[] start = welc();
                    String ip = start[0];
                    System.out.println("adresse multicast: "+ip);
                    int port = Integer.parseInt(start[1]);
                    System.out.println("port multicast: "+port);
                    communication = new Communication(Integer.parseInt(portUdp));
                    commMulticast = new CommMulticast(ip,port);
                    t = new Thread(communication);
                    t2 = new Thread(commMulticast);
                    t.start();
                    t2.start();
                    isOk = true;
                    enJeu();
                    if(isOver){
                        System.out.println("partie terminée");
                        return;
                    }
                break;
                default:
                    System.out.println("Veuillez suivre les instructions");
                break;
            }
            } catch (Exception e) {
                System.out.println("Veuillez suivre les instructions");
                continue;
            }
            
        }
    }

    public static void enJeu(){
        //POSIT␣id␣x␣y***
        byte[] pos = receiveTCPMessage(25);
        String mess = new String(pos);
        System.out.println(mess);
        String id = mess.substring(6,14);
        int x = Integer.valueOf(mess.substring(15,18));
        int y = Integer.valueOf(mess.substring(19,22));
        System.out.println("id : "+id+" position : x="+x+" y="+y);
        boolean fin = false;
        while(!fin){
            commMulticast.affiche=true;
            communication.affiche=true;
            System.out.println("choisissez une action");
            System.out.println("se déplacer: Haut/Bas/Gauche/Droite -> 0/1/2/3");
            System.out.println("quitter la partie: 4");
            System.out.println("liste des joueurs: 5");
            System.out.println("chat: privé/général -> 6/7");
            int choice = sc.nextInt();
            sc.nextLine();

            switch(choice){
                //MOUVEMENTS
                case 0:{
                    System.out.println("combien de cases ?");
                    int nb = sc.nextInt();
                    sc.nextLine();
                    if(nb > 999 || nb < 1){
                        System.out.println("Veuillez choisir un nombre entre 1 et 999");
                        break;
                    }
                    String depl;
                    System.out.println("mouvement haut de "+nb+" cases");
                    if(nb<100){
                        if(nb<10){
                            depl="00"+nb;
                        }else{
                            depl="0"+nb;
                        }
                    }else{
                        depl=""+nb;
                    }
                    String out = "UPMOV "+depl+"***";
                    sendTCPMessage(out);
                    
                    //MOVEF␣x␣y␣p***
                    byte[] prem = receiveTCPMessage(5);
                    String rec=new String(prem);
                    System.out.println(rec);
                    if(rec.equals("MOVEF")){
                        System.out.println("fantome attrapé");
                        byte[] suite = receiveTCPMessage(16);
                        String full = new String(prem)+new String(suite);
                        System.out.println(full);
                        
                        String x2 = full.substring(6,9);
                        String y2 = full.substring(10,13);
                        String p = full.substring(14,17);
                        System.out.println("Vous avez attrapé un fantôme!\nVotre score est maintenant de: "+p+"\nEt votre position est: x = "+x2+" y = "+y2);
                        
                    }else {
                        //MOVE!␣x␣y***
                        byte[] suite = receiveTCPMessage(11);
                        String full = new String(prem)+new String(suite);
                        String x2 = full.substring(6,9);
                        String y2 = full.substring(10,13);
                        System.out.println(full);
                        System.out.println("Vous êtes maintenant en position : x = "+x2+" y = "+y2);
                        
                    }
                break;
                }
                case 1:{
                    System.out.println("combien de cases ?");
                    int nb = sc.nextInt();
                    sc.nextLine();
                    if(nb > 999 || nb < 1){
                        System.out.println("Veuillez choisir un nombre entre 1 et 999");
                        break;
                    }
                    String depl;
                    System.out.println("mouvement bas de "+nb+" cases");
                    if(nb<100){
                        if(nb<10){
                            depl="00"+nb;
                        }else{
                            depl="0"+nb;
                        }
                    }else{
                        depl=""+nb;
                    }
                    String out = "DOMOV "+depl+"***";
                    sendTCPMessage(out);

                    //MOVEF␣x␣y␣p***
                    byte[] prem = receiveTCPMessage(5);
                    String rec=new String(prem);
                    System.out.println(rec);
                    if(rec.equals("MOVEF")){
                        System.out.println("fantome attrapé");
                        byte[] suite = receiveTCPMessage(16);
                        String full = new String(prem)+new String(suite);
                        System.out.println(full);
                        
                        String x2 = full.substring(6,9);
                        String y2 = full.substring(10,13);
                        String p = full.substring(14,17);
                        System.out.println("Vous avez attrapé un fantôme!\nVotre score est maintenant de: "+p+"\nEt votre position est: x = "+x2+" y = "+y2);
                        
                    }else {
                        //MOVE!␣x␣y***
                        byte[] suite = receiveTCPMessage(11);
                        String full = new String(prem)+new String(suite);
                        String x2 = full.substring(6,9);
                        String y2 = full.substring(10,13);
                        System.out.println(full);
                        System.out.println("Vous êtes maintenant en position : x = "+x2+" y = "+y2);
                        
                    }
                break;
                }
                case 2:{
                    System.out.println("combien de cases ?");
                    int nb = sc.nextInt();
                    sc.nextLine();
                    if(nb > 999 || nb < 1){
                        System.out.println("Veuillez choisir un nombre entre 1 et 999");
                        break;
                    }
                    String depl;
                    System.out.println("mouvement gauche de "+nb+" cases");
                    if(nb<100){
                        if(nb<10){
                            depl="00"+nb;
                        }else{
                            depl="0"+nb;
                        }
                    }else{
                        depl=""+nb;
                    }
                    String out = "LEMOV "+depl+"***";
                    sendTCPMessage(out);
                    
                    //MOVEF␣x␣y␣p***
                    byte[] prem = receiveTCPMessage(5);
                    String rec=new String(prem);
                    System.out.println(rec);
                    if(rec.equals("MOVEF")){
                        System.out.println("fantome attrapé");
                        byte[] suite = receiveTCPMessage(16);
                        String full = new String(prem)+new String(suite);
                        System.out.println(full);
                        
                        String x2 = full.substring(6,9);
                        String y2 = full.substring(10,13);
                        String p = full.substring(14,17);
                        System.out.println("Vous avez attrapé un fantôme!\nVotre score est maintenant de: "+p+"\nEt votre position est: x = "+x2+" y = "+y2);
                        
                    }else {
                        //MOVE!␣x␣y***
                        byte[] suite = receiveTCPMessage(11);
                        String full = new String(prem)+new String(suite);
                        String x2 = full.substring(6,9);
                        String y2 = full.substring(10,13);
                        System.out.println(full);
                        System.out.println("Vous êtes maintenant en position : x = "+x2+" y = "+y2);
                        
                    }
                break;
                }
                case 3:{
                    System.out.println("combien de cases ?");
                    int nb = sc.nextInt();
                    sc.nextLine();
                    if(nb > 999 || nb < 1){
                        System.out.println("Veuillez choisir un nombre entre 1 et 999");
                        break;
                    }
                    String depl;
                    System.out.println("mouvement droite de "+nb+" cases");
                    if(nb<100){
                        if(nb<10){
                            depl="00"+String.valueOf(nb);
                        }else{
                            depl="0"+String.valueOf(nb);
                        }
                    }else{
                        depl=String.valueOf(nb);
                    }
                    System.out.println("Deplacement mgl: "+depl);
                    String out = "RIMOV "+depl+"***";
                    System.out.println(out);
                    sendTCPMessage(out);
                    
                    //MOVEF␣x␣y␣p***
                    byte[] prem = receiveTCPMessage(5);
                    String rec=new String(prem);
                    System.out.println(rec);
                    if(rec.equals("MOVEF")){
                        System.out.println("fantome attrapé");
                        byte[] suite = receiveTCPMessage(16);
                        String full = new String(prem)+new String(suite);
                        System.out.println(full);
                        
                        String x2 = full.substring(6,9);
                        String y2 = full.substring(10,13);
                        String p = full.substring(14,17);
                        System.out.println("Vous avez attrapé un fantôme!\nVotre score est maintenant de: "+p+"\nEt votre position est: x = "+x2+" y = "+y2);
                        
                    }else {
                        //MOVE!␣x␣y***
                        byte[] suite = receiveTCPMessage(11);
                        String full = new String(prem)+new String(suite);
                        String x2 = full.substring(6,9);
                        String y2 = full.substring(10,13);
                        System.out.println(full);
                        System.out.println("Vous êtes maintenant en position : x = "+x2+" y = "+y2);
                        
                    }
                break;
                }
                case 4:{
                    System.out.println("quitter partie");
                    sendTCPMessage("IQUIT***");
                    byte[] prem = receiveTCPMessage(8);
                    System.out.println(new String(prem));
                    fin=true;
                    commMulticast.arreter();
                    communication.arreter();
                    sc.close();
                    isOver=true;
                    disconnect();
                    return;
                }
                case 5:{
                    System.out.println("liste des joueurs");
                    sendTCPMessage("GLIS?***");
                    byte[] first = receiveTCPMessage(10);

                    if(first[4]=='E'){
                        System.out.println(first);
                        System.out.println("partie terminée");
                        return;
                    }
                    
                    int nbplayers = first[7];
                    System.out.println(new String(first));
                    
                    for (int i = 0; i < nbplayers; i++) {
                        //GPLYR␣id␣x␣y␣p***
                        byte[] player = receiveTCPMessage(29);
                        String mess2 = new String(player);
                        String id2 = mess2.substring(6,14);
                        int x2 = Integer.valueOf(mess2.substring(15,18));
                        int y2 = Integer.valueOf(mess2.substring(19,22));
                        int p = Integer.valueOf(mess2.substring(23,26));
                        System.out.println(mess2);
                        System.out.println("Joueur "+id2+"position : x : "+x2+" y : "+y2+ " points : "+p);
                    }
                    
                    System.out.println("ok liste des joueurs");
                    break;
                }
                case 6:{
                    System.out.println("Chat privé");
                    System.out.println("A qui voulez vous envoyer un message ?");
                    String dest = sc.nextLine();
                    System.out.println("Que voulez vous envoyer ?");
                    String msg = sc.nextLine();
                    String out = "SEND? "+dest+" "+msg+"***";
                    sendTCPMessage(out);
                    String recep = new String(receiveTCPMessage(8));
                    if(recep.equals("SEND!***")){
                        System.out.println("Message envoyé");
                    }else{
                        System.out.println("Erreur lors de l'envoi du message");
                    }
                    break;
                }
                case 7:{
                    System.out.println("Chat global");
                    System.out.println("Que voulez vous envoyer ?");
                    String msg = sc.nextLine();
                    String out = "MALL? "+msg+"***";
                    sendTCPMessage(out);
                    String recep = new String(receiveTCPMessage(8));
                    if(recep.equals("MALL!***")){
                        System.out.println("Message envoyé");
                    }else{
                        System.out.println("Erreur lors de l'envoi du message");
                    }
                    break;
                }
            } 
        }
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
    
    public static void disconnect() {
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
    public static byte[] receiveTCPMessage(int size) {
        byte[] data=new byte[size];
        try{
            socket.getInputStream().read(data);
        }
        catch(IOException e){
            System.out.println("Erreur de lecture");
        }
        return data;
    }
    
    
    public static void sendTCPMessage(String message) {
        out.print(message);
        out.flush();
    }
    
    public static void sendTCPMessage(byte[] message) {
        try {
            outB.write(message);
        } catch (IOException e) {
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

    public static String[] welc(){
        //[WELCO␣m␣h␣w␣f␣ip␣port***]
        String[] tmp = new String[2];
        String requete = new String(receiveTCPMessage(6));
        if(requete.equals("DUNNO ")) 
        {
            System.out.println("La partie demandée n'existe pas.");
            return null;
        }
        else if(!requete.equals("WELCO "))
        {
            System.out.println("Requête reçue inattendue.");
            return null;
        }
        byte[] m = receiveTCPMessage(1); // m
        receiveTCPMessage(1); // _
        byte[] h = receiveTCPMessage(2); // h
        receiveTCPMessage(1); // _
        byte[] w = receiveTCPMessage(2); // w
        receiveTCPMessage(1); // _
        byte[] f = receiveTCPMessage(1); // f
        receiveTCPMessage(1); // _
        byte[] ip = receiveTCPMessage(15); // ip
        receiveTCPMessage(1); // _
        byte[] port = receiveTCPMessage(4); // port
        receiveTCPMessage(3); // ***
        String newIp=new String(ip);
        for(int i=0;i<newIp.length();i++)
        {
            if(newIp.charAt(i)=='#')
            {
                newIp=newIp.substring(0,i);
                break;
            }
        }
        tmp[0]=newIp;
        tmp[1] = new String(port);
        System.out.println("Bienvenue dans la partie " + (int) m[0] + " qui a pour hauteur " + 
            (int) ((h[0] & 0xff) + (h[1] & 0xff) * 0x100) + " cases pour largeur " + 
            (int) ((w[0] & 0xff) + (w[1] & 0xff) * 0x100) + " cases ainsi que "+ 
            (int) f[0] + " fantômes et dont l'ip est " + new String(ip) + " et le port est " + 
            new String(port));
        return tmp;
    }
}
