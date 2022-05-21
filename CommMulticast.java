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
            while(!terminate){
                user="";
                message="";
                byte[] buffer = new byte[218];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String recu = new String(packet.getData(), 0, packet.getLength());
                if(recu.substring(0, 6).equals("SCORE")){
                    user=recu.substring(6,14);
                    message=recu.substring(15,recu.length()-3);
                    while(!affiche){}
                    System.out.println("Le joueur "+user+" a capturé un fantome !");
                    System.out.println("Ce dernier se cachait en x = "+message.substring(20,23)+" y = "+message.substring(24,27));
                    System.out.println(user+" a maintenant "+message.substring(16,19)+" points !");
                    affiche=false;
                }
                else{
                    if(recu.length()>16){
                        user=recu.substring(6,14);
                        message=recu.substring(15,recu.length()-3);
                    }
                    else{
                        message=recu.substring(0,recu.length()-3);
                    }
                    //while(!affiche){}
                    if(user.equals("")){
                        System.out.println(message);
                    }
                    else{
                        System.out.println(user+": "+message);
                    }
                }
                    //affiche=false;
            }
            return;
        }
        catch (Exception e) {
            if(!terminate){
                arreter();
            }
        } 
    }







}

