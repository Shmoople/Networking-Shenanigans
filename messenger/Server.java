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

class ClientRequest implements Runnable {

    private Scanner scanner;
    private String name;
    private Socket socket;
    boolean isActive;
    public final DataInputStream input;
    public final DataOutputStream output;

    public ClientRequest(Socket socket, String name, DataInputStream input, DataOutputStream output) {
        this.socket = socket;
        this.name = name;
        this.input = input;
        this.output = output;
        this.isActive = true;
    }

    @Override
    public void run() {

        String inputString;

        while(true) {

            try {

                inputString = input.readUTF();

                System.out.println(inputString);

                if(inputString.equals("exit")) {
                    this.isActive = false;
                    this.socket.close();
                    break;
                }

                StringTokenizer rawMessage = new StringTokenizer(inputString, "#");

                // get the first half of the message (everything before the delimiter)
                String message = rawMessage.nextToken();

                // get the second half of the message (everything after the delimiter)
                String recipient = rawMessage.nextToken();

                // search for the recipient in the connected devices list
                // clientRequests is what stores all of the active clients'
                for(ClientRequest cr : Server.requests) {
                    if(cr.name.equals(recipient) && cr.isActive) {
                        cr.output.writeUTF(this.name+" : "+message);
                        break; // break since messages have been sent
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
