package hermes.matching;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Created by hult on 3/26/17.
 */
public class ConstantHasher {
    private static final int REPLICA = 29;
    private TreeSet<Integer> hcSet;
    private Map<Integer, Integer> replica2original;
    private HashFunction hashFunction;

    public ConstantHasher(List<Integer> matchers) {
        this.hcSet = new TreeSet<>();
        this.hashFunction = Hashing.murmur3_32();
        for (int matcherID: matchers) {
            for (int i = 0; i < REPLICA; i += 1) {
                int hc = hashFunction.newHasher()
                        .putString(UUID.randomUUID().toString(), StandardCharsets.US_ASCII)
                        .hashCode();
                replica2original.put(hc, matcherID);
            }
        }
    }

    public int find(int target) {
        return 0;
    }
}
