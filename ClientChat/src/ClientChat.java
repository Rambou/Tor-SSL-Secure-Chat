
/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
//<editor-fold defaultstate="collapsed" desc="Imports">
import java.net.*;
import java.io.*;
import java.util.*;
import java.net.InetAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
//</editor-fold>

public class ClientChat {

    //<editor-fold defaultstate="collapsed" desc="Declare variables">
    ObjectOutputStream OutStream;
    ObjectInputStream InStream;
    Scanner InputStream = new Scanner(System.in);
    Socket myclient;
    Socket TorSocket;

    boolean communicationFlag = false;
    boolean Tor = false;

    OutputStream os = null;
    InputStream is = null;

    String clientFilesDirectory = System.getProperty("user.dir") + "\\";
    int fileSizeLimit = 1024;
    int PlainPort = 5555;
    int SSLPort = 5556;
    int TorPort = 9050;
    int Port;
    GUI mygui;
    String torhostname;

    public static enum Select {

        Plain, SSl
    };
    InetAddress ip;
    Select sl;
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructor">
    ClientChat(GUI gui, Select selection, boolean _Tor, String _ip) throws UnknownHostException {
        mygui = gui;
        sl = selection;
        if (!_Tor) {
            ip = InetAddress.getByName(_ip);
        }
        torhostname = _ip;
        Tor = _Tor;
    }
//</editor-fold>

