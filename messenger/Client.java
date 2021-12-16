// Java implementation for multithreaded chat client
// Save file as Client.java

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client
{
    final static int ServerPort = Server.port;

    public static void main(String[] args) throws UnknownHostException, IOException
    {
        Scanner in = new Scanner(System.in);

        // getting localhost ip
        InetAddress ip = InetAddress.getByName("localhost");

        // establish the connection
        Socket s = new Socket(ip, ServerPort);

        // obtaining input and out streams
        DataInputStream input = new DataInputStream(s.getInputStream());
        DataOutputStream output = new DataOutputStream(s.getOutputStream());

        // sendMessage thread
        new Thread(()-> {
            while (true) {

                // read the message to deliver.
                String msg = in.nextLine();
                try {
                    // write on the output stream
                    output.writeUTF(msg);
                } catch (IOException e) {
                    // do nothing for now...
                }
            }
        }).start();

        // readMessage thread
        new Thread(()-> {
            while (true) {
                try {
                    // read the message sent to this client
                    String msg = input.readUTF();
                    System.out.println(msg);
                } catch (IOException e) {
                    // do nothing for now...
                }
            }
        }).start();

    }
}
