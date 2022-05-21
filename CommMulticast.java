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
                String reçu = new String(packet.getData(), 0, packet.getLength());
                if(reçu.length()>16){
                    user=reçu.substring(6,14);
                    message=reçu.substring(15,reçu.length()-3);
                }
                else{
                    message=reçu.substring(0,reçu.length()-3);
                }
                //while(!affiche){}
                if(user.equals("")){
                    System.out.println(message);
                }
                else{
                    System.out.println(user+": "+message);
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

