/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
//<editor-fold defaultstate="collapsed" desc="Imports">
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
//</editor-fold>

public class SSLConnectionsServer extends Thread {

    @Override
    public void run() {

        int SSLPort = 5556; //our socket port

        try {
            //<editor-fold defaultstate="collapsed" desc="Declare and initialize variables">
            String serverFileDirectory = System.getProperty("user.dir") + "\\";
            //String CertificatesFileDirectory = serverFileDirectory.replace(serverFileDirectory.split("\\\\")[serverFileDirectory.split("\\\\").length - 1] + "\\", "") + "Certificates\\";

            char[] trustpass = "aegean".toCharArray();
            char[] keypass = "aegean".toCharArray();
            String trustStoreName = "CA_Keystore.jks";
            String keyStoreName = "Server_Keystore.jks";
            String encryptionProtocol = "SSLv3";
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Initialize SSL context(keymanager and trustmanager)">
            SSLContext context = InitSSLContext(serverFileDirectory + keyStoreName, keypass, serverFileDirectory + trustStoreName, trustpass, encryptionProtocol);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Declare and initialize the Server Socket">
            SSLServerSocketFactory sslServerSocketfactory = context.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketfactory.createServerSocket(SSLPort);
            sslServerSocket.setEnabledProtocols(new String[]{encryptionProtocol});
            sslServerSocket.setNeedClientAuth(true);
            Print.printMsg("Server over SSL is Listening on Port " + SSLPort);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Connect with client">
            while (true) {
                SSLSocket sslSocketClient = (SSLSocket) sslServerSocket.accept();
                String ClnIp = "\nClient " + sslSocketClient.getRemoteSocketAddress().toString().replace("/", "");

                Print.printMsg(ClnIp + " connected over SSL socket.\n");

                //<editor-fold defaultstate="collapsed" desc="Initiate Communication">
                Communication runCommunication = new Communication(sslSocketClient);
                runCommunication.start();
                //</editor-fold>
            }
            //</editor-fold>
        }//<editor-fold defaultstate="collapsed" desc="Catch connection exception"> 
        catch (IOException ex) {
            Logger.getLogger(SSLConnectionsServer.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(SSLConnectionsServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private KeyStore LoadJKSKeystore(String storePath, char[] storePass) {
        try {
            KeyStore ks;
            InputStream fin = new FileInputStream(storePath);
            ks = KeyStore.getInstance("JKS");
            ks.load(fin, storePass);
            return ks;
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException ex) {
            Logger.getLogger(SSLConnectionsServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
