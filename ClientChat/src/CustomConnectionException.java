/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
public class CustomConnectionException extends Exception {

    public CustomConnectionException(String message) {
        //I just commented the exception!!
        super("String \"" + message + "\" was not received correctly");
    }
}
