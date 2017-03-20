import hermes.dataobj.Event;
import hermes.dataobj.EventGenerator;

/**
 * Created by hult on 3/14/17.
 */
public class Test {
    public static void main(String[] args) {
        EventGenerator generator = new EventGenerator(5);
        String p = generator.nextEvent().toASCIIString();
        System.out.println(Event.fromASCIIString(p));
    }
}
