package matching;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hult on 3/11/17.
 */
public class Subscriptions {
    Map<Integer, Integer> sizeMap;

    public Subscriptions() {
        this.sizeMap = new HashMap<>();
    }

    public int add(int[][] sub) {
        int id = this.sizeMap.size();
        int size = (int) Arrays.stream(sub[1]).filter(ub -> ub != -1).count();
        this.sizeMap.put(id, size);
        return id;
    }

    public Subscriptions(ArrayList<int[][]> subscriptions) {
        this();
        for (int id = 0; id < subscriptions.size(); id += 1) {
            int[][] sub = subscriptions.get(id);
            int size = 0;
            for (int j = 0; j < sub.length; j += 1) {
                if (sub[0][j] != -1) size += 1;
            }
            sizeMap.put(id, size);
        }
    }

    @Override
    public String toString() {
        return this.sizeMap.toString();
    }

    byte[] toBytes() {
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 + 8 * sizeMap.size());
        bytebuffer.putInt(sizeMap.size());
        for (int k: sizeMap.keySet()) {
            bytebuffer.putInt(k);
            bytebuffer.putInt(sizeMap.get(k));
        }
        return bytebuffer.array();
    }

    static Subscriptions fromBytes(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        int n = buf.getInt();
        Subscriptions ret = new Subscriptions();
        for (int i = 0; i <n ; i += 1) {
            ret.sizeMap.put(buf.getInt(), buf.getInt());
        }
        return ret;
    }

    public static void main(String[] args) {

    }

}
