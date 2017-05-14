
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Kushal Mukherjee
 */
public class ClientGBN {

    static int work = 0;
    static boolean isLast = false;
    static boolean sendLast = false;
    static int sizeOfFile = Constants.SIZE_OF_FILE;
    static int delay = Constants.DELAY;
    static Double LossRate = Constants.LOSS_RATE;
    static byte[][] fileBuffer = new byte[100][sizeOfFile];
    DatagramSocket senderSocket, recieverSocket;

    int clientPort;
    InetAddress clientAddress;
    byte[][] dataArray = new byte[50][];
    DatagramSocket clientSocket;
    static String fileName = "COPYRIGHT";
    static int sequenceNo = 0;

    int mod = Constants.WINDOW_SIZE - 1;

    public ClientGBN(int port, InetAddress address) {
        this.clientPort = port;
        this.clientAddress = address;
    }

    public void startClient() throws Exception {

        System.out.println("Enter Filename (N.B.:CHECK FOR PATH IN THE CODE):");
        Scanner in = new Scanner(System.in);
        senderSocket = new DatagramSocket();
        recieverSocket = new DatagramSocket(clientPort + 10);
        while (true) {
            if (work == 0) {
                fileName = in.next();
                System.out.println(fileName);
                System.out.println("Connection Established with:" + clientAddress + " Port:" + clientPort);

                //----------------SEND FILE REQUEST----------
                byte[] REQUEST = ("REQUEST " + fileName + " \n\r").getBytes();
                //byte[] REQUEST = ("REQUEST new 1.jpg \n\r").getBytes();

                System.out.println(new String(REQUEST));
                DatagramPacket sender = new DatagramPacket(REQUEST, REQUEST.length, clientAddress, clientPort);
                senderSocket.send(sender);
                System.out.println("sent at port=" + sender.getPort());

                work++;

            }

            if (work == 1) {

                //SEND PACKETS TO port
                //RECIEVE PACKETS FROM port+1
                try {
                    DatagramPacket reciever = new DatagramPacket(new byte[sizeOfFile + 20], sizeOfFile + 20);
                    recieverSocket.receive(reciever);
                    recieverSocket.setSoTimeout(delay + 5000);
                    byte[] data = reciever.getData();
                    byte[][] file = parsePacket(data);
                    String PckNo = new String(file[1]).trim();
                    if (sequenceNo == Integer.parseInt(PckNo)) {
                        System.out.println("From Server packet#" + PckNo.trim() + " Sent from server at:" + new String(file[2]));
                        writeData(file[3]);

                        String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
                        String d = "ACK " + sequenceNo + " " + timeStamp + " \n\r";
                        if (!TimedOut() || sequenceNo == 0) {
                            System.out.println("Acknowledgement sent for packet#" + sequenceNo + " at timestamp:" + timeStamp);
                            DatagramPacket sender = new DatagramPacket(d.getBytes(), d.length(), clientAddress, clientPort);
                            senderSocket.send(sender);
                        } else {
                            System.out.println("<SIMULATION> Ack was not sent for Packet:" + (sequenceNo));

                        }

                        sequenceNo++;
                    } else {
                        System.out.println("Duplicate data:"
                                + PckNo.trim() + ". Waiting for:" + (sequenceNo));
//                    String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
//                    String d = "ACK " + sequenceNo + " " + timeStamp + " \n\r";
//                    DatagramPacket sender = new DatagramPacket(d.getBytes(), d.length(), clientAddress, clientPort);
//                    senderSocket.send(sender);
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Timed Out.....Quiting......");
                    break;
                }

                System.out.println("----------------------------------");
//                try {
//                    DatagramPacket reciever = new DatagramPacket(new byte[sizeOfFile + 20], sizeOfFile + 20);
//                    recieverSocket.receive(reciever);
//                    recieverSocket.setSoTimeout(delay + 3000);
//                    byte[] data = reciever.getData();
//                    byte[][] file = parsePacket(data);
//
//                    String PckNo = new String(file[1]).trim();
//                    if (sequenceNo == Integer.parseInt(PckNo)) {
//                        System.out.println("From Server packet#" + PckNo.trim() + " Sent from server at:" + new String(file[2]));
//
//                        //dataArray[sequenceNo]=file[2];
//                        writeData(file[3]);
//                        String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
//                        String d = "ACK " + sequenceNo + " " + timeStamp + " \n\r";
//                        DatagramPacket sender = new DatagramPacket(d.getBytes(), d.length(), clientAddress, clientPort);
//                        if (!TimedOut() || sequenceNo == 0) {
//                            System.out.println("Acknowledgement sent for packet#" + sequenceNo + " at timestamp:" + timeStamp);
//                            clientSocket.send(sender);
//                        }
//                        sequenceNo++;
//                    } else {
//                        System.out.println("Duplicate data:"
//                                + PckNo.trim() + ". Waiting for:" + sequenceNo + "-------->RESENDING ACK:" + (sequenceNo - 1));
//                        String timeStamp = new SimpleDateFormat("HHmmssSSS").format(new Date());
//                        String d = "ACK " + (sequenceNo - 1) + " " + timeStamp + " \n\r";
//                        DatagramPacket sender = new DatagramPacket(d.getBytes(), d.length(), clientAddress, clientPort);
//                        clientSocket.send(sender);
//                    }
//
//                } catch (SocketTimeoutException e) {
//
//                    System.out.println("Timed Out...Quiting...");
//                    break;
//                }
            }

            Thread.sleep(delay);
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
        //SEQUENCE NO
        infoData[1] = Arrays.copyOfRange(data, 0, indexOf(data, space));
        data = Arrays.copyOfRange(data, indexOf(data, space) + 1, data.length);
        //TIMESTAMP
        infoData[2] = Arrays.copyOfRange(data, 0, indexOf(data, space));
        data = Arrays.copyOfRange(data, indexOf(data, space) + 1, data.length);

        int indexN = lastIndexOf(data, n);
        int indexR = lastIndexOf(data, r);

        if (indexN + 1 == indexR) {
            //DATA
            infoData[3] = Arrays.copyOfRange(data, 0, indexN - 1);
        }

        return infoData;
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

    private void writeData(byte[] file) {
        try {
            if (new String(file).trim().equals("END")) {

                System.out.println("I was here" + new String(file));

                return;

            } else {
                //------------------CHANGE WRITE PATH HERE --------------------------
                File f = new File("C://Users//Johnny 28//Desktop//project//my" + fileName);

                FileOutputStream fo;
                if (sequenceNo == 0) {
                    fo = new FileOutputStream(f, false);
                } else {
                    fo = new FileOutputStream(f, true);
                }
                try (BufferedOutputStream stream = new BufferedOutputStream(fo)) {

                    stream.write(file);
                    System.out.println("Data Written:" + sequenceNo + " Payload size:" + file.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } //        catch (InterruptedException ex) {
        //                Logger.getLogger(ClientSAW.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        catch (FileNotFoundException ex) {
            Logger.getLogger(ClientSAW.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private boolean TimedOut() {
        Random random = new Random();
        Double next = random.nextDouble();
        if (next > LossRate) {
            return false;
        } else {
            return true;
        }
    }

}
