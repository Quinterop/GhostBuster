import java.net.DatagramPacket;
import java.net.DatagramSocket;




public class Communication implements Runnable{

    String ip;
    int port;
    DatagramSocket socket;


    public Communication(String ip, int port) {
        this.ip = ip;
        this.port = port;
        System.out.println("Communication: " + ip + " " + port);
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
                System.out.println("test");
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                System.out.println("oueeeeeeeeee");
                String reçu = new String(packet.getData(), 0, packet.getLength());
                String user=reçu.substring(6,14);
                String message=reçu.substring(15,reçu.length()-4);
                System.out.println(user+": "+message);
            }
        }
        catch (Exception e) {
            socket.close();
            System.out.println("Error: " + e.getMessage());
        } 
    }
}
