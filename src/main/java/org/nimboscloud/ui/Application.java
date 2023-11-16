package org.nimboscloud.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Scanner;

public class Application {

    private final String serverAddress;
    private final int port;
    private final Socket clientSocket;

    //private final TaggedConnection connection;
    //private final AuthenticationManagerStub authManager;
    //private final NotificationManagerStub notificationManager;

    public Application(String serverAddress, int port) throws IOException {

        this.serverAddress = serverAddress;
        this.port = port;

        this.clientSocket = new Socket(serverAddress, port);
        //this.connection = new TaggedConnection(clientSocket);
        //this.demultiplexer = new Demultiplexer(connection);

        //this.authManager = new AuthenticationManagerStub(connection, demultiplexer);
        //this.notificationManager = new NotificationManagerStub(connection, demultiplexer);

        //demultiplexer.start();
        // new Thread(new NotificationListener(demultiplexer)).start();
    }

    public static void main(String[] args) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        Application app = new Application("localhost", 12345);
        app.run();
    }

    public void run() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Scanner userInput = new Scanner(System.in);
        Processor proc = new Processor();

        while (true) {

            System.out.print("nimboscloud> ");
            String userCommand = userInput.nextLine();
            proc.process(userCommand);
        }

    }

}
