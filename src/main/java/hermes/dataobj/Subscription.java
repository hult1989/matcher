package hermes.dataobj;

import java.util.Arrays;

/**
 * Created by hult on 3/19/17.
 */
public class Subscription {
    public final int id;
    public final int[][] filter;

    public Subscription(int id, int[][] filter) {
        this.id = id;
        this.filter = filter;
    }

    @Override
    public String toString() {
        return String.format("id: %s\n%s\n%s\n", id, Arrays.toString(filter[1]), Arrays.toString(filter[0]));
    }

    public boolean match(Event e) throws RuntimeException{
        if (e.values.length != filter[0].length) {
            throw new RuntimeException("event length != subscription length");
        }
        for (int i = 0; i < e.values.length; i += 1) {
            if (e.values[i] == -1 && filter[1][i] == -1) continue;
            if (filter[0][i] <= e.values[i] && e.values[i] <= filter[1][i]) continue;
            return false;
        }
        return true;
    }
}
