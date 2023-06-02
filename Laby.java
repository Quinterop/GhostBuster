import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
//import actionListener 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Scanner;
import java.awt.BorderLayout;
//import Client;

public class Laby{
    public int[][] laby;
    public int longueur;
    public int largeur;
    public int cptPlayers;
    public int nbFantomes;  
    public int score;
    int curX;
    int curY;
    JFrame frame;
    int nbParties;
    int[][] parties;
    int expected;
    
    /*
    -1 = inconnu
    0 = joueur
    1 = vide
    2 = mur
    3 = fantôme
    3 - infini = autres joueurs
    */
    public Laby(int nbParties, int[][] parties){
        frame = new JFrame("Laby");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.nbParties = nbParties;
        this.parties = parties;
    }
    
    public void genLaby(int longueur, int largeur,int x, int y,int nbFantomes){
        this.longueur = longueur;
        this.largeur = largeur;
        this.laby = new int[longueur][largeur];
        this.cptPlayers = 3;
        this.nbFantomes = nbFantomes;
        this.score = 0;
        this.curX = x;
        this.curY = y;
        for(int i = 0; i < longueur; i++){
            for(int j = 0; j < largeur; j++){
                this.laby[i][j] = -1;
            }
        }
        laby[x][y] = 0;
        frame = new JFrame("Laby");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }
    
    
    public void movePlayer(int x, int y, int expected){
        laby[curX][curY] = 1;
        laby[x][y] = 0;
        //make elements between x and curX and y and curY 1
        
        System.out.println("x : "+x+" y : "+y);
        System.out.println("curX : "+curX+" curY : "+curY);
        
        if(x < curX){ //move up
            for(int i = x; i < curX; i++){
                laby[i][y] = 1;
            }if(curX-x<expected){
                laby[x-1][curY] = 2;
            }
        }
        
        else if(x>curX){ //move down
            for(int i = curX; i < x; i++){
                laby[i][y] = 1;
            }
            System.out.println(curX+" "+x);
            System.out.println(expected);
            if(x-curX<expected){
                laby[x+1][curY] = 2;
            }
        } 
        
        if(y < curY){ //move left
            for(int i = y; i < curY; i++){
                laby[x][i] = 1;
            }
            if(curY-y<expected){
                laby[curX][y-1] = 2;
            }
        }
        
        else if (y>curY) { //move right
            for(int i = curY; i < y; i++){
                laby[x][i] = 1;
            }
            
            if(y-curY<expected){
                laby[curX][y+1] = 2;
            }
        }
        
        curX = x;
        curY = y;
    }
    
    public void showPlayers(int[][] playersxy){
        for(int i = 0; i < playersxy.length; i++){
            cptPlayers++;
            laby[playersxy[i][0]][playersxy[i][1]] = cptPlayers;
        }
    }
    
    public void showFantomes(int[][] fantomesxy){
        for(int i = 0; i < fantomesxy.length; i++){
            laby[fantomesxy[i][0]][fantomesxy[i][1]] = 3;
        }
    }
    
