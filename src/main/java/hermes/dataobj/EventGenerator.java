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

    public EventGenerator(int n) {
        this.size = n;
        this.rand = new Random();
        this.id = 0;
    }

    public int[] nextValues() {
        int[] event = new int[size];
        for (int i = 0; i < size; i += 1) {
            if (rand.nextFloat() > 0.2) {
                event[i] = rand.nextInt(100);
                if (event[i] < 10) event[i] += 10;
            } else {
                event[i] = -1;
            }
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
