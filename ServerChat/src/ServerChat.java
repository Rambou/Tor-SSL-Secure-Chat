/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
public class ServerChat {

    public static void main(String[] args) {
        //Initialization of Plain and SSL connection threads.
        PlainConnectionsServer runPlainConenction = new PlainConnectionsServer();
        SSLConnectionsServer runSSLConenction = new SSLConnectionsServer();

        //start threads
        runPlainConenction.start();
        runSSLConenction.start();
    }

}
