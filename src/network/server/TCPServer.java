package network.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import network.NetworkCodes;
import network.NetworkMaster;


public class TCPServer {

    private class DealWithConnection implements Runnable{

        Socket myClientSocket;
        
        public DealWithConnection(Socket clientSocket){
            myClientSocket = clientSocket;
        }
        
        @Override
        public void run () {
            
            try{
                ObjectInputStream in = new ObjectInputStream(myClientSocket.getInputStream());
                String inType = (String) in.readObject();
                Object inObj = in.readObject();
                dealWithObjectReceived(inType, inObj);
                in.close();
                
//                ObjectOutputStream out = new ObjectOutputStream(myClientSocket.getOutputStream());
//                out.writeObject("Hi Client, this is server. Your information has been received");
//                out.flush();
//                out.close();

                myClientSocket.close();
                
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        
    }
    
    private static int PORT;
    private static final String DEFAULT_RECEIVED_FILE = System.getProperty("user.dir") +
                                                        File.separator
                                                        + "src" + File.separator + "network" +
                                                        File.separator + "server" + File.separator +
                                                        "ReceivedFile.txt";
    private Object receivedObj = null;
    private String receivedFile = null;
    
    private NetworkMaster myNetwork;
    
    public TCPServer(int port){
        PORT = port;
    }

    public void registerNetwork(NetworkMaster n){
        myNetwork = n;
    }
    
    @SuppressWarnings("resource")
    public void runServer () {
        try {

            ServerSocket serverS = new ServerSocket(PORT, 10);

            while (true) {

                Socket clientSocket = serverS.accept();

                new Thread(new DealWithConnection(clientSocket)).start();
                
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    private void dealWithObjectReceived (String inType, Object inObj) {
        if (inType.equals("textfile")) {
            writeReceivedFile(inObj);
        }
        else {
            Class c = null;
            try {
                c = Class.forName(inType);
            }
            catch (ClassNotFoundException e) {
                System.out.println("Client's object type is not found...");
                return;
            }
            receivedObj = c.cast(inObj);
//            System.out.println("I received object \"" + c.cast(inObj) + "\" from the client!");

            // do whatever you want to do with the objects here
            
            // register the node into myNetwork if the action code matches
            if (inType.equals("java.lang.String")){
                String s = (String) receivedObj;
                if (s.startsWith(Integer.toString(NetworkCodes.JOIN))){ // what if a word starts with this??
                    String[] splits = s.split(" ");
                    myNetwork.register(splits[1], splits[2]);
                    System.out.printf("Registered peer ip: %s, port: %s, into the network!\n", splits[1], splits[2]);
                } else {
                    System.out.println("Received word count word: " + s);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeReceivedFile (Object inObj) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(DEFAULT_RECEIVED_FILE));

            List<String> fileLines = (List<String>) inObj;
            for (String s : fileLines) {
                out.write(s + "\n");
            }

            out.close();
            receivedFile = DEFAULT_RECEIVED_FILE;
        }
        catch (Exception e) {
            System.out.println("Error reading client's file input or writing it to a file...");
            return;
        }

        System.out.println("I received file \"" + DEFAULT_RECEIVED_FILE + "\" from the client!");
    }

    public Object getMostRecentObject () {
        return receivedObj;
    }

    public String getMostRecentFileName () {
        return receivedFile;
    }
}
