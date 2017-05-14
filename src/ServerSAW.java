/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Kushal Mukherjee
 */
public class ServerSAW {

    final int DELAY = Constants.DELAY;
    final Double LossRate=Constants.LOSS_RATE;
    static int sizeOfFile = Constants.SIZE_OF_FILE;
    static byte[][] fileBuffer = new byte[100][sizeOfFile];
    static int fileIndex;
    DatagramSocket serverSocket;
    static String fileName;
    static int work = 0;
    static boolean isLast=false;
    static boolean sendLast=false;
    static int pckToBeSent=0;

    public ServerSAW(DatagramSocket serverSocket) {

        this.serverSocket = serverSocket;
    }

    //SCANNING THE WHOLE FILE
//     public static void scanFile(File f) throws IOException {
//
//     try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f))) {
//     int tmp = 0;
//     while ((tmp = bis.read(fileBuffer[fileIndex++])) > 0) {
//     }
//
//     }
//     }
     
    public void startServer() throws Exception {

        int sequenceNo = 0;
        InetAddress clientAdd = null;
        int clientPort = 0;
        boolean canSend = true;

        while (true) {
            if (work == 0) {
                
                //-----------RECIEVE FILE REQUEST---------
                DatagramPacket reciever = new DatagramPacket(new byte[1024], 1024);
                serverSocket.receive(reciever);
                System.out.println("recieved");
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
                try {
                    byte[] send = makePacketByte(sequenceNo,false);
                    DatagramPacket sender = new DatagramPacket(send, send.length, clientAdd, clientPort);

                    if ((!TimedOut() && canSend == true) || pckToBeSent!=0) {

                        //---------SENDING PACKET---------                    
                        serverSocket.send(sender);
                        System.out.println("Packet no#" + sequenceNo);
                        
                    }
                    
                    DatagramPacket reciever = new DatagramPacket(new byte[1024], 1024);
                    serverSocket.receive(reciever);
                    serverSocket.setSoTimeout(DELAY+300);
                    String d = new String(reciever.getData());
                    String[] s=d.split("\\s+");
                    
                    System.out.println("Acknowledgement from client recieved for packet:" + s[1]+" Sent from client at time:"+s[2]);
                    canSend=true;
                    
                    if(sequenceNo==0){
                        sequenceNo=1;
                    }else{
                        sequenceNo=0;
                    }
                    
                    //sequenceNo++;

                   
                } catch (SocketTimeoutException e) {
                    System.out.println("Acknowledgement for packet#" + sequenceNo + " was NOT recieved");
                    byte[] send = makePacketByte(sequenceNo,true);
                    DatagramPacket sender = new DatagramPacket(send, send.length, clientAdd, clientPort);
                    serverSocket.send(sender);
                    System.out.println("Packet ReSend #" + sequenceNo);
                    canSend = false;

                }
            }
            if (work > 1) {
                System.out.println("All sent .. Quiting ...");
                break;
            }

            System.out.println("----------------------------------------");
            Thread.sleep(DELAY);
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

    private boolean TimedOut() {
        Random random = new Random();
        Double next = random.nextDouble();
        if (next > LossRate) {
            return false;
        } else {
            System.out.println("<SIMULATION> Packet was not sent");
            return true;
        }
    }

    private byte[] makePacketByte(int sequenceNo,boolean resend) {
        
        
//        String time="TEST";
        String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
//        byte[] test=timeStamp.getBytes();
        System.out.println("Packet wat sent at TIME:"+timeStamp);
        byte[] prefix = ("RDT " + sequenceNo + " "+timeStamp+" ").getBytes();
        byte[] suffix;
        
        if (sendLast) {
            //System.out.println("Last sent");
            suffix = (" END \n\r").getBytes();
            work++;
        }
        else {
            suffix = (" \n\r").getBytes();
        }        
        if(isLast){
            sendLast=true;
            //System.out.println("Send Last true");
        }
        //byte[] dataByte = fileBuffer[sequenceNo];//to be seen ---------------<

        byte[] dataByte = readFile(pckToBeSent, work);
        
       
        byte[] temp = new byte[prefix.length + dataByte.length + suffix.length];

        System.arraycopy(prefix, 0, temp, 0, prefix.length);
        System.arraycopy(dataByte, 0, temp, prefix.length, dataByte.length);
        System.arraycopy(suffix, 0, temp, prefix.length + dataByte.length, suffix.length);

        if(!resend){
            pckToBeSent++;
        }
        
        return temp;
    }

    private byte[] readFile(int sequenceNo, int work) {
        //------------------------CHANGE READ PATH HERE-------------------------
        File f = new File("C://Users//Johnny 28//Desktop//project//" + fileName);
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(f))) {
            stream.skip(sequenceNo * sizeOfFile);
            if(stream.available()>sizeOfFile){
                stream.read(fileBuffer[1]);                
            }
            else if(stream.available() <sizeOfFile) {
                    System.out.println("Last packet: " + stream.available());
                    fileBuffer[1] = new byte[stream.available()];
                    stream.read(fileBuffer[1], 0, stream.available());
                    isLast = true;
                }
            
            //System.out.println(new String(fileBuffer[1]));
        } catch (IOException e) {
            System.out.println("ENTER VALID FILE NAME");
            System.exit(0);
        } 
        return fileBuffer[1];
    }

}
