import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    BufferedReader in;
    PrintWriter out;
    Socket client;
    private boolean done = false;
    @Override
    public void run(){
        try {
            client = new Socket("localhost", 9999);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            InputHandler inputHandler = new InputHandler();
            Thread InputThread = new Thread(inputHandler);
            InputThread.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null) {
                System.out.println("Server: " + inMessage);
            }

        } catch (IOException e) {
            shutdown();
        }
    }
    public void shutdown(){
        done = true;
        try {
            if(in != null) in.close();
            if(out != null) out.close();
            if(client != null && !client.isClosed()) client.close();
        } catch (IOException e) {
            //Ignore
        }
    }

    class InputHandler implements Runnable{
        @Override
        public void run() {
            //it reads from the client's console input
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String consoleInput = inReader.readLine();
                    if(consoleInput.equalsIgnoreCase("exit")){
                        System.out.println("Exiting...");
                        inReader.close();
                        shutdown();
                    } else {
                        out.println(consoleInput);
                    }
                }
            } catch (IOException e) {
                shutdown();
            }

        }
    }
}
