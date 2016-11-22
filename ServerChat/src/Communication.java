
/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
//<editor-fold defaultstate="collapsed" desc="Imports">
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
//</editor-fold>

public class Communication extends Thread {

    static String serverFileDirectory = System.getProperty("user.dir") + "\\";
    static int fileSizeLimit = 1024;
    Socket client;

    //<editor-fold defaultstate="collapsed" desc="Constructor">
    Communication(Socket client) {
        this.client = client;
    }
    //</editor-fold>

    @Override
    public void run() {
        try {
            //<editor-fold defaultstate="collapsed" desc="Initialize variables">
            ObjectOutputStream WriteObj = new ObjectOutputStream(client.getOutputStream());
            WriteObj.flush();
            ObjectInputStream ReadObj = new ObjectInputStream(client.getInputStream());
            OutputStream os = client.getOutputStream();
            InputStream is = client.getInputStream();

            Object fromClient;
            boolean communicationFlag;
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Handshake">
            if (!ReadObj.readObject().equals("InitiateConnection")) {
                throw new CustomConnectionException("InitiateConnection");
            }
            WriteObj.writeObject("ConnectionInitiated");
            Print.printMsg("ConnectionInitiated");
            //</editor-fold>

            do {
                //<editor-fold defaultstate="collapsed" desc="Check client's request">
                fromClient = ReadObj.readObject();
                //<editor-fold defaultstate="collapsed" desc="Exchange file">
                if (fromClient.equals("FileExchange")) {
                    WriteObj.writeObject("FileExchangeInitiated");
                    Print.printMsg("FileExchangeInitiated");

                    fromClient = ReadObj.readObject();
                    //<editor-fold defaultstate="collapsed" desc="Receive file from client">
                    if (fromClient.equals("ClientSendsFile")) {
                        WriteObj.writeObject("ClientSendsFileInitiated");
                        Print.printMsg("ClientSendsFileInitiated");

                        //<editor-fold defaultstate="collapsed" desc="Declare and initialize ">
                        String filename = ReadObj.readObject().toString();
                        Print.printMsg(filename);
                        int totalBytes = (int) ReadObj.readObject();
                        byte[] buffer = new byte[client.getReceiveBufferSize()];
                        int bytesReceived = 0;

                        InputStream input = client.getInputStream();
                        FileOutputStream wr = new FileOutputStream(new File(serverFileDirectory + filename));
                        //</editor-fold>

                        //<editor-fold defaultstate="collapsed" desc="Receive and save file">
                        while (totalBytes > 0) {
                            if ((bytesReceived = input.read(buffer)) <= 0) {
                                break;
                            }
                            wr.write(buffer, 0, bytesReceived);
                            totalBytes -= bytesReceived;
                            //Print.printMsg(totalBytes);
                        }
                        wr.close();
                        //</editor-fold>
                        Print.printMsg("File saved to " + serverFileDirectory + filename);
                    } //</editor-fold>
                    //<editor-fold defaultstate="collapsed" desc="Send file to client">
                    else if (fromClient.equals("ClientReceivesFile")) {
                        Print.printMsg("ClientReceivesFile");

                        String Filename = ReadObj.readObject().toString();

                        //<editor-fold defaultstate="collapsed" desc="Load file and send">
                        String filepath = serverFileDirectory + Filename;//Get file path from gui
                        File myFile = new File(filepath);
                        WriteObj.writeObject((int) myFile.length());
                        byte[] filebytearray = new byte[(int) myFile.length()];
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                        bis.read(filebytearray, 0, filebytearray.length);

                        os.write(filebytearray, 0, filebytearray.length);
                        os.flush();
                        bis.close();
                        Print.printMsg("File send");
                        //</editor-fold>
                        //Add functionality
                    } //</editor-fold>
                    else {
                        throw new CustomConnectionException("ClientSendsFile or ClientReceivesFile");
                    }
                    /*Exchange files*//*End*/

                } //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="Exchange message">
                else if (fromClient.equals("MessageExchange")) {
                    WriteObj.writeObject("MessageExchangeInitiated");
                    Print.printMsg("MessageExchangeInitiated");
                    String message = ReadObj.readObject().toString();
                    Print.printMsg("Message:'" + message + "' Received from client.");

                    WriteObj.writeObject("I am the server. Did you say " + message + "?");
                    Print.printMsg("Send response string");
                } //</editor-fold>
                //<editor-fold defaultstate="collapsed" desc="Check for error">
                else {
                    throw new CustomConnectionException("FileExchange or MessageExchange");
                }
                //</editor-fold>
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Continue or terminate communication">
                String communication = ReadObj.readObject().toString();
                if (communication.compareTo("ContinueCommunication") == 0) {
                    Print.printMsg("ContinueCommunication");
                    communicationFlag = true;
                } else if (communication.compareTo("TerminateCommunication") == 0) {
                    Print.printMsg("TerminateCommunication");
                    communicationFlag = false;
                    client.close();
                } else {
                    throw new CustomConnectionException("ContinueCommunication or TerminateCommunication");
                }
                //</editor-fold>
            } while (communicationFlag == true);

        } //<editor-fold defaultstate="collapsed" desc="Catch exceptions"> //<editor-fold defaultstate="collapsed" desc="Catch exceptions">
        catch (IOException | ClassNotFoundException | CustomConnectionException ex) {
            //Logger.getLogger(ServerChat.class.getName()).log(Level.SEVERE, null, ex);
            Print.printMsg("Client Disconnected.");
        }
        //</editor-fold>

    }

}
