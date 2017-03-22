package hermes.dataobj;

import hermes.matching.Matcher;

import java.util.*;


/**
 * Created by hult on 3/17/17.
 */

public class SubscriptionGenerator {
    private int size;
    private Random rand;
    private int id;
    static final int MAX = EventGenerator.MAX;


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
            int small = rand.nextInt(MAX);
            int big = Math.min(MAX - 1, small + 250);
            ret[1][i] = big;
            ret[0][i] = small;
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
        int nSub = 40_0;
        int size = 4;
        int total = 0;
        SubscriptionGenerator gen = new SubscriptionGenerator(size);
        EventGenerator eventGenerator = new EventGenerator(size);
        List<int[][]> subList = new ArrayList<>(nSub);
        Matcher matcher = new Matcher(size);
        for (int i =0; i < nSub; i += 1) {
            int[][] sub = gen.nextSub().filter;
            subList.add(sub);
            matcher.addFilter(sub);
        }
        int err = 0;
        long start = System.currentTimeMillis();
        System.out.println("start: " + start);
        for (int i = 0; i < 1_000; i += 1) {
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
