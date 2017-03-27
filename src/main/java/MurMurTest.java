import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by hult on 3/6/17.
 */

public class MurMurTest {
    static final int MATCHER_NUM = 10;
    static final int SUBSPACE_NUM = 1_000;
    static final int REPLICA_NUM = 9;

    public static void main(String[] args) {
        TreeSet<Integer> hcSet = new TreeSet<>();
        SortedMap<Integer,  Matcher> matcherMap = new TreeMap<>();
        ArrayList< Matcher> matcherList = new ArrayList<>(MATCHER_NUM);

        for (int i = 0; i < MATCHER_NUM; i += 1) {
            Matcher m = new  Matcher(UUID.randomUUID().toString());
            matcherList.add(m);
            //System.out.println(m.hc);
            for (int hc : m.replicas) {
                hcSet.add(hc);
                matcherMap.put(hc, m);
            }
        }

        HashFunction hashFunction = Hashing.murmur3_32();
        for (int i = 0; i < SUBSPACE_NUM; i += 1) {
            int hc = hashFunction.newHasher().putString(UUID.randomUUID().toString(), StandardCharsets.UTF_8).hashCode();
            int id = (hcSet.last() <= hc) ? hcSet.first() : hcSet.higher(hc);
            matcherMap.get(id).add(i);
        }

        matcherList.forEach(m -> System.out.println(m.container.size()));
        System.out.println(matcherList.stream().mapToInt(m -> m.container.size()).sum());
    }

    static class Matcher {
        ArrayList<Integer> container;
        ArrayList<Integer> replicas;
        String strID;

        Matcher(String strID) {
            this.strID = strID;
            this.container = new ArrayList<>();
            this.replicas = new ArrayList<>(REPLICA_NUM);
            for (int i = 0; i < REPLICA_NUM; i += 1) {
                int hc = Hashing.murmur3_32().newHasher().putString(strID + i, StandardCharsets.UTF_8).hashCode();
                this.replicas.add(hc);
            }
        }

        void add(int e) {
            container.add(e);
        }


    }
}
