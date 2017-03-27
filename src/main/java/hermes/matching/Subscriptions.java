package hermes.matching;

import hermes.dataobj.Subscription;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hult on 3/11/17.
 */
class Subscriptions {
    Map<Integer, Integer> sizeMap;
    //private HashMap<Integer, int[][]> subArchive;

    Subscriptions() {
        this.sizeMap = new HashMap<>();
        //this.subArchive = new HashMap<>();
    }

    /*
    int[][] getSubById(int id) {
        return subArchive.get(id);
    }
    */

    private void add(int id, int[][] sub) {
        int size = (int) Arrays.stream(sub[1]).filter(ub -> ub != -1).count();
        this.sizeMap.put(id, size);
        //this.subArchive.put(id, sub);
    }

    int add(int[][] sub) {
        int id = this.sizeMap.size();
        this.add(id, sub);
        return id;
    }

    boolean add(Subscription sub) {
        /*
        if (this.subArchive.containsKey(sub.id)) {
            return false;
        }
        */
        this.add(sub.id, sub.filter);
        return true;
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
