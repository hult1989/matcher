package matching;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hult on 3/10/17.
 */
public class Pairwise {
    public BoundList first;
    public BoundList second;

    public static Pairwise fromBytes(byte[] bytes) {
        Pairwise pairwise = new Pairwise();
        if (bytes != null && bytes.length != 0) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            if (buffer.hasRemaining()) {
                pairwise.first = BoundList.fromBytes(buffer);
                if (buffer.hasRemaining()) {
                    pairwise.second = BoundList.fromBytes(buffer);
                }
            }
        }
        return pairwise;
    }

    public byte[] toBytes() {
        int size = 0;
        ArrayList<byte[]> list = new ArrayList<>();
        if (this.first != null) {
            byte[] bytes = first.toBytes();
            size += bytes.length;
            list.add(bytes);
        }
        if (this.second != null) {
            byte[] bytes = second.toBytes();
            list.add(bytes);
            size += bytes.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(size);
        for (byte[] bytes: list) {
            buffer.put(bytes);
        }
        return buffer.array();
    }

    public List<Integer> search(int valI, int valJ) {
        List<Integer> ret = new ArrayList<>();
        if (first != null) ret.addAll(first.search(valI));
        if (second != null) ret.addAll(second.search(valJ));
        return ret;
    }


    public void addSub(boolean both, int id, int[] boundI, int[] boundJ) {
        if (first == null) first = new BoundList();
        first.insert(id, boundI);
        if (both) {
            if (second == null) second = new BoundList();
            second.insert(id, boundJ);
        }
    }

    @Override
    public String toString() {
        return String.format("FIRST: \n%s\nSECOND: \n%s\n", first, second);
    }

    static class KV {
        public static final int BYTE_SIZE = 8;
        int id;
        int value;
        public KV(int k, int v) {
            this.id = k;
            this.value = v;
        }
        @Override
        public String toString() {
            return String.format("%s\\%s", this.id, this.value);
        }

        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(8);
            return buffer.putInt(id).putInt(value).array();
        }

        public long toLong() {
            return  ((long)this.id << 32) | this.value;
        }

        public static KV fromBytes(byte[] bytes) {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            return new KV(buf.getInt(), buf.getInt());
        }

        public static KV fromLong(long l) {
            int id = (int)(l >> 32);
            int value = (int) (l & 0xffffffffL);
            return new KV(id, value);
        }

    }

    static class BoundList {
        static Comparator<KV> comp = (a, b) -> a.value - b.value;
        private ArrayList<KV> upperBoundList;
        private ArrayList<KV> lowerBoundList;

        public List<Integer> search(int value) {
            List<Integer> ret = new ArrayList<>();
            KV target = new KV(0, value);
            if (this.upperBoundList != null) {
                int k = Collections.binarySearch(upperBoundList, target, comp);
                if (k < 0) k = -k - 1;
                for (int i = k; i < upperBoundList.size(); i += 1) {
                    ret.add(upperBoundList.get(i).id);
                }
            }
            if (this.lowerBoundList != null) {
                int k = Collections.binarySearch(lowerBoundList, target, comp);
                k = k < 0 ? -1 - k : k + 1;
                for (int i = 0; i < k; i += 1) {
                    //ret.add(upperBoundList.get(i).id);
                    ret.add(lowerBoundList.get(i).id);
                }
            }
            return ret;
        }


        public void insert(int id, int[] bound) {
            if (upperBoundList == null) upperBoundList = new ArrayList<>();
            if (lowerBoundList == null) lowerBoundList = new ArrayList<>();
            KV upperBound = new KV(id, bound[1]);
            KV lowerBound = new KV(id, bound[0]);
            int k = Collections.binarySearch(upperBoundList, upperBound, comp);
            if (k < 0) k = -1 - k;
            upperBoundList.add(k, upperBound);
            k = Collections.binarySearch(lowerBoundList, lowerBound, comp);
            if (k < 0) k = -1 - k;
            lowerBoundList.add(k, lowerBound);
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            ret.append("Upper: ");
            if (this.upperBoundList != null) {
                for (KV ub : upperBoundList) {
                    ret.append(ub);
                    ret.append(",");
                }
            }
            ret.append("\n");
            ret.append("Lower: ");
            if (this.lowerBoundList != null) {
                for (KV lb : lowerBoundList) {
                    ret.append(lb);
                    ret.append(",");
                }
            }
            return ret.toString();
        }

        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(4 + upperBoundList.size() * KV.BYTE_SIZE
                    + 4 + lowerBoundList.size() * KV.BYTE_SIZE);
            buffer.putInt(upperBoundList.size());
            for (KV kv: upperBoundList) buffer.putLong(kv.toLong());
            buffer.putInt(lowerBoundList.size());
            for (KV kv: lowerBoundList) buffer.putLong(kv.toLong());
            return buffer.array();
        }

        public static BoundList fromBytes(ByteBuffer buffer) throws RuntimeException{
            BoundList boundList = new BoundList();
            if (buffer == null || buffer.remaining() < 8) {
                throw new RuntimeException("deserialization error for buffer size is " + buffer.remaining());
            }
            int size = buffer.getInt();
            if (size > 0) {
                boundList.upperBoundList = new ArrayList<>(size);
                for (int i = 0; i < size; i += 1) {
                    boundList.upperBoundList.add(KV.fromLong(buffer.getLong()));
                }
            }
            size = buffer.getInt();
            if (size > 0) {
                boundList.lowerBoundList = new ArrayList<>(size);
                for (int i = 0; i < size; i += 1) {
                    boundList.lowerBoundList.add(KV.fromLong(buffer.getLong()));
                }
            }
            return boundList;
        }

    }


    public static void main(String[] args) {
        byte[] bytes = "".getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        System.out.println(buffer.hasRemaining());
        System.out.println(buffer.remaining());
        System.out.println(buffer.getInt());

        Pairwise pairwise = Pairwise.fromBytes(bytes);
        System.out.println(bytes.length);
        System.out.println(pairwise);
    }
}
