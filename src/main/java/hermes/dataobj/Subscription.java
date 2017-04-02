package hermes.dataobj;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by hult on 3/19/17.
 */
public class Subscription {
    public final int id;
    public final int[][] filter;
    public long timestamp;

    public Subscription(int id, int[][] filter) {
        this.id = id;
        this.timestamp = System.currentTimeMillis();
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

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 8 + 4 + 8 * filter[0].length);
        buffer.putLong(this.timestamp);
        buffer.putInt(this.id);
        buffer.putInt(this.filter[0].length);
        for (int i = 0; i < filter[0].length; i += 1) {
            buffer.putInt(filter[1][i]);
            buffer.putInt(filter[0][i]);
        }
        return buffer.array();
    }

    public static Subscription fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long time = buffer.getLong();
        int id = buffer.getInt();
        int[][] filter = new int[2][buffer.getInt()];
        for (int i = 0; i < filter[0].length; i += 1) {
            filter[1][i] = buffer.getInt();
            filter[0][i] = buffer.getInt();
        }
        Subscription sub = new Subscription(id, filter);
        sub.timestamp = time;
        return sub;
    }

    public int length() {
        return (int) Arrays.stream(filter[0]).filter(v-> v != -1).count();
    }

    public static void main(String[] args) {
    }
}
