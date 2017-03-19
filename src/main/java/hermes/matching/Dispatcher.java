package matching;

import java.util.stream.IntStream;

/**
 * Created by hult on 3/17/17.
 */
public class Dispatcher {
    public static void main(String[] args) {
            /*
        matching.Dispatcher dispatcher = new matching.Dispatcher();
        float total = 0;
        int n = 1000;
        int dimension = 10;
        hermes.dataobj.SubscriptionGenerator subscriptionGenerator = new hermes.dataobj.SubscriptionGenerator(dimension);
        for (int i = 0; i < n; i += 1) {
            int[][] sub = subscriptionGenerator.nextSub();
            List<Integer> vectors = dispatcher.vectorOfSubscription(sub, 50);
            total += vectors.size();
            System.out.println("sub: ");
            System.out.println(Arrays.toString(sub[1]));
            System.out.println(Arrays.toString(sub[0]));

            for (int v: vectors) {
                System.out.printf("vector: %010d\n", Integer.valueOf(Integer.toBinaryString(v)));
            }
        }
        System.out.println("ideal: " + 1.0 / Math.pow(2, dimension));
        System.out.println("fact: " + total / n / Math.pow(2, dimension));
        */
    }

    public int[] vectorOfSubscription(int[][] subscription, int mid) {
        IntStream vStream = IntStream.of(0);
        for (int i = 0; i < subscription[0].length; i += 1) {
            int ub = subscription[1][i];
            int lb = subscription[0][i];
            if (ub != -1 && ub <= mid) {
                vStream = vStream.map(v -> v << 1);
            } else if (lb != -1 && lb >= mid) {
                vStream = vStream.map(v -> 1 + (v << 1));
            } else {
                vStream = vStream.flatMap(v -> IntStream.of(v << 1, 1 + (v << 1)));
            }
        }
        return vStream.toArray();
    }

    public int[] vectorOfEvent(int[] event, int mid) {
        IntStream vectorsStream = IntStream.of(0);
        for (int val: event) {
            if (val == -1 || val == mid) {
                vectorsStream = vectorsStream.flatMap(v -> IntStream.of(v << 1, 1 + (v << 1)));
            } else if (val > mid) {
                vectorsStream = vectorsStream.map(v -> 1 + (v << 1));
            } else {
                vectorsStream = vectorsStream.map(v -> v << 1);
            }
        }
        return vectorsStream.toArray();
    }
}
