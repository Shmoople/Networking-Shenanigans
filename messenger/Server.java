/*
    PROGRAM: Command-line messenger
    LAST EDIT: 12/15/21
    AUTHOR: Wyatt Rose
    DESCRIPTION: I was looking at some of the cool things to do in different programming languages, and I found this
    really interesting concept that most programming languages have, it's called concurrency. In short it's executing
    program statements in parallel, so you could have two statements being processed at the same time. This is really
    good for building network models (which is the field I would like to get into) so I tried making one from scratch in
    this program. Although it might not be a lot of code, it can be used to create some different network topologies in
    a single program.
*/

import java.io.*; // used for input output streams
import java.util.*; // used for basic utility
import java.net.*; // used for networking sockets

// server class represents the single server that exists in a network
public class Server {

    // port number for the server to be hosted on
    public static int port = 3000;

    // list to store all active requests on "localhost:3000"
    public static ArrayList<ClientRequest> requests = new ArrayList<ClientRequest>();

    // number of clients currently connected to the server
    public static int numClients = 0;

    // main will throw an IOException when the data-stream through the socket gets interrupted
    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(port);

        Socket socket;

        // think of this as the loop() in arduino, since initialization is complete, it's time to listen for requests
        while(true) {

            // when a request is sent from a client, the server dispatches a thread to handle it
            System.out.println("Waiting for request...");
            socket = serverSocket.accept();

            System.out.println("Client request received (usually connected) : " + socket);

            // a socket has input and output streams, they can be initialized like this:
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            System.out.println("Creating a new ClientRequest...");

            // create a new request from the socket
            ClientRequest request = new ClientRequest(socket,"user" + numClients, input, output);

            // add this request to the active requests list
            requests.add(request);

            // create new thread and start executing it (the instructions its executing are defined in ClientRequest.run())
            new Thread(request).start();

            // since we're done dispatching a thread, we can safely increment the number of clients and wait for another
            numClients++;

        }
    }
}

// the clientRequest class is instanciated in 'Server' when a new request from a client comes in
class ClientRequest implements Runnable {

    // the fields of this class represent different parts of a client request:
    private String name; // the name of the client (right now it can't be changed by the client, however this can be adjusted in the server class)
    private Socket socket; // the socket that the request is located on
    boolean isActive; // if the request is active (if the user is currently on localhost)
    public final DataInputStream input; // input stream from the socket
    public final DataOutputStream output; // output stream from the socket
    
    // constructor for ClientRequest, used to initialze all the fields
    public ClientRequest(Socket socket, String name, DataInputStream input, DataOutputStream output) {
        this.socket = socket;
        this.name = name;
        this.input = input;
        this.output = output;
        this.isActive = true; // if a clientRequest is constructed we set 'isActive' to true, since the client couldn't have disconnected yet
    }

    // all classes that implement a runnable interface _have_ to define a 'run()' method
    @Override
    public void run() {
        
        // the current string that is received from the client
        String inputString;
        
        while(true) {

            try {
                
                // get the input string from the client via the socket's data-stream
                inputString = input.readUTF();
                
                // print out the input-string from the client (this goes directly to the server cmd prompt)
                System.out.println(inputString);
                
                // this is where different operations can occur based on the string sent to the servr, this is a simple 'exit' operation
                if(inputString.equals("exit")) {
                    this.isActive = false; // although the clientrequest will still be in the list, it will be set to inactive
                    this.socket.close(); // terminate the connection
                    break; // break to close the datastreams
                }

                /*
                The following code let's the user define a message and a recipient, this is done through a request to the server that looks
                like this: "message#user", all usernames are given by default in the order they connect to the server, so the first person
                to connect will be "user0". Since I won't be able to show this working in school (due to the restricted networks) I can still
                send a message to myself.
                */
                
                // A string tokenizer divides strings based on a defined token, the token is called a delimiter
                StringTokenizer rawMessage = new StringTokenizer(inputString, "#");

                // get the first half of the message (everything before the delimiter)
                String message = rawMessage.nextToken();

                // get the second half of the message (everything after the delimiter)
                String recipient = rawMessage.nextToken();

                // search for the recipient in the connected devices list
                // clientRequests is what stores all of the active clients'
                for(ClientRequest cr : Server.requests) { // loop through all the requests
                    if(cr.name.equals(recipient) && cr.isActive) { // check if the current client in the list matches the username
                        cr.output.writeUTF(this.name+" : "+message); // if the recipient matches, send the message to them via their output stream
                        break; // break since the requested client has been found!
                    }
                }

            } catch(IOException e) {
                // do nothing, could print to stack trace...
            }
        }

        System.out.println("Closing request on: "+name);

        // close data-streams opened by the client before falling out of scope
        try {
            this.input.close();
            this.output.close();
        } catch(IOException e) {
            // do nothing
        }
    }
};
