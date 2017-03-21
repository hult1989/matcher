package hermes.dataobj;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by hult on 3/17/17.
 */
public class EventGenerator {
    int size;
    Random rand;
    int id;
    public static final int MAX = 1_000;

    public EventGenerator(int size) {
        this.size = size;
        this.rand = new Random();
        this.id = 0;
    }

    public int[] nextValues() {
        int[] event = new int[size];
        for (int i = 0; i < size; i += 1) {
            event[i] = rand.nextInt(MAX);
        }
        return event;
    }

    public Event nextEvent() {
        return new Event(this.id++, nextValues());
    }

    static void print(int[] event) {
        System.out.println(Arrays.toString(event));
    }

    public static void main(String[] args) {
        EventGenerator generator = new EventGenerator(5);
        for (int i = 0; i < 5; i++) {
            System.out.println(generator.nextEvent());
        }
        System.out.println();
    }

}