    public void showLobby(int nbParties, int[][] parties){
        HashMap<JButton, Integer> buttons = new HashMap<JButton, Integer>();
        
        JPanel rows = new JPanel();
        
        rows.setLayout(new GridLayout(nbParties,1));
        
        for(int i = 0; i < nbParties; i++){       
            JPanel panel = new JPanel();
            
            JButton label = new JButton("partie " + (parties[i][0]) + " joueurs " + (parties[i][1]));
            JButton liste = new JButton("liste joueurs");
            JButton taille = new JButton("taille laby");
            
            
            label.setPreferredSize(new Dimension(600,100));
            liste.setPreferredSize(new Dimension(200,100));
            taille.setPreferredSize(new Dimension(200,100));
            //actu.setPreferredSize(new Dimension(200,100));
            // actu.setMargin(null);
            
            panel.add(label);
            panel.add(liste);
            panel.add(taille);
            //panel.add(actu);
            
            buttons.put(label,i);
            buttons.put(liste, i);
            buttons.put(taille, i);
            //buttons.put(actu, i);
            rows.add(panel);
        }
        
        JScrollPane scroll = new JScrollPane(rows,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        frame.add(scroll);
        frame.pack();
        frame.setSize(new Dimension(1300,800));
        JPanel pan = new JPanel();
        JButton newGame = new JButton("créer partie");
        JButton actu = new JButton("actualiser parties");
        pan.add(actu);
        pan.add(newGame);
        frame.add(pan, BorderLayout.NORTH);
        
        //frame.add(panel);
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
        frame.setResizable(false);
        
        
        //iterate through the hashmap
        for(JButton button : buttons.keySet()){
            final int key = buttons.get(button);
            button.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    
                    if(e.getActionCommand().equals("liste joueurs")){
                        String[] players = Client.list(key); //RECUP JOUEURS
                        String conc = "";
                        for(String player : players){
                            conc += player + "\n";
                        }
                        JTextArea textArea = new JTextArea(players.length, 9);
                        textArea.setText(conc);
                        textArea.setEditable(false);  
                        // wrap a scrollpane around it
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        // display them in a message dialog
                        JOptionPane.showMessageDialog(frame, scrollPane);
                    }
                    
                    
                    else if(e.getActionCommand().equals("taille laby")){
                        int[] size = Client.size(key);
                        String message = "partie "+size[0]+ '\n' 
                        +"hauteur : "+size[1]+'\n' + "largeur :"+size[2]; 
                        JOptionPane.showMessageDialog(frame, message, "partie "+key, JOptionPane.PLAIN_MESSAGE);
                    }
                    
                    else {
                        //REJOINDRE PARTIE
                        int numpartie = Client.regis(Client.id,Client.portUDP,key);
                        if(numpartie == -1){
                            System.out.println("erreur");
                        }
                        partiePopUp(numpartie);
                    }
                }
            });
        }
        
        newGame.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                int numpartie = Client.newpl(Client.id ,Client.portUDP);
                if(numpartie == -1){
                    System.out.println("erreur");
                }
                partiePopUp(numpartie);}
            });
            
            actu.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    //RECUP PARTIES
                    int[][] parties2 = Client.game();
                    int nbparties2 = parties2.length;
                    showLobby(nbparties2, parties2);
                }
            });
            
        }
        
        public void partiePopUp(int numpartie){
            String message = "inscrit dans partie "+numpartie;
            String[] buttons = {"START", "SE DESINSCRIRE"};
            int choice = JOptionPane.showOptionDialog(frame, message, "inscrit", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, buttons, buttons[0]);
            if(choice == 0){
                String[][] params = Client.start();
                int h = Integer.parseInt(params[0][1]);
                int w = Integer.parseInt(params[0][2]);
                int x = Integer.parseInt(params[1][1]);
                int y = Integer.parseInt(params[1][2]);
                int nbf = Integer.parseInt(params[0][3]);
                genLaby(h, w, x, y, nbf);
                showLaby();
            }else{
                int m = Client.unreg();
                if(m == -1){
                    System.out.println("erreur");
                    showLobby(nbParties, parties);
                }else{
                    System.out.println("désinscrit");
                    showLobby(nbParties, parties);
                }
                
                
            }
        }
        //swing windows to show the laby
        public void showLaby(){
            
            frame = new JFrame("Laby"); //TODO FIX THIS 
            
            
            for(int i = 1;i<frame.getComponentCount();i++){
                System.out.println(frame.getComponent(i).getName());
                frame.remove(frame.getComponent(i));
            }
            frame.revalidate();
            frame.repaint();
            
            frame.invalidate();
            
            
            frame.setSize(longueur*50,largeur*50);
            if(frame.getSize().getWidth()>1000){
                frame.setSize(1000,1000);
            }
            if(frame.getSize().getHeight()>1000){
                frame.setSize(1000,1000);
            }
            if(frame.getSize().getWidth()<100){
                frame.setSize(200,200);
            }
            if(frame.getSize().getHeight()<100){
                frame.setSize(200,200);
            }
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(longueur,largeur));
            for(int i = 0; i < longueur; i++){
                for(int j = 0; j < largeur; j++){
                    JButton button = new JButton();
                    button.setBackground(Color.black);
                    if(laby[i][j] == 0){
                        button.setBackground(Color.blue);
                    }
                    else if(laby[i][j] == 1){
                        button.setBackground(Color.white);
                    }
                    else if(laby[i][j] == 2){
                        button.setBackground(Color.gray);
                    }
                    else if(laby[i][j] == 3){
                        button.setBackground(Color.red);
                    }
                    else if(laby[i][j] > 3){
                        button.setBackground(Color.GREEN);
                    }
                    /*
                    -1 = inconnu
                    0 = joueur
                    1 = vide
                    2 = mur
                    3 = fantôme
                    3 - infini = autres joueurs
                    */
                    button.setEnabled(false);
                    panel.add(button);
                }
            }
            frame.add(panel);
            JPanel options = new JPanel();
            /*
            items :
            mouvements haut, bas, gauche, droite
            quitter partie
            demander liste des joueurs
            textbox message
            textbox destinataire
            bouton envoyer multi
            afficher score
            */
            //add items to options
            options.setLayout(new GridLayout(3,5));
            JButton haut = new JButton("haut");
            JButton bas = new JButton("bas");
            JButton gauche = new JButton("gauche");
            JButton droite = new JButton("droite");
            JTextArea distance = new JTextArea("distance");
            JButton quitter = new JButton("quitter");
            JButton liste = new JButton("mettre a jour joueurs");
            JTextArea message = new JTextArea();
            JTextArea destinataire = new JTextArea();
            JButton envoyer = new JButton("envoyer a tous");
            JButton envoyerDest = new JButton("envoyer a un");
            JLabel score = new JLabel("score : "+this.score);
            
            options.add(haut);
            options.add(bas);
            options.add(gauche);
            options.add(droite);
            options.add(distance);
            options.add(quitter);
            options.add(liste);
            options.add(message);   
            options.add(envoyer);
            options.add(destinataire);
            options.add(score);
            
            frame.add(options,BorderLayout.SOUTH);
            frame.setBackground(Color.black);
            frame.validate();
            //System.out.println("showLaby");
            frame.revalidate();
            frame.repaint();
            frame.setVisible(true);
            frame.setResizable(false);
            
            //add listeners to buttons
            haut.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    expected = Integer.parseInt(distance.getText());
                    String d = distance.getText();
                    int[] res = Client.mov(expected, 0);
                    movePlayer(res[0], res[1], expected);
                    score.setText("score : "+res[2]);
                    showLaby();
                }
            });
            
            bas.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    expected = Integer.parseInt(distance.getText());
                    String d = distance.getText();
                    int[] res = Client.mov(expected, 1);
                    movePlayer(res[0], res[1], expected);
                    score.setText("score : "+res[2]);
                    frame.revalidate();
                    frame.repaint();
                    showLaby();
                }
            });
            
            gauche.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    expected = Integer.parseInt(distance.getText());
                    String d = distance.getText();
                    int[] res = Client.mov(expected, 2);
                    movePlayer(res[0], res[1], expected);
                    score.setText("score : "+res[2]);
                    frame.revalidate();
                    frame.repaint();
                    showLaby();
                }
            });
            
            droite.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    expected = Integer.parseInt(distance.getText());
                    String d = distance.getText();
                    int[] res = Client.mov(expected, 3);
                    movePlayer(res[0], res[1], expected);
                    score.setText("score : "+res[2]);
                    frame.revalidate();
                    frame.repaint();
                    showLaby();
                    //TODO ACTUALISER
                }
            });
            
            quitter.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    if(!Client.iquit()){
                        System.out.println("ERREUR IQUIT");
                    }
                    //TODO CLOSE FRAME
                }
            });
            
            liste.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    String conc = "";
                    String[][] list = Client.glis();
                    for(int i = 0; i < list.length; i++){
                        conc += "joueur "+list[i][0]+" x: "+list[i][1]+" y: "+list[i][2]+"score :"+list[i][3]+"\n";
                    }
                    JTextArea textArea = new JTextArea(list.length, 30);
                        textArea.setText(conc);
                        textArea.setEditable(false);  
                        // wrap a scrollpane around it
                        JScrollPane scrollPane = new JScrollPane(textArea);
                        // display them in a message dialog
                        JOptionPane.showMessageDialog(frame, scrollPane);
                        int[][] res = new int[list.length][2];
                        for(int i = 0; i < list.length; i++){
                            res[i][0] = Integer.parseInt(list[i][1]);
                            res[i][1] = Integer.parseInt(list[i][2]);
                        }
                        showPlayers(res);
                        showLaby(); //TODO FIX              
                }
                
            });
            
            envoyer.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    String s = message.getText();
                    if(!Client.mall(s)){
                        System.out.println("ERREUR MALL");
                    }
                    
                }
            });
            
            envoyerDest.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e){
                    String s = message.getText(); 
                    String dest = destinataire.getText(); 
                    
                    if(!Client.send(dest,s)){
                        System.out.println("ERREUR SEND");
                    }
                    
                    
                }
            });
            
            //add listeners to panel
            
        }
        
        
        public static void main(String[] args){
            int [][] partie = {{0,4}};
            //Laby laby = new Laby(10,10,5,5,3,1,partie);
            int [][] partie2 = {{0,4},{1,4},{2,4},{3,4},{4,4},{5,4},{6,4},{7,4},{8,4},{9,4}};
            Laby laby = new Laby(partie2.length, partie2);
            int[][] playersxy = {{0,0},{0,1},{0,2},{0,3},{0,4},{0,5},{0,6},{0,7},{0,8},{0,9}};
            int[][] fantomesxy = {{1,1},{1,2},{1,3},{1,4},{1,5},{1,6},{1,7},{1,8},{1,9}};
            //laby.showLobby(partie2.length, partie2);
            laby.genLaby(10, 10, 5, 5, 9);
            laby.showPlayers(playersxy);
            laby.showFantomes(fantomesxy);
            laby.showLaby();
            
            Scanner sc = new Scanner(System.in);
            String s = sc.nextLine();
            System.out.println("moving");
            laby.movePlayer(8,5,10);
            laby.showLaby();

        }
    }