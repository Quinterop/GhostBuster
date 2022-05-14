import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        Client client = new Client();
        int port=Integer.parseInt(args[0]);
        client.connect(port);
        client.out.println("REGIS_smilouuu_4244");
        client.out.flush();
    }

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Client() {
        // Initialize the socket
        socket = null;
        // Initialize the input stream
        in = null;
        // Initialize the output stream
        out = null;
    }

    public void connect(int port) {
        try {
            // Create a new socket
            socket = new Socket("localhost", port);
            // Create a new input stream
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Create a new output stream
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Could not connect to the server.");
            System.exit(1);
        }
    }

    public void disconnect() {
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

    public void sendMessage(String message) {
        out.println(message);
    }

    public String receiveMessage() {
        try {
            // Read a message from the server
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Could not receive a message from the server.");
            System.exit(1);
        }
        return null;
    }
}
    
    //connect to tcp
    

