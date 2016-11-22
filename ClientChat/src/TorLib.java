/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
//<editor-fold defaultstate="collapsed" desc="Imports">
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
//</editor-fold>

public class TorLib {

    /**
     * Default TOR Proxy port.
     */
    static int proxyPort = 9050;
    /**
     * Default TOR Proxy hostaddr.
     */
    static String proxyAddr = "localhost";
    /**
     * Constant tells SOCKS4/4a to connect. Use it in the <i>req</i> parameter.
     */
    final static byte TOR_CONNECT = (byte) 0x01;
    /**
     * Constant tells TOR to do a DNS resolve. Use it in the <i>req</i>
     * parameter.
     */
    final static byte TOR_RESOLVE = (byte) 0xF0;
    /**
     * Constant indicates what SOCKS version are talking Either SOCKS4 or
     * SOCKS4a
     */
    final static byte SOCKS_VERSION = (byte) 0x04;
    /**
     * SOCKS uses Nulls as field delimiters
     */
    final static byte SOCKS_DELIM = (byte) 0x00;
    /**
     * Setting the IP field to 0.0.0.1 causes SOCKS4a to be enabled.
     */
    final static int SOCKS4A_FAKEIP = (int) 0x01;

    static Socket TorSocketPre(String targetHostname, int targetPort, byte req)
            throws IOException {

        Socket s;
        s = new Socket(proxyAddr, proxyPort);
        DataOutputStream os = new DataOutputStream(s.getOutputStream());
        os.writeByte(SOCKS_VERSION);
        os.writeByte(req);
        // 2 bytes 
        os.writeShort(targetPort);
        // 4 bytes, high byte first
        os.writeInt(SOCKS4A_FAKEIP);
        os.writeByte(SOCKS_DELIM);
        os.writeBytes(targetHostname);
        os.writeByte(SOCKS_DELIM);

        return (s);
    }

    static Socket TorSocket(String targetHostname, int targetPort)
            throws IOException {
        Socket s = TorSocketPre(targetHostname, targetPort, TOR_CONNECT);
        DataInputStream is = new DataInputStream(s.getInputStream());

        // only the status is useful on a TOR CONNECT
        byte version = is.readByte();
        byte status = is.readByte();
        if (status != (byte) 90) {
            //failed for some reason, return useful exception
            throw (new IOException(ParseSOCKSStatus(status)));
        }

        int port = is.readShort();
        int ipAddr = is.readInt();
        return (s);
    }

    static String ParseSOCKSStatus(byte status) {
        // func to turn the status codes into useful output
        // reference 
        String retval;
        switch (status) {
            case 90:
                retval = status + " Request granted.";
                break;
            case 91:
                retval = status + " Request rejected/failed - unknown reason.";
                break;
            case 92:
                retval = status + " Request rejected: SOCKS server cannot connect to identd on the client.";
                break;
            case 93:
                retval = status + " Request rejected: the client program and identd report different user-ids.";
                break;
            default:
                retval = status + " Unknown SOCKS status code.";
        }
        return (retval);

    }
}
