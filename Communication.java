import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Communication implements Runnable{

    int port;
    DatagramSocket socket;
    boolean affiche;
    volatile boolean terminate;
    boolean isPortFree;

    public Communication(int port) {
        this.port = port;
        affiche = false;
        terminate = false;
        System.out.println("Communication UDP : " + port);
        try {
            socket = new DatagramSocket(port);
            isPortFree = true;
        } 
        catch (Exception e) {
            isPortFree = false;
        }
    }

    public void arreter() {
        terminate = true;
        System.out.println("Déconnexion du UDP");
        socket.close();
    }

    @Override
    public void run() {
        try {
            while(!terminate) {
                byte[] buffer = new byte[218];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String reçu = new String(packet.getData(), 0, packet.getLength());
                String user=reçu.substring(6, 14);
                String message=reçu.substring(15, reçu.length() - 3);
                //while(!affiche){}
                System.out.println(user + " vous dit : \"" + message + "\"");
               // affiche=false;
            }
            return;
        }
        catch (Exception e) {
            if(!terminate){
                System.out.println("Error: " + e.getMessage());
                arreter();
            }
        } 
    }
}
