/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*;

/**
 *
 * @author Johnny
 */
public class Client {

    static String ip;
    static int port;
    static InetAddress address;

    public static void main(String[] args) throws Exception {

        if (args.length == 2) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
            address = InetAddress.getByName(ip);
        } else {
            port = 8080;
            address = InetAddress.getByName("localhost");
        }
        /**
         * Stop and Wait following 2 lines.
         */
//        ClientSAW saw = new ClientSAW(port, address);
//        saw.startClient();
        
        ClientGBN gbn=new ClientGBN(port, address);
        gbn.startClient();

    }

}
