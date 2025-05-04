import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable{
    ArrayList <ConnectionHandler> clients;

    public Server(){
        clients = new ArrayList<>();
    }

    @Override
    public void run(){
        try {
            ServerSocket serverSocket= new ServerSocket(9999);
            Socket client = serverSocket.accept();
            clients.add(new ConnectionHandler(client));

        } catch (IOException e) {
            // TODO: Handle exception;
        }
    }

    class ConnectionHandler implements Runnable{
        Socket client;
        PrintWriter out;
        BufferedReader in;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a username: ");
                String username = in.readLine();
                out.println("welcome "+ username);
                out.println("Please enter a message: ");
                String message = in.readLine();
                System.out.println(message);
            } catch (IOException e) {
                // TODO: Handle exception
            }

        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void broadcastMessage(){

        }
    }

}
