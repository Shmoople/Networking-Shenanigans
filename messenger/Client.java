/*
    PROGRAM: Client side of message (connects to server)
    AUTHOR: Wyatt Rose
    DESCRIPTION: The client side of the program is used to establish connections with the server and communicate with other clients,
    this message receiver follows a 'star topology' as all trafic is routed through the server, including client to client requests.
    The 'Client' class is used in conjunction with the 'Server' class and the 'ClientRequest' class. This relationship is used in regular
    network communication. Since this kind of network could send any type of data, it could be implemented into a program with a GUI and
    possibily made into a multiplayer game. The java.net library has a ton of really cool features that can also be incorperated into the program,
    however I am currently working on a node.js implemntation which allows people to connect via a web browser. Overall node is a much better
    alternative to java when working with server side programming, so this program is just to show some cool concepts that I've picked up on. 
    Especially with concurrent programming (multiple threads of of execution).
*/
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

        // get the ip of the localhost, this should return 127.0.0.1
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
