import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private final ArrayList <ConnectionHandler> connectionHandlers = new ArrayList<>();
    private ServerSocket serverSocket;
    private ExecutorService pool;
    private boolean done = false;

    @Override
    public void run(){
        try {
            serverSocket= new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while(!done){
                Socket client = serverSocket.accept();
                ConnectionHandler connectionHandler = new ConnectionHandler(client);
                connectionHandlers.add(connectionHandler);
                pool.execute(connectionHandler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public void broadcastMessage(String message, ConnectionHandler sender){
        for(ConnectionHandler ch : connectionHandlers){
            if (ch != null && ch != sender){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown(){
        try {
            done=true;
            for(ConnectionHandler ch : connectionHandlers){
                ch.shutdown();
            }
            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }
    class ConnectionHandler implements Runnable{
        private Socket client;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a username: ");
                username = in.readLine();
                System.out.println(username+" connected!");
                broadcastMessage(username+" joined the chat", this);

                String message;
                while ((message = in.readLine()) != null){
                    if(message.equalsIgnoreCase("exit")){
                        broadcastMessage(username+" has left the chat", this);
                        shutdown();
                    } else {
                        broadcastMessage(username + ": " + message, this);
                    }
                    System.out.println(username+": "+message);
                }

            } catch (IOException e) {
                shutdown();
            }

        }

        public void sendMessage(String message){
            if(out !=null){
                out.println(message);
            }
        }

        public void shutdown(){
            try {
                if(in != null) in.close();
                if (out != null) out.close();
                if(client!=null && !client.isClosed()) client.close();
            } catch (IOException e) {
                //ignore
            }
        }

    }

    public static void main(String[] args){
        Server server = new Server();
        server.run();
    }
}
