import java.net.InetAddress;
import java.net.MulticastSocket;



public class Communication implements Runnable{

    String ip;
    int port;
    MulticastSocket socket;

    public Communication(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new MulticastSocket(port);
            socket.joinGroup(InetAddress.getByName(ip));
            System.out.println("Connected to server");
            while(true){
                byte[] buffer = new byte[218];
                socket.receive(new java.net.DatagramPacket(buffer, buffer.length));
                System.out.println("oueeeeeeeeee");
                String reçu = new String(buffer);
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
