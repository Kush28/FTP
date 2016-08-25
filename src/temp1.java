
import java.io.IOException;
import java.net.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Johnny 28
 */
public class temp1 {
    public static void main(String[] args) throws SocketException, IOException {
        InetAddress add=InetAddress.getByName("localhost");
        DatagramSocket socket=new DatagramSocket(1234);
        DatagramSocket send=new DatagramSocket();
        DatagramPacket packet=new DatagramPacket(new byte[1024], 1024);
        socket.receive(packet);
        System.out.println("REC...");
        System.out.println(new String(packet.getData()));
        byte[] data="ACK".getBytes();
        DatagramPacket sender=new DatagramPacket(data,data.length,add,8080);
        send.send(sender);
    }
    
}
