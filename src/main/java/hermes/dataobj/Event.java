package hermes.dataobj;


import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by hult on 3/7/17.
 */
public class Event {
    public final long timestamp;
    public final int id;
    public final int[] values;

    public Event(int id, int[] values) {
        this.id = id;
        this.values = values;
        this.timestamp = System.currentTimeMillis();
    }

    private Event(long timestamp, int id, int[] values) {
        this.values = values;
        this.id = id;
        this.timestamp = timestamp;
    }


    public static void main(String[] args) {
        Event e = new Event(3, new int[]{2, 3});
        System.out.println(e);
        byte[] bytes = e.toBytes();
        Event ecopy = Event.fromBytes(bytes);
        System.out.println(ecopy);
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(8 + 4 + + 4 +  4 * values.length);
        buffer.putLong(timestamp);
        buffer.putInt(id);
        buffer.putInt(values.length);
        for (int v: values) buffer.putInt(v);
        return buffer;
    }

    public byte[] toBytes() {
        return toByteBuffer().array();
    }

    public static Event fromBytes(byte[] bytes) {
        return fromByteBuffer(ByteBuffer.wrap(bytes));

    }

    public static Event fromByteBuffer(ByteBuffer buffer) {
        long time = buffer.getLong();
        int id = buffer.getInt();
        int len = buffer.getInt();
        int[] values = new int[len];
        for (int i = 0; i < len; i += 1) {
            values[i] = buffer.getInt();
        }
        return new Event(time, id, values);
    }

    @Override
    public String toString() {
        return  String.format("id: %s, values: %s, timestamp: %s", id, Arrays.toString(values), timestamp);
    }
}
