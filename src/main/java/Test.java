import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hult on 3/14/17.
 */
public class Test {
    public static void main(String[] args) {
        List<Integer> lower = new ArrayList<>(10);
        lower.add(10);
        lower.add(19);
        lower.add(21);
        lower.add(30);
        lower.add(32);
        lower.add(35);
        lower.add(40);
        lower.add(41);
        lower.add(64);
        lower.add(73);
        List<Integer> ret = new ArrayList<>();
        int k = Collections.binarySearch(lower, 24);
        k = k < 0 ? -1 - k : k + 1;
        for (int i = 0; i < k; i += 1) {
            ret.add(lower.get(i));
        }

    }
}
