import java.net.DatagramPacket;
import java.net.DatagramSocket;




public class Communication implements Runnable{


    int port;
    DatagramSocket socket;


    public Communication(int port) {
        this.port = port;
        System.out.println("Communication UDP: " + port);
        try {
            socket = new DatagramSocket(port);
        } catch (Exception e) {
            System.out.println("mmmmhhh étrange tout ça");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Connected to server");
            while(true){
                byte[] buffer = new byte[218];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String reçu = new String(packet.getData(), 0, packet.getLength());
                String user=reçu.substring(6,14);
                String message=reçu.substring(15,reçu.length()-3);
                System.out.println(user+": "+message);
            }
        }
        catch (Exception e) {
            socket.close();
            System.out.println("Error: " + e.getMessage());
        } 
    }
}
