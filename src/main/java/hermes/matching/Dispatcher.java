package hermes.matching;

import hermes.dataobj.EventGenerator;
import hermes.dataobj.SubscriptionGenerator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by hult on 3/17/17.
 */
public class Dispatcher {
    int size;
    int nSeg;
    int range;
    static final int MAX = EventGenerator.MAX;
    private Matcher[] matchers;


    public Dispatcher(int size, int nSeg) {
        this.size = size;
        this.nSeg = nSeg;
        this.range = MAX / nSeg;
        this.matchers = Stream.generate(()-> new Matcher(size)).limit((int)Math.pow(nSeg, size)).toArray(Matcher[]::new);
    }
    public static void main(String[] args) {
        int size = 4;
        int nSub = 40_000;
        int nEvent = 10_000;
        int nSeg = 10;
        Dispatcher dispatcher = new Dispatcher(size, nSeg);
        SubscriptionGenerator subscriptionGenerator = new SubscriptionGenerator(size);
        EventGenerator eventGenerator = new EventGenerator(size);
        List<int[]> events = Stream.generate(eventGenerator::nextValues).limit(nEvent).collect(Collectors.toList());
        for (int i = 0; i < nSub; i += 1) {
            dispatcher.addSub(subscriptionGenerator.nextFilter());
        }
        Arrays.stream(dispatcher.matchers).mapToInt(m-> m.clusterSize() - nSub / (int)Math.pow(nSeg, size)).forEach(System.out::println);
        long start = System.currentTimeMillis();
        int total = 0;
        System.out.println("start: " + start);
        for (int[] e: events) {
            Set<Integer> matched = new HashSet<>();
            for (int v: dispatcher.vectorOfEvent(e)) {

               // matched.addAll(dispatcher.matchers[v].match(e));
            }
            total += matched.size();
        }
        System.out.println("use: " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("avg: " + (total / nEvent) );
    }

    public void addSub(int[][] subscription) {
        List<Integer> vectors = this.vectorOfSubscription(subscription);
        for (int v: vectors) {
            this.matchers[v].addSubscription(subscription);
        }
    }

    public List<Integer> vectorOfSubscription(int[][] subscription) {
        List<Integer> vectors = new ArrayList<>();
        vectors.add(0);
        for (int i = 0; i < subscription[0].length; i += 1) {
            int ub = subscription[1][i];
            int lb = subscription[0][i];
            int ubid = ub / this.range;
            int lbid = lb / this.range;
            if (ub == -1 || lb == -1) {
                ubid = this.nSeg - 1;
                lbid = 0;
            }
            List<Integer> next = new ArrayList<>((ubid - lbid + 1) * vectors.size());
            for (int j = lbid; j <= ubid; j += 1) {
                final int J = j;
                next.addAll(vectors.stream().map(v -> v * 10 + J).collect(Collectors.toList()));
            }
            vectors = next;
        }
        return vectors;
    }

    public List<Integer> vectorOfEvent(int[] event) {
        List<Integer> vectors = new ArrayList<>();
        vectors.add(0);
        for (int val: event) {
            if (val != -1) {
                vectors = vectors.stream().map(id-> id * 10 + val / this.range).collect(Collectors.toList());
            } else {
                List<Integer> next = new ArrayList<>(vectors.size() * this.nSeg);
                for (int i = 0; i < this.nSeg; i += 1) {
                    final int I = i;
                    next.addAll(vectors.stream().map(id -> id * 10 + I).collect(Collectors.toList()));
                }
                vectors = next;
            }
        }
        return vectors;
    }
}
