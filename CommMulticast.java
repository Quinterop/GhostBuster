import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;



public class CommMulticast implements Runnable{
    
    String ip;
    int port;
    MulticastSocket socket;
    boolean affiche;
    InetAddress address;
    volatile boolean terminate;


    

    public CommMulticast(String ip,int port) {
        this.port = port;
        try {
            address=InetAddress.getByName(ip);
            affiche=false;
            terminate=false;
            socket = new MulticastSocket(port);
            socket.joinGroup(address);
            System.out.println("Communication Multicast: " + port+" "+InetAddress.getByName(ip));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void arreter(){
        terminate=true;
        System.out.println("Déconnexion du multicast");
        try {
            socket.leaveGroup(address);
            socket.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }  
    }

    @Override
    public void run() {
        String user;
        String message;
        try {
            while(!terminate) {
                user="";
                message="";
                byte[] buffer = new byte[218];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String reçu = new String(packet.getData(), 0, packet.getLength());
                if(reçu.substring(0, 5).equals("GHOST")) {
                    message = "Un fantôme se trouve aux coordonnées (" + reçu.substring(6, 9) + ", " + reçu.substring(10, 13) + ").";
                }
                else if(reçu.substring(0, 5).equals("SCORE")) {
                    message = "Un fantôme s'est fait capturé par le joueur " + reçu.substring(6, 14) + " (" + reçu.substring(15, 19) + " points) aux coordonnées (" + reçu.substring(20, 23) + ", " + reçu.substring(24, 27) + ")."; 
                }
                else if(reçu.substring(0, 5).equals("MESSA")) {
                    message = reçu.substring(6, 14) + " a dit : " + reçu.substring(15, reçu.length() - 3);
                }
                else if(reçu.substring(0, 5).equals("ENDGA")) {
                    message = "La partie est terminée ! Le vainqueur est " + reçu.substring(6, 14) + " avec " + reçu.substring(15, 19) + " points !";
                }
                else {
                    message = reçu.substring(0, reçu.length() - 3);
                }
                //while(!affiche){}
                System.out.println(message);
                //affiche=false;
            }
            return;
        }
        catch (Exception e) {
            if(!terminate) {
                arreter();
            }
        } 
    }







}

