package hermes.dataobj;

import hermes.matching.Matcher;

import java.util.*;


/**
 * Created by hult on 3/17/17.
 */

class SubscriptionGenerator {
    private int size;
    private Random rand;
    private int id;

    public SubscriptionGenerator(int size) {
        this.size = size;
        this.rand = new Random();
        this.id = 0;
    }

    public int[][] nextFilter() {
        int[][] ret = new int[2][size];
        Arrays.fill(ret[0], -1);
        Arrays.fill(ret[1], -1);
        for (int i = 0; i < size; i += 1) {
            if (rand.nextFloat() > 0.6) {
                int small = rand.nextInt(100);
                if (small < 10) small += 10;
                int big = rand.nextInt(100);
                if (big < 10) big += 10;
                ret[1][i] = Math.max(small, big);
                ret[0][i] = Math.min(small, big);
            }
        }
        return ret;
    }

    public Subscription nextSub() {
        return new Subscription(this.id++, nextFilter());
    }

    static void print(int[][] sub) {
        System.out.println(Arrays.toString(sub[1]));
        System.out.println(Arrays.toString(sub[0]));

    }

    public static void main(String[] args) {
        //System.out.println("start: " + System.currentTimeMillis());
        int nSub = 10000;
        int size = 20;
        int total = 0;
        SubscriptionGenerator gen = new SubscriptionGenerator(size);
        EventGenerator eventGenerator = new EventGenerator(size);
        List<int[][]> subList = new ArrayList<>(nSub);
        Matcher matcher = new Matcher(size);
        for (int i =0; i < nSub; i += 1) {
            int[][] sub = gen.nextSub().filter;
            subList.add(sub);
            matcher.addSubscription(sub);
        }
        int err = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10_000; i += 1) {
            int[] event = eventGenerator.nextEvent().values;
            Set<Integer> ids = matcher.match(event);
            total += ids.size();
            /*
            if (ids.size() > 0) {
                //System.out.println("hermes.dataobj.Event: \n" + Arrays.toString(event));
                for (int id: ids) {
                    int[][] sub = subList.get(id);
                    for (int j = 0; j < size; j += 1) {
                        if (sub[1][j] == -1) continue;
                        if (event[j] > sub[1][j] || event[j] < sub[0][j]) {
                            System.out.println("err match ids: " + ids);
                            for (int k = 0; k < subList.size(); k += 1) {
                                System.out.println("sub " + k + ":");
                                hermes.dataobj.SubscriptionGenerator.print(subList.get(k));
                            }
                            System.out.println(matcher);
                            hermes.dataobj.EventGenerator.print(event);
                            System.out.println(err);
                            System.exit(0);
                        }
                    }
                }
            }
            */
        }
        System.out.println("use: " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("avg: " + (float)total / 1000);
    }
}