    public boolean Connect() {
        try {

            //<editor-fold defaultstate="collapsed" desc="Connect to server">
            //<editor-fold defaultstate="collapsed" desc="Plain">
            if (sl == Select.Plain) {

                if (Tor) {
                    mygui.printMsg("[Debug] " + "Starting unencrypted connection through Tor Network");
                    myclient = TorLib.TorSocket(torhostname, PlainPort);
                } else {
                    mygui.printMsg("[Debug] " + "Starting unencrypted connection");
                    myclient = new Socket();
                    myclient.connect(new InetSocketAddress(ip, PlainPort));
                }

                Port = PlainPort;
            }//</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="SSL">
            else if (Select.SSl == sl) {

                //<editor-fold defaultstate="collapsed" desc="Declare and initialize variables">
                //String CertificatesFileDirectory = clientFilesDirectory.replace(clientFilesDirectory.split("\\\\")[clientFilesDirectory.split("\\\\").length - 1] + "\\", "") + "Certificates\\";
                String CertificatesFileDirectory = System.getProperty("user.dir") + "\\";
                //String AegeanSigned = CertificatesFileDirectory + "Aegean Signed\\";

                char[] trustpass = "aegean".toCharArray();
                char[] keypass = "aegean".toCharArray();
                String trustStoreName = "CA_Keystore.jks";
                String keyStoreName = "Client_Keystore.jks";
                String encryptionProtocol = "SSLv3";
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Initialize SSL context(keymanager and trustmanager)">
                SSLContext context = InitSSLContext(CertificatesFileDirectory + keyStoreName, keypass, CertificatesFileDirectory + trustStoreName, trustpass, encryptionProtocol);
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Initialize ssl socket factory">
                SSLSocketFactory sslsocketfactory = context.getSocketFactory();
                if (Tor) {
                    //7nqrwvca2nboeer5.onion    5556
                    //jybkxibzmdabypy7.onion    5555
                    mygui.printMsg("[Debug] " + "Starting connection over SSL through Tor Network");
                    TorSocket = TorLib.TorSocket(torhostname, SSLPort);
                    myclient = (SSLSocket) sslsocketfactory.createSocket(
                            TorSocket, null,
                            SSLPort, false);
                } else {
                    mygui.printMsg("[Debug] " + "Starting connection over SSL");
                    myclient = (SSLSocket) sslsocketfactory.createSocket(ip, SSLPort);
                }
                //</editor-fold>

                Port = SSLPort;
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Check client connection">
            if (myclient.isConnected()) {
                mygui.printMsg("[Debug] " + "Connecting to the " + torhostname + " on Port:" + Port);
            } else {
                return false;
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Initialize streams">
            OutStream = new ObjectOutputStream(myclient.getOutputStream());
            InStream = new ObjectInputStream(myclient.getInputStream());
            os = myclient.getOutputStream();
            is = myclient.getInputStream();
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Handshake">
            OutStream.writeObject("InitiateConnection");
            if (!InStream.readObject().equals("ConnectionInitiated")) {
                throw new CustomConnectionException("ConnectionInitiated");
            }
            mygui.printMsg("[Debug] " + "Connection Initiated: Listening to the server...");
            //</editor-fold>
            //</editor-fold>
        } catch (IOException | CustomConnectionException | ClassNotFoundException ex) {
            Logger.getLogger(ClientChat.class.getName()).log(Level.SEVERE, null, ex);
            mygui.printMsg("[Debug] " + "Failed to connect");
            return false;
        }

        return true;
    }

    public void ExchangeFile(String filepath, String filename, String UserInput) {
        //<editor-fold defaultstate="collapsed" desc="Continue communication">
        if (communicationFlag == true) {
            try {
                OutStream.writeObject("ContinueCommunication");
                mygui.printMsg("[Debug] " + "Continue Communication");
            } catch (IOException ex) {
                Logger.getLogger(ClientChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>

        try {
            //<editor-fold defaultstate="collapsed" desc="Initiate file exchange">
            OutStream.writeObject("FileExchange");
            mygui.printMsg("[Debug] " + "FileExchange");
            if (!InStream.readObject().equals("FileExchangeInitiated")) {
                throw new CustomConnectionException("FileExchangeInitiated");
            }
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Choose between SendFile and ReceiveFile">
            //<editor-fold defaultstate="collapsed" desc="Send file">
            if (UserInput.compareTo("ClientSendsFile") == 0) {
                OutStream.writeObject("ClientSendsFile");
                mygui.printMsg("[Debug] " + "ClientSendsFile");
                if (!InStream.readObject().equals("ClientSendsFileInitiated")) {
                    throw new CustomConnectionException("ClientSendsFileInitiated");
                }

                //<editor-fold defaultstate="collapsed" desc="Load file and send">
                File myFile = new File(filepath);
                byte[] filebytearray = new byte[(int) myFile.length()];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile));
                bis.read(filebytearray, 0, filebytearray.length);

                OutStream.writeObject(filename);
                OutStream.writeObject((int) myFile.length());

                os.write(filebytearray, 0, filebytearray.length);
                os.flush();
                bis.close();
                mygui.printMsg("[Debug] " + "File send");
                //</editor-fold>
            } //</editor-fold>
            //<editor-fold defaultstate="collapsed" desc="Receive file">
            else if (UserInput.compareTo("ClientReceivesFile") == 0) {
                OutStream.writeObject("ClientReceivesFile");
                mygui.printMsg("[Debug] " + "ClientReceivesFile");

                //<editor-fold defaultstate="collapsed" desc="Declare and initialize ">
                OutStream.writeObject(filename);
                int totalBytes = (int) InStream.readObject();
                mygui.printMsg("[Debug] " + "Client choosed file: " + filename + "with size: " + totalBytes / 1024 + " KB");

                byte[] buffer;
                InputStream input;

                buffer = new byte[myclient.getReceiveBufferSize()];
                input = myclient.getInputStream();

                int bytesReceived = 0;

                FileOutputStream wr = new FileOutputStream(new File(clientFilesDirectory + filename));
                //</editor-fold>

                //<editor-fold defaultstate="collapsed" desc="Receive and save file">
                while (totalBytes > 0) {
                    if ((bytesReceived = input.read(buffer)) <= 0) {
                        break;
                    }
                    wr.write(buffer, 0, bytesReceived);
                    totalBytes -= bytesReceived;
                    //System.out.println(totalBytes);
                }
                wr.close();
                //</editor-fold>

                mygui.printMsg("[Debug] " + "File saved to " + clientFilesDirectory + filepath);
            }
            //</editor-fold>
            //</editor-fold>
            //</editor-fold>

        } //<editor-fold defaultstate="collapsed" desc="Catch exceptions"> //<editor-fold defaultstate="collapsed" desc="Catch exceptions"> //<editor-fold defaultstate="collapsed" desc="Catch exceptions"> //<editor-fold defaultstate="collapsed" desc="Catch exceptions"> //<editor-fold defaultstate="collapsed" desc="Catch exceptions"> //<editor-fold defaultstate="collapsed" desc="Catch exceptions"> //<editor-fold defaultstate="collapsed" desc="Catch exceptions"> //<editor-fold defaultstate="collapsed" desc="Catch exceptions">
        catch (IOException | ClassNotFoundException | CustomConnectionException ex) {
            Logger.getLogger(ClientChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>
        communicationFlag = true;
    }

    public String ExchangeMessage(String Message) {
        //<editor-fold defaultstate="collapsed" desc="Continue communication">
        String serverAnswer = "";
        if (communicationFlag == true) {
            try {
                OutStream.writeObject("ContinueCommunication");
                mygui.printMsg("[Debug] " + "Continue Communication");
            } catch (IOException ex) {
                Logger.getLogger(ClientChat.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //</editor-fold>

        try {
            //<editor-fold defaultstate="collapsed" desc="Make message exchange request">
            OutStream.writeObject("MessageExchange");
            if (!InStream.readObject().equals("MessageExchangeInitiated")) {
                throw new CustomConnectionException("MessageExchangeInitiated");
            }
            mygui.printMsg("[Debug] " + "MessageExchange");
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Send message">
            mygui.printMsg("[Debug] " + "Write message");
            OutStream.writeObject(Message);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Get response message">
            serverAnswer = InStream.readObject().toString();
            mygui.printMsg("[Debug] " + "Server answered: " + serverAnswer);
            //</editor-fold>

        } //<editor-fold defaultstate="collapsed" desc="Catch exceptions">
        catch (IOException | ClassNotFoundException | CustomConnectionException ex) {
            Logger.getLogger(ClientChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>

        communicationFlag = true;
        return serverAnswer;
    }

    public void Disconnect() {
        //<editor-fold defaultstate="collapsed" desc="Disconnect from server">
        try {
            OutStream.writeObject("TerminateCommunication");
            if (Tor && Port == SSLPort) {
                TorSocket.close();
            }
            myclient.close();

            mygui.printMsg("[Debug] " + "Connection to the server have been closed");
        } catch (IOException ex) {
            Logger.getLogger(ClientChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
    }

    private SSLContext InitSSLContext(String keystorePath, char[] KeystorePass, String truststorePath, char[] truststorePass, String encryptionProtocol) {
        try {
            //<editor-fold defaultstate="collapsed" desc="Initialize keystore">
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(LoadJKSKeystore(keystorePath, KeystorePass), KeystorePass);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Initialize Trust manager">
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(LoadJKSKeystore(truststorePath, truststorePass));
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Initialize context">
            SSLContext context = SSLContext.getInstance(encryptionProtocol);
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
            //</editor-fold>
            return context;
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException | KeyStoreException ex) {
            Logger.getLogger(ClientChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private KeyStore LoadJKSKeystore(String storePath, char[] storePass) {
        try {
            KeyStore ks;
            FileInputStream fin = new FileInputStream(storePath);
            ks = KeyStore.getInstance("JKS");
            ks.load(fin, storePass);
            return ks;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            Logger.getLogger(ClientChat.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
