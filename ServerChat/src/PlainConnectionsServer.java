/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
//<editor-fold defaultstate="collapsed" desc="Imports">
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
//</editor-fold>

public class PlainConnectionsServer extends Thread {

    @Override
    public void run() {

        int PlainPort = 5555; //our socket port

        try {
            //<editor-fold defaultstate="collapsed" desc="Declare variables">
            ServerSocket myServer;
            Socket myClient;
            myServer = new ServerSocket(PlainPort, 50);
            Print.printMsg("Server is Listening on Port " + PlainPort);
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Connect with client">
            while (true) {
                myClient = myServer.accept();
                String ClnIp = "\nClient " + myClient.getRemoteSocketAddress().toString().replace("/", "");

                Print.printMsg(ClnIp + " connected over plain socket.\n");

                //<editor-fold defaultstate="collapsed" desc="Initiate Communication">
                Communication runCommunication = new Communication(myClient);
                runCommunication.start();
                //</editor-fold>
            }
            //</editor-fold>

        } //<editor-fold defaultstate="collapsed" desc="Catch connection exception">
        catch (IOException ex) {
            Logger.getLogger(ServerChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        //</editor-fold>
    }
}
