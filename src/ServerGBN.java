/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.*;
import java.util.*;
import javax.sound.midi.Receiver;

/**
 *
 * @author Johnny 28
 */
public class ServerGBN {

    DatagramSocket socket;

    static int work = 0;
    static String fileName;
    int port;

    public ServerGBN(DatagramSocket socket,int port) {
        this.socket = socket;
        this.port=port;
    }

    public void startServer() throws Exception {

        int sequenceNo = 0;
        InetAddress clientAdd = null;
        int clientPort = 0;
        boolean canSend = true;

        if (work == 0) {

            //-----------RECIEVE FILE REQUEST---------
            DatagramPacket reciever = new DatagramPacket(new byte[1024], 1024);
            socket.receive(reciever);
            System.out.println("recieved at port="+reciever.getPort());
            clientAdd = reciever.getAddress();
            clientPort = reciever.getPort();
            byte[] data = reciever.getData();
            System.out.println(new String(data));
            byte[][] parsedData = parsePacket(data);
            if (new String(parsedData[0]).equals("REQUEST")) {
                System.out.println(new String(parsedData[1]));
                fileName = new String(parsedData[1]);
                //scanFile(new File("C://Users//Johnny 28//Desktop//project//" + fileName));
            }
            work++;
            
        }

        if (work == 1) {
            ThreadHandlerGBN st=new ThreadHandlerGBN(clientAdd,clientPort,fileName,socket,port);
            st.run();
            
            
        }

    }

    private byte[][] parsePacket(byte[] data) {

        byte[][] infoData = new byte[4][];
        byte space = " ".getBytes()[0];
        byte n = "\n".getBytes()[0];
        byte r = "\r".getBytes()[0];

        //RDT
        infoData[0] = Arrays.copyOfRange(data, 0, indexOf(data, space));
        data = Arrays.copyOfRange(data, indexOf(data, space) + 1, data.length);
        System.out.println(new String(data));
        //SEQUENCE NO
//        infoData[1] = Arrays.copyOfRange(data, 0, indexOf(data, space));
//        data = Arrays.copyOfRange(data, indexOf(data, space) + 1, data.length);

        int indexN = lastIndexOf(data, n);
        int indexR = lastIndexOf(data, r);

        if (indexN + 1 == indexR) {
            //DATA
            infoData[1] = Arrays.copyOfRange(data, 0, indexN - 1);
        }

        return infoData;
    }

    private int lastIndexOf(byte[] array, byte search) {
        int index = -1;
        for (int i = array.length - 1; i > 0; i--) {
            if (array[i] == search) {
                index = i;
                break;
            }
        }
        return index;
    }

    private int indexOf(byte[] array, byte search) {
        int index = -1;
        for (int i = 0; i < array.length; i++) {
            if (array[i] == search) {
                index = i;
                break;
            }
        }
        return index;
    }
}
