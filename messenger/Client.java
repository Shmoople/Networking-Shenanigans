// check this for a reference: https://www.geeksforgeeks.org/multi-threaded-chat-application-set-2/?ref=lbp
// Java implementation for multithreaded chat client
// Save file as Client.java

import java.io.*; // used for data streams
import java.net.*; // used for sockets and IP address
import java.util.Scanner; // used for command line input

// client class should be run after server, that way a connection can be established (this version of client doesn't wait)
public class Client
{
    // the current port that the server is on, since this is all done on the localhost it makes it a lot easier
    final static int ServerPort = Server.port;

    // main method is called in parallel with the 'Server' main method
    public static void main(String[] args) throws UnknownHostException, IOException
    {
        // initialize a new scanner for command-line input
        Scanner in = new Scanner(System.in);

        // get the ip of the localhost, this usually defaults to 127.0.0.1
        InetAddress ip = InetAddress.getByName("localhost");

        // start a new connection on the specified port (in this case the port is defined in 'Server'
        Socket s = new Socket(ip, ServerPort);

        // get the input and output streams from the new connection (sockets return these streams)
        DataInputStream input = new DataInputStream(s.getInputStream());
        DataOutputStream output = new DataOutputStream(s.getOutputStream());

        // sendMessage thread, (this thread is polling at the same time as the readMessage thread)
        new Thread(()-> {
            while (true) {

                // read input from the command-line, this is the input from the client-side
                String msg = in.nextLine();
                try {
                    // write the message to the output stream, if the connection gets disrupted the program terminates
                    output.writeUTF(msg);
                } catch (IOException e) {
                    // unhandled exception, could add recovery later
                }
            }
        }).start(); // start the thread

        // readMessage thread
        new Thread(()-> {
            while (true) {
                try {
                    // read the messages from the input stream
                    String msg = input.readUTF();
                    
                    // display the message
                    System.out.println(msg);
                } catch (IOException e) {
                    // do nothing for now...
                }
            }
        }).start(); // start the thread
    }
}
