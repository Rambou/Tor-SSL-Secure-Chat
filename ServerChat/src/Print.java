import java.text.SimpleDateFormat;
import java.util.Calendar;

/*
 @author Bousios Nikolaos 321/2010124
 @author Chasiotis Konstantinos 321/2011175
 @author Kaonis Charis Ioannikios 321/2010069
 */
public class Print {

    public static void printMsg(String msg) {
        Calendar  cal = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
        fmt.setCalendar(cal);
        msg = "[" + fmt.format(cal.getTime()) + "] " + msg;
        System.out.println(msg);
    }
}
