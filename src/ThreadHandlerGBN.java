
import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Johnny 28
 */
public class ThreadHandlerGBN {

    int port;
    static int work = 0;
    static boolean isLast = false;
    static boolean sendLast = false;
    static int sizeOfFile = Constants.SIZE_OF_FILE;
    static int delay = Constants.DELAY;
    static Double LossRate = Constants.LOSS_RATE;
    static byte[][] fileBuffer = new byte[100][sizeOfFile];

    InetAddress clientAdd;
    int clientPort;
    String fileName;
    final SharedGBN shared = new SharedGBN();
    boolean timerRunning = false;
    int mod=Constants.WINDOW_SIZE-1;
    static int timerRunningOn;

    DatagramSocket recieveSocket;
    DatagramSocket senderSocket;

    static Timer timer;

    ThreadHandlerGBN(InetAddress clientAdd, int clientPort, String fileName, DatagramSocket socket, int port) {
        this.clientAdd = clientAdd;
        this.clientPort = clientPort;
        this.fileName = fileName;
        this.port = port;
    }

    public void run() {

        send st = new send();
        recieve rt = new recieve();
        st.start();
        rt.start();

//        byte[] sent="DUMMY".getBytes();
//        DatagramPacket sender=new DatagramPacket(sent, sent.length,clientAdd,port+10);
//        senderSocket.send(sender);
//        
//        DatagramPacket rec=new DatagramPacket(new byte[1024], 1024);
//        recieveSocket.receive(rec);
//        System.out.println(new String(rec.getData()));
    }

    private byte[] makePacketByte(int sequenceNo) {
//        String time="TEST";
        String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
//        byte[] test=timeStamp.getBytes();
        System.out.println("Packet#" + sequenceNo + " was sent at TIME:" + timeStamp);
        byte[] prefix = ("RDT " + sequenceNo + " " + timeStamp + " ").getBytes();
        byte[] suffix;

        if (sendLast) {
            //System.out.println("Last sent");
            suffix = (" END \n\r").getBytes();
            work++;
        } else {
            suffix = (" \n\r").getBytes();
        }
        if (isLast) {
            sendLast = true;
            //System.out.println("Send Last true");
        }
        //byte[] dataByte = fileBuffer[sequenceNo];//to be seen ---------------<

        byte[] dataByte = readFile(sequenceNo, work);

        byte[] temp = new byte[prefix.length + dataByte.length + suffix.length];

        System.arraycopy(prefix, 0, temp, 0, prefix.length);
        System.arraycopy(dataByte, 0, temp, prefix.length, dataByte.length);
        System.arraycopy(suffix, 0, temp, prefix.length + dataByte.length, suffix.length);

        return temp;
    }

    private byte[] readFile(int sequenceNo, int work) {
        //------------------------CHANGE READ PATH HERE-------------------------
        File f = new File("C://Users//Johnny 28//Desktop//project//" + fileName);
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(f))) {
            stream.skip(sequenceNo * sizeOfFile);
            if (stream.available() > sizeOfFile) {
                stream.read(fileBuffer[1]);
            } else if (stream.available() < sizeOfFile) {
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

    class send extends Thread {

        public void run() {

//            try {
//                senderSocket = new DatagramSocket();
//                byte[] sent = "DUMMY".getBytes();
//                DatagramPacket sender = new DatagramPacket(sent, sent.length, clientAdd, port + 10);
//
//                senderSocket.send(sender);
//            } catch (IOException ex) {
//                Logger.getLogger(ThreadHandlerGBN.class.getName()).log(Level.SEVERE, null, ex);
//            }
            try {
                senderSocket = new DatagramSocket();
                while (shared.canSend && work == 0) {

                    if (shared.sn - shared.sf >= shared.sw) {
                        continue;
                    } else {
                        //SEND DATA
                        byte[] send = makePacketByte(shared.sn);
                        DatagramPacket sender = new DatagramPacket(send, send.length, clientAdd, port + 10);
                        senderSocket.send(sender);
                        System.out.println("SENDER THREAD: Packet #" + shared.sn + " Sent");
                        if (!timerRunning) {
                            sendTimeout(delay + 300,shared.sn);
                        }

                        shared.sn++;
                    }
                    System.out.println("----------------------------------------^SENDER THREAD");
                    Thread.sleep((long) (delay*0.1));
                }
                System.out.println("All sent.......Quiting Sender Thread");

            } catch (InterruptedException ex) {
                Logger.getLogger(send.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ThreadHandlerGBN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    class recieve extends Thread {

        public void run() {
            int counter = 0;
//            try {
//                System.out.println("Port:" + port);
//                recieveSocket = new DatagramSocket(port);
//                DatagramPacket rec = new DatagramPacket(new byte[1024], 1024);
//
//                recieveSocket.receive(rec);
//                System.out.println(new String(rec.getData()));
//            } catch (IOException ex) {
//                Logger.getLogger(ThreadHandlerGBN.class.getName()).log(Level.SEVERE, null, ex);
//            }
            try {
                recieveSocket = new DatagramSocket(port);
                while (true) {
                    try {
                        DatagramPacket receiver = new DatagramPacket(new byte[1024], 1024);
                        recieveSocket.receive(receiver);
                        recieveSocket.setSoTimeout(delay + 300);
                        String d = new String(receiver.getData());
                        String[] s = d.split("\\s+");

                        System.out.println("RECIEVER THREAD: Acknowledgement from client recieved for packet:" + s[1] + " Sent from client at time:" + s[2]);
                        timer.cancel();
                        timerRunning = false;
                        System.out.println("Timer stopped for :"+timerRunningOn);
                        
                        shared.sf=(Integer.parseInt(s[1])+1);
                        System.out.println("----------------------------------------^RECIVER THREAD");
                        Thread.sleep(delay);

                    } catch (SocketTimeoutException e) {
                        System.out.println("RECIEVER THREAD: Waiting for AckNo#" + shared.sf);                        
                        counter++;
                    }
                    if(counter==10){
                        shared.sf++;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(recieve.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ThreadHandlerGBN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void sendTimeout(int d,int running) {
        System.out.println("Timer Started for:"+running);
        timerRunningOn=running;
        timerRunning = true;
        timer = new Timer();
        timer.schedule(new Task(), (long) (d));
    }

    private class Task extends TimerTask {

        @Override
        public void run() {
            
            System.out.println("Timer ran out for window: "+shared.sf+"-"+(shared.sf+shared.sw-1));
            
            int temp = shared.sf;
            while (temp < shared.sn) {
                try {

                    byte[] send = makePacketByte(temp);
                    DatagramPacket sender = new DatagramPacket(send, send.length, clientAdd, port + 10);
                    senderSocket.send(sender);
                    System.out.println("TIMER THREAD: Packet #" + temp + " Sent");
                    temp++;

                    System.out.println("Im here once temp=" + temp + "  Sn=" + shared.sn);
                    //shared.sn--;
                    
                    //Thread.sleep(90);
                } catch (IOException ex) {
                    Logger.getLogger(ThreadHandlerGBN.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.out.println("----------------------------------------^TIMER THREAD");
            timerRunning = false;
        }
    }

}
