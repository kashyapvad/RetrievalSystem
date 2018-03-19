package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws Exception {
        //Utilities.createDocs("./time/TIME.txt");  // Creates separate file for each document in the TIME.ALL file
        //Utilities.invertedIndex("./DocumentFiles"); // Creates the inverted index and then write it to a file
        Utilities.getInvertedIndex("./invertedIndex.txt"); // retrieves the inverted index from the file which has been
                                                                    // created by the above Utilities.invertedIndex method

        //Utilities.relevanceAssesment();
        int q_len = 6;
        int port = 3000;
        Socket sock;

        //new SecondaryServer().start();

        ServerSocket servsock = new ServerSocket(port, q_len); /* Instantiantes a server socket object which
                                                                  opens a connection so that clients can connect to it*/

        System.out.println("Retrieval System is starting up, listening at port 3000.\n");
        while (true) {
            sock = servsock.accept(); /* It stores whatever information received from client in the sock object*/
            new Worker(sock).start(); /* starts the worker thread and it passes the information
                                         from client to worker's run method using sock object*/
        }

    }


}



class Worker extends Thread {
    Socket sock;

    Worker(Socket s) {
        sock = s;
    }

    public void run() {
        PrintStream out = null;
        BufferedReader in = null;

        try {
            in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));
            out = new PrintStream(sock.getOutputStream());

            try {
                String request;
                request = in.readLine();
                System.out.println(request);
                Utilities.showPage(request, out);
            } catch (IOException e) {
                System.out.println("Server read error");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            sock.close();
        } catch (IOException e) {
            System.out.println("IO exception");
        }
    }

}








