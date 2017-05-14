/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*;

/**
 *
 * @author Kushal Mukherjee
 */
public class Server {

    static int port;

    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;// PORT SHOULD BE 8080 INCASE OF SAW
        }
        DatagramSocket socket = new DatagramSocket(port);
        System.out.println("\n*********************Up on Port=" + port + "*****************\n");

        /**
         * Stop and Wait following 3 lines.
         */
//        ServerSAW saw = new ServerSAW(socket);
//        saw.startServer();
//        socket.close();
        
        ServerGBN gbn=new ServerGBN(socket,port);
        gbn.startServer();
        socket.close();
    }

}
