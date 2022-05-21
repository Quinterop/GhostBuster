import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    private static String id; // 8 caractères max
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static DataOutputStream outB;
    private static int port;
    private static int maxReadUDP = 50;
    private static String portUDP;
    private static Scanner sc = new Scanner(System.in);
    private static Communication communication;
    private static CommMulticast commMulticast;
    private static Thread t = new Thread(communication);
    private static Thread t2 = new Thread(commMulticast);
    
    public static void main(String[] args) {
        // Parsing de la ligne de commande
        id = args[0];
        port = Integer.parseInt(args[1]);
        if(id.length() != 8 || id.matches("^.*[^a-zA-Z0-9 ].*$")) {
            System.err.println("L'ID ne doit contenir que des charactères alphanumériques et être de taille exactement 8.");
            System.exit(1);
        }
        if(2000 > port || port > 65535) {
            System.err.println("La valeur donnée au port est illégale.");
            System.exit(1);
        }
        System.out.println("Ligne de commande parsée.");

        connect("127.0.0.1");
        avantPartie();
        enJeu();
    }

    public static void avantPartie() {
        // Déclaration des variables
        int choix, m, n;
        int[][] ogame;

        // Réception des messages [GAMES_n***] et [OGAME_m_s***] envoyés en connexion au serveur.
        ogame = get_game();
        for(int i = 0; i < ogame.length; i++) {
            System.out.println("La partie numéro " + ogame[i][0] + " a " + ogame[i][1] + " joueurs.");
        }

        while(true) {
            System.out.print(
                "\nSélectionnez un choix :\n" +
                "1/ Créer une nouvelle partie\n" +
                "2/ Rejoindre une partie existante\n" +
                "3/ Quitter la partie en cours\n" +
                "4/ Demander la taille d'un lobby\n" +
                "5/ Lister les joueurs d'un lobby\n" +
                "6/ Lister les lobbys rejoignables\n" +
                "7/ Commencer la partie\n\n" +
                "Votre choix : "
            );

            // Parsing du choix de l'utilisateur
            try {
                choix = Integer.parseInt(sc.nextLine());
                if(0 > choix || choix > 7) {
                    throw new IllegalArgumentException();
                }
            }  
            catch (IllegalArgumentException | NoSuchElementException | IllegalStateException e) {
                System.out.println("Choix donné illégal.");
                continue;
            }

            switch(choix) {
                case 1: // [NEWPL_id_port***]
                    System.out.print("Veuillez entrer un numéro de port UDP : ");
                    portUDP = sc.nextLine();
                    try {
                        if(Integer.parseInt(portUDP) < 1024 || Integer.parseInt(portUDP) > 8191) {
                            throw new IllegalArgumentException();    
                        }
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Numéro de port illégal.");
                        break;
                    }
                    m = newpl(id, portUDP);
                    if(m == -1) {
                        System.out.println("Erreur lors de la création de la nouvelle partie.");
                        break;          
                    }
                    System.out.println("Nouvelle partie numéro " + m + " créée.");
                    break;
                case 2: // [REGIS_id_port_m***]
                    System.out.print("Veuillez entrer un numéro de port UDP : ");
                    portUDP = sc.nextLine();
                    try {
                        if(Integer.parseInt(portUDP) < 1024 || Integer.parseInt(portUDP) > 8191) {
                            throw new IllegalArgumentException();    
                        }
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Numéro de port illégal.");
                        break;
                    }
                    System.out.print("Sélectionnez le numéro de partie que vous souhaitez rejoindre : ");
                    try {
                        n = Integer.parseInt(sc.nextLine());
                        if(0 > n || n > 255) {
                            throw new IllegalArgumentException();
                        }
                    } 
                    catch (IllegalArgumentException e) {
                        System.out.println("Numéro de partie illégal.");
                        break;
                    }
                    m = regis(id, portUDP, n);
                    if(m == -1) {
                        System.out.println("Erreur lors de la tentative de join la partie citée.");
                        break;
                    }
                    System.out.println("Vous avez rejoins la partie numéro " + m + ".");
                    break;
                case 3: // [UNREG***]
                    m = unreg();
                    if(m == -1) {
                        System.out.println("Vous n'avez aucune partie à quitter.");
                        break;
                    }
                    System.out.println("Vous avez quitté la partie numéro " + m + ".");
                    break;
                case 4: // [SIZE?_m***]
                    System.out.print("Sélectionnez le numéro de la partie : ");
                    try {
                        m = Integer.parseInt(sc.nextLine());
                        if(0 > m || m > 255) {
                            throw new IllegalArgumentException();
                        }
                    } 
                    catch (IllegalArgumentException e) {
                        System.out.println("Numéro de partie illégal.");
                        break;
                    }
                    int[] s = size(m);
                    if(s == null) {
                        System.out.println("Erreur lors de la demande de taille de cette partie. Elle peut être inexistante.");
                        break;
                    }
                    System.out.println("La partie " + s[0] + " a pour hauteur " + s[1] + " cases et pour largeur " + s[2] + " cases.");
                    break;
                case 5: // [LIST?_m***]
                    System.out.print("Sélectionnez le numéro de la partie : ");
                    try {
                        m = Integer.parseInt(sc.nextLine());
                        if(0 > m || m > 255) {
                            throw new IllegalArgumentException();
                        }
                    } 
                    catch (IllegalArgumentException e) {
                        System.out.println("Numéro de partie illégal.");
                        break;
                    }
                    String[] id_list = list(m);
                    if(id_list == null) {
                        System.out.println("Erreur lors de la demande de joueurs de cette partie. Elle peut être inexistante.");
                        break;
                    }

                    for(int i = 0; i < id_list.length; i++) {
                        System.out.println("ID : " + id_list[i]);
                    }
                    break;
                case 6: // [GAME?***]
                    ogame = game();
                    System.out.println("Nombre de parties : " + ogame.length);
                    for(int i = 0; i < ogame.length; i++) {
                        System.out.println("La partie numéro " + ogame[i][0] + " a " + ogame[i][1] + " joueurs.");
                    }
                    break;
                case 7: // [START***]
                    start();
                    return;
                default:
                    System.out.println("Choix donné illégal.");
                    break;
            }
        }
    }

    /**
     * Envoie la requête [GAME?***] au serveur
     * @return un tableau de tableaux sous la forme [0: m, 1: s] où m est le numéro de partie et s le nombre de joueurs
     */
    public static int[][] game() {
        sendTCPMessage("GAME?***");
        return get_game();
    }

     /**
     * Reçoit la requête [GAME?***] au serveur
     * @return un tableau de tableaux sous la forme [0: m, 1: s] où m est le numéro de partie et s le nombre de joueurs
     */
    public static int[][] get_game() {
        byte[] first = receiveTCPMessage(10);
        int nbgames = first[6];
        
        int[][] output = new int[nbgames][2];
        for (int i = 0; i < nbgames; i++) {
            
            byte[] gamei = receiveTCPMessage(12);
            output[i] = new int[]{gamei[6], gamei[8]};
        }

        return output;
    }

    /**
     * Envoie la requête [NEWPL_id_port***]
     * @param id l'id du joueur
     * @param portUDP le port UDP du joueur
     * @return l'index m représentation le numéro de partie où s'est inscrit le joueur. Retourne -1 si erreur.
     */
    public static int newpl(String id, String portUDP) {
        sendTCPMessage("NEWPL " + id + " " + portUDP + "***");
        return newpl_regis();
    }

    /**
     * Envoie la requête [REGIS_id_port_m***]
     * @param id l'id du joueur
     * @param portUDP le port UDP du joueur
     * @param m la partie souhaitée être rejoins
     * @return l'index m représentation le numéro de partie où s'est inscrit le joueur. Retourne -1 si erreur.
     */
    public static int regis(String id, String portUDP, int m) {
        sendTCPMessage("REGIS " + id + " " + portUDP + " " + (char) m + "***");
        return newpl_regis();
    }

    /**
     * Code commun aux fonctions pour les requêtes [NEWPL_id_port***] et [REGIS_id_port_m***]
     */
    public static int newpl_regis() {
        String reponse = new String(receiveTCPMessage(5)); // REGNO ou REGOK
        if(reponse.equals("REGNO")) {
            receiveTCPMessage(3); // lis les "***" restantes
            System.out.println(reponse+" NO");
            System.err.println("Échec de création de la partie.");
            return -1;
        }
        if(!reponse.equals("REGOK")) {
            System.out.println(reponse+" ???");
            System.err.println("Requête reçue inattendue.");
            System.exit(1);
        }
        byte[] regok = receiveTCPMessage(5); // _m***
        return regok[1];
    }

    /**
     * Envoie la requête [UNREG***] au serveur
     * @return le numéro de la partie quittée, ou -1 si la requête a échouée
     */
    public static int unreg() {
        sendTCPMessage("UNREG***");
        String reponse = new String(receiveTCPMessage(5));
        if(reponse.equals("DUNNO")) {
            receiveTCPMessage(3); // lis les "***" restantes
            System.err.println("Inscrit à aucune partie.");
            return -1;
        }
        if(!reponse.equals("UNROK")) {
            System.err.println("Requête reçue inattendue.");
            System.exit(1);
        }
        byte[] unrok = receiveTCPMessage(5);
        return unrok[1];
        
    }

    /**
     * Envoie la requête [SIZE?_m***] au serveur
     * @param m le numéro de partie demandé
     * @return un tableau au format {m, h, w} où m est le numéro de partie, h et w les tailles du labyrinthe correspondant. Renvoie null si la partie m n'existe pas.
     */
    public static int[] size(int m) {
        m = m & 0xFF;
        String sSize = "SIZE? m***";
        byte[] b = sSize.getBytes();
        b[6] = (byte) m;
        sendTCPMessage(b);

        String reponse = new String(receiveTCPMessage(5));
        if(reponse.equals("DUNNO")) {
            receiveTCPMessage(3); // lis les "***" restantes
            return null;
        }
        else if(!reponse.equals("SIZE!")) {
            System.err.println("Requête reçue inattendue.");
            System.exit(1);
        }

        // Réponse attendue : [SIZE!_m_h_w***]
        byte[] size = receiveTCPMessage(11); // _m_h_w***
        int m2 = size[1];
        int h = (size[3] & 0xff) + (size[4] & 0xff) * 0x100;
        int w = (size[6] & 0xff) + (size[7] & 0xff) * 0x100;
        return new int[]{m2, h, w};
    }

    /**
     * Envoie la requête [LIST?_m***] au serveur
     * @param m le numéro de partie demandé
     * @return un tableau listant les ID des joueurs de la partie m. Renvoie null si la partie m est inexistante.
     */
    public static String[] list(int m) {
        m = m & 0xFF;
        String sList = "LIST? m***";
        byte[] b = sList.getBytes();
        b[6] = (byte) m;
        sendTCPMessage(b);
        String reponse = new String(receiveTCPMessage(5));
        if(reponse.equals("DUNNO")) {
            receiveTCPMessage(3); // lis les "***" restantes
            return null;
        }
        else if(!reponse.equals("LIST!")) {
            System.err.println("Requête reçue inattendue.");
            System.exit(1);
        }
        byte[] list = receiveTCPMessage(7);
        int m2 = list[1];
        int s = list[3];
        if(m2 != m) {
            System.err.println("Réponse reçue inattendue.");
            System.exit(1);
        }

        String[] output = new String[s];
        byte[] playr;
        for(int i = 0; i < s; i++) {
            playr = receiveTCPMessage(17);
            output[i] = new String(Arrays.copyOfRange(playr, 6, 14));
        }

        return output;
    }

    /**
     * Envoie la requête [START***] au serveur
     */
    public static void start() {
        sendTCPMessage("START***");
        String[] welco = welco();
        commMulticast = new CommMulticast(welco[4], Integer.parseInt(welco[5]));
        communication = new Communication(Integer.parseInt(portUDP));
        t2 = new Thread(commMulticast);
        t.start();
        t2.start();
        String[] posit = posit();

        System.out.println("Bienvenue dans la partie " + welco[0] + " qui a pour hauteur " + welco[1] + " cases pour largeur " + welco[2] + " cases ainsi que " + welco[3] + " fantômes et dont l'ip est " + welco[4] + " et le port est " + welco[5]);
        System.out.println("Vous êtes positonné dans les coordonnées (" + posit[1] + ", " + posit[2] + ").");
        
        
    }

    /**
     * Gestion de la requête [WELCO_m_h_w_f_ip_port***]
     * @return un tableau de chaînes de charactères type [m, h, w, f, ip, port]
     */
    public static String[] welco() {
        String requete = new String(receiveTCPMessage(5));
        if(requete.equals("DUNNO")) {
            receiveTCPMessage(3); // lis les "***" restantes
            System.err.println("La partie demandée n'existe pas.");
            return null;
        }
        else if(!requete.equals("WELCO")) {
            System.err.println("Requête reçue inattendue.");
            System.exit(1);
        }
        byte[] donnees = receiveTCPMessage(34); // _m_h_w_f_ip_port***
        int m = donnees[1];
        int h = (donnees[3] & 0xff) + (donnees[4] & 0xff) * 0x100;
        int w = (donnees[6] & 0xff) + (donnees[7] & 0xff) * 0x100;
        int f = donnees[9];
        String ip = new String(Arrays.copyOfRange(donnees, 11, 26));
        String port = new String(Arrays.copyOfRange(donnees, 27, 31));

        for(int i = 0; i < ip.length(); i++) {
            if(ip.charAt(i) == '#') {
                ip = ip.substring(0, i);
                break;
            }
        }

        return new String[]{String.valueOf(m), String.valueOf(h), String.valueOf(w), String.valueOf(f), ip, port};
    }

    /**
     * Gestion de la requête [POSIT_id_x_y***]
     * @return un tableau de chaînes de charactères type [id, x, y]
     */
    public static String[] posit() {
        String requete = new String(receiveTCPMessage(5));
        if(!requete.equals("POSIT")) {
            System.err.println("Requête reçue inattendue.");
            System.exit(1);
        }
        String donnees = new String(receiveTCPMessage(20));
        return new String[] {donnees.substring(1,9), donnees.substring(10, 13), donnees.substring(14, 17)};
    }

    public static void enJeu() {
        // Déclaration des variables
        int choix, n;
        int[] deplacement;
        String destId, fleche, mess;
        String[][] liste_joueurs;

        while(true){
            //commMulticast.affiche=true;
            //communication.affiche=true;
            System.out.print(
                "\nSélectionnez un choix :\n" +
                "0-3/ Se déplacer, respectivement, vers le haut, bas, gauche et droite\n" +
                "4/ Quitter la partie en cours\n" +
                "5/ Lister les joueurs de la partie\n" +
                "6/ Envoyer un message privé\n" +
                "7/ Envoyer un message public\n\n" +
                "Votre choix : \n"
            );

            // Parsing du choix de l'utilisateur
            try {
                choix = Integer.parseInt(sc.nextLine());
                if(0 > choix || choix > 7) {
                    throw new IllegalArgumentException();
                }
            }  
            catch (IllegalArgumentException | NoSuchElementException | IllegalStateException e) {
                System.out.println("Choix donné illégal.");
                continue;
            }

            switch(choix){
                case 0: // [UPMOV_d***]
                    fleche = "^";
                case 1: // [DOMOV_d***]
                    fleche = "v";
                case 2: // [LEMOV_d***]
                    fleche = "<-";
                case 3: // [RIMOV_d***]
                    fleche = "->";

                    System.out.print("De combien de cases souhaitez-vous vous déplacer : ");
                    try {
                        n = Integer.parseInt(sc.nextLine());
                        if(n > 999 || 1 > n) {
                            throw new IllegalArgumentException();
                        }
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Veuillez sélectionner un nombre valide compris entre 1 et 999.");
                        break;
                    }
                    deplacement = mov(n, choix);
                    System.out.println("Vous vous êtes déplacé effectivement de (" + deplacement[0] + ", " + deplacement[1] + ") vers " + fleche);
                    if(deplacement[2] != -1) {
                        System.out.println("Vous avez attrapé un fantôme ! Votre score est de " + deplacement[2]);
                    }
                    break;
                case 4: // [IQUIT***]
                    if(iquit()) {
                        commMulticast.arreter();
                        communication.arreter();
                        sc.close();
                        disconnect();
                        return;
                    }
                    else {
                        System.out.println("Echec lors de la tentative d'abandon de la partie.");
                    }
                    break;

                case 5: // [GLIS?***]
                    liste_joueurs = glis();
                    if(liste_joueurs == null) {
                        System.out.println("La partie est terminée !");
                        return;
                    }
                    System.out.println("Le lobby est composé de " + liste_joueurs.length + " joueurs");
                    for(int i = 0; i < liste_joueurs.length; i++) {
                        System.out.println("Joueur " + liste_joueurs[i][0] + " se situant dans (" + liste_joueurs[i][1] + ", " + liste_joueurs[i][2] + ") avec pour score " + liste_joueurs[i][3] + ".");
                    }
                    break;
                case 6: // [SEND?_id_mess***]
                    System.out.print("À qui voulez-vous envoyer un message privé : ");
                    destId = sc.nextLine();
                    System.out.print("Entrez le message que vous souhaitez envoyer : ");
                    mess = sc.nextLine();
                    if(send(destId, mess)) {
                        System.out.println("Message envoyé.");
                    }
                    else {
                        System.out.println("Erreur lors de l'envoi du message.");
                    }
                    break;
                case 7: // [MALL?_mess***]
                    System.out.print("Entrez le message que vous souhaitez envoyer : ");
                    mess = sc.nextLine();
                    if(mall(mess)) {
                        System.out.println("Message envoyé.");
                    }
                    else {
                        System.out.println("Erreur lors de l'envoi du message.");
                    }
                    break;
                default:
                    System.out.println("Choix donné illégal.");
                    break;

            } 
        }
    }

    /**
     * Envoie une requête de déplacement [..MOV_d***] au serveur
     * @param d valeur de déplacement souhaité
     * @param direction 0 -> haut, 1 -> bas, 2 -> gauche, 3 -> droite 
     * @return {x, y, p} où x et y sont les nouvelles coordonnées du joueur, et p son nouveau score s'il a touché un fantôme - sinon -1 -
     */
    public static int[] mov(int d, int direction) { 
        // Déclaration des variables
        String coordonnees, d_string, p, reponse, requete, x, y;

        // Transformation de d en chaîne de charactères de taille 3
        if(d < 10) {
            d_string = "00" + d;
        }
        else if(d < 100) {
            d_string = "0" + d;
        }
        else if(d < 1000) {
            d_string = String.valueOf(d);
        }
        else {
            System.err.println("Valeur de déplacement illégale.");
            return null;
        }

        // Formation de la requête basée sur la direction
        if(direction == 0) {
            requete = "UP";
        }
        else if(direction == 1) {
            requete = "DO";
        }
        else if(direction == 2) {
            requete = "LE";
        }
        else if(direction == 3) {
            requete = "RI";
        }
        else {
            System.err.println("Valeur de direction illégale.");
            return null;
        }
    
        // Envoi de la requête [..MOV_d***]
        requete += "MOV " + d_string + "***";
        sendTCPMessage(requete);

        // Vérification de la légalité de la réponse du serveur
        reponse = new String(receiveTCPMessage(5));
        if(!reponse.equals("MOVEF") || !reponse.equals("MOVE!")) {
            System.err.println("Requête inattendue.");
            System.exit(1);
        }

        // Parsing des réponses [MOVE!_x_y**] et [MOVEF_x_y_p***]
        coordonnees = new String(receiveTCPMessage(16));
        x = coordonnees.substring(1, 4);
        y = coordonnees.substring(5, 8);
        if(reponse.equals("MOVE!")) {
            return new int[]{Integer.parseInt(x), Integer.parseInt(y), -1};
        }
        p = coordonnees.substring(9, 12);
        return new int[]{Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(p)};
    }

    /**
     * Envoie la requête [IQUIT***] au serveur
     * @return true si le serveur répond par conséquent, sinon false
     */
    public static boolean iquit() {
        sendTCPMessage("IQUIT***");
        return new String(receiveTCPMessage(8)).equals("GOBYE***");
    }

    /**
     * Envoie la requête [GLIS?***] au serveur
     * @return un tableau composé de tableaux de type [id, x, y, p] où id désigne l'id du joueur, x et y ses coordonnées et p son score. La fonction renvoie null si la partie est finie lorsque ce message est envoyé.
     */
    public static String[][] glis() {
        // Déclaration des variables
        int s;
        String gplyr, id, x, y, p;
        String[][] output;

        // Envoi de la requête [GLIS?***]
        sendTCPMessage("GLIS?***");

        // Vérification de la légalité de la réponse du serveur
        String reponse = new String(receiveTCPMessage(5));
        if(reponse.equals("GOBYE")) {
            receiveTCPMessage(3); // lis les "***" restantes
            System.out.println("Partie terminée.");
            return null;
        }
        else if(!reponse.equals("GLIS!")) {
            System.err.println("Requête reçue inattendue.");
            System.exit(1);
        }

        // Parsing de la reponse [GLIS!_s***]
        s = receiveTCPMessage(5)[1];

        // Réception des réponses [GPLYR_id_x_y_p***]
        output = new String[s][4];
        for (int i = 0; i < s; i++) {
            gplyr = new String(receiveTCPMessage(29));
            id = gplyr.substring(6, 14);
            x = gplyr.substring(15, 18);
            y = gplyr.substring(19, 22);
            p = gplyr.substring(23, 26);
            output[i] = new String[]{id, x, y, p};
        }
        return output;
    }

    /**
     * Envoie la requête [SEND?_id_mess***] au serveur
     * @param id id du destinaire
     * @param mess message à envoyer
     * @return true si le serveur répond que le message a été envoyé avec succès, false sinon
    */
    public static boolean send(String id, String mess) {
        sendTCPMessage("SEND? " + id + " " + mess + "***");
        return new String(receiveTCPMessage(8)).equals("SEND!***");
    }

    /**
     * Envoie la requête [MALL?_mess***] au serveur
     * @param mess message à envoyer
     * @return true si le serveur répond que le message a été envoyé avec succès, false sinon
     */
    public static boolean mall(String mess) {
        sendTCPMessage("MALL? " + mess + "***");
        return new String(receiveTCPMessage(8)).equals("MALL!***");
    }

    public static void connect(String adresse) {
        try {
            socket = new Socket(adresse, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            outB = new DataOutputStream(socket.getOutputStream());
        } 
        catch (IOException e) {
            System.err.println("Erreur lors de la création de la socket.");
            System.exit(1);
        }
    }
    
    public static void disconnect() {
        try {
            in.close();
            out.close();
            socket.close();
        } 
        catch (IOException e) {
            System.err.println("Erreur lors de la tentative de déconnexion du serveur.");
            System.exit(1);
        }
    }
    
    public static byte[] receiveTCPMessage(int size) {
        byte[] data=new byte[size];
        try {
            socket.getInputStream().read(data);
        }
        catch(IOException e) {
            System.err.println("Erreur de lecture du message TCP.");
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
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String recieveUdpMessage() {
        try {
            DatagramSocket dso = new DatagramSocket(port);
            byte[]data = new byte[maxReadUDP];
            DatagramPacket paquet = new DatagramPacket(data,data.length);
            dso.receive(paquet);
            String st = new String(paquet.getData(), 0, paquet.getLength());
            dso.close();
            return st;
        } 
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
