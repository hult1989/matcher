package hermes.matching;

import hermes.dataobj.Event;
import hermes.dataobj.EventGenerator;
import hermes.dataobj.Subscription;
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


    public Dispatcher(int size, int nSeg, boolean standalone) {
        this.size = size;
        this.nSeg = nSeg;
        this.range = MAX / nSeg;
        if (standalone) {
            this.matchers = Stream.generate(() -> new Matcher(size)).limit((int) Math.pow(nSeg, size)).toArray(Matcher[]::new);
        }
    }
    public static void main(String[] args) {
        int size = 10;
        int nSub = 70_000;
        int nEvent = 1_000;
        int nSeg = 1;
        Dispatcher dispatcher = new Dispatcher(size, nSeg, true);
        //Dispatcher dispatcher = new Dispatcher(size, nSeg, false);
        SubscriptionGenerator subscriptionGenerator = new SubscriptionGenerator(size);

        /*
        Map<Integer, Integer> map = new HashMap<>();
        int total = 0;
        for (int i = 0; i < nSub; i += 1) {
            System.out.println("processing: " + i);
            Subscription sub = subscriptionGenerator.nextSub();
            List<Integer> vectors = dispatcher.vectorOfSubscription(sub.filter);
            vectors = vectors.stream().map(v -> v / (1024 * 32)).distinct().collect(Collectors.toList());
            total += vectors.size();
            for (int v: vectors) {
                map.compute(v, (key, value) -> value == null ? 1 : value + 1);
            }
        }
        System.out.println(map);
        System.out.println(total);
        System.out.println(map.values().stream().reduce((a, b)-> a + b));
        */

        EventGenerator eventGenerator = new EventGenerator(size);
        List<Event> events = Stream.generate(eventGenerator::randomEvent).limit(nEvent).collect(Collectors.toList());
        int lenAvg = 0;
        for (int i = 0; i < nSub; i += 1) {
            Subscription sub = subscriptionGenerator.randomSub();
            System.out.printf("processing: %s, length: %s\n", i, sub.length());
            lenAvg += sub.length();
            dispatcher.dispatchSubscription(sub);
        }
        long start = System.currentTimeMillis();
        int total = 0;
        System.out.println("start: " + start);
        for (Event e: events) {
            Set<Integer> matched = new HashSet<>();
            matched.addAll(dispatcher.findMatched(e));
            total += matched.size();
        }
        System.out.println("use: " + (System.currentTimeMillis() - start) + " ms");
        System.out.println("avg: " + (total / nEvent) );
        System.out.println("sub length avg: " + (float)lenAvg / nSub);
        new Scanner(System.in).next();
        /*
        */
    }

    private void dispatchFilter(int[][] subscription) {
        List<Integer> vectors = this.vectorOfSubscription(subscription);
        for (int v: vectors) {
            this.matchers[v].addFilter(subscription);
        }
    }

    public void dispatchSubscription(Subscription sub) {
        List<Integer> vectors = this.vectorOfSubscription(sub.filter);
        for (int v: vectors) {
            this.matchers[v].addSubscription(sub);
        }
    }

    public Set<Integer> findMatched(Event e) {
        Set<Integer> matched = new HashSet<>();
        List<Integer> vectors = this.vectorOfEvent(e.values);
        for (int v: vectors) {
            matched.addAll(this.matchers[v].match(e));
        }
        return matched;
    }

    public List<Integer> vectorOfSubscription(int[][] filter) {
        List<Integer> vectors = new ArrayList<>();
        vectors.add(0);
        for (int i = 0; i < filter[0].length; i += 1) {
            int ub = filter[1][i];
            int lb = filter[0][i];
            int ubid = ub / this.range;
            int lbid = lb / this.range;
            if (ub == -1 || lb == -1) {
                ubid = this.nSeg - 1;
                lbid = 0;
            }
            List<Integer> next = new ArrayList<>((ubid - lbid + 1) * vectors.size());
            for (int j = lbid; j <= ubid; j += 1) {
                final int J = j;
                next.addAll(vectors.stream().map(v -> v * this.nSeg + J).collect(Collectors.toList()));
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
                vectors = vectors.stream().map(id-> id * this.nSeg + val / this.range).collect(Collectors.toList());
            } else {
                List<Integer> next = new ArrayList<>(vectors.size() * this.nSeg);
                for (int i = 0; i < this.nSeg; i += 1) {
                    final int I = i;
                    next.addAll(vectors.stream().map(id -> id * this.nSeg + I).collect(Collectors.toList()));
                }
                vectors = next;
            }
        }
        return vectors;
    }
}
