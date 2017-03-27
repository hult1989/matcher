package hermes.matching;

import hermes.dataobj.Event;
import hermes.dataobj.Subscription;

import java.util.*;

/**
 * Created by hult on 3/10/17.
 */
public class Matcher {

    private Pairwise[][] subspaces;
    private Subscriptions subscriptions;

    /*
    public int[][] getSub(int id) {
        return subscriptions.getSubById(id);
    }
    */

    public int clusterSize() {
        return subscriptions.sizeMap.size();
    }
    //quad <subId, attrIndex, lowerBound, upperBound>
    ArrayList<int[]> singleAttributeSubscription;
    final int ATTRIBUTE_SPACE_SIZE;

    public Matcher(int attrSize) {
        this.ATTRIBUTE_SPACE_SIZE = attrSize;
        this.subscriptions = new Subscriptions();
        this.singleAttributeSubscription = new ArrayList<>();
        this.subspaces = new Pairwise[ATTRIBUTE_SPACE_SIZE][ATTRIBUTE_SPACE_SIZE];
        for (int i = 0; i < ATTRIBUTE_SPACE_SIZE - 1; i += 1) {
            for (int j = i + 1; j < ATTRIBUTE_SPACE_SIZE; j += 1) {
                this.subspaces[i][j] = new Pairwise();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("size: " + ATTRIBUTE_SPACE_SIZE + "\n");
        builder.append("map: " + subscriptions.toString() + "\n");
        for (int i = 0; i < ATTRIBUTE_SPACE_SIZE; i += 1) {
            for (int j = 0; j < ATTRIBUTE_SPACE_SIZE; j += 1) {
                if (subspaces[i][j] == null) continue;
                builder.append(String.format("%s&%s:\n%s\n", i, j, subspaces[i][j]));
            }
        }
        return builder.toString();
    }

    public void addSubscription(Subscription sub) {
        if (this.subscriptions.add(sub)) {
            this.addFilter(sub.id, sub.filter);
        }
    }

    public void addFilter(int[][] sub) throws RuntimeException{
        if (sub[0].length != ATTRIBUTE_SPACE_SIZE)
            throw new RuntimeException("subscription length != " + ATTRIBUTE_SPACE_SIZE);
        int id = this.subscriptions.add(sub);
        this.addFilter(id, sub);
    }

    private void addFilter(int id, int[][] sub) throws RuntimeException {
        //System.out.println("PROCESS " + id);
        LinkedList<Integer> attrIndex = new LinkedList<>();
        for (int i = 0; i < sub[0].length; i += 1) {
            if (sub[0][i] != -1) attrIndex.add(i);
        }
        if (attrIndex.isEmpty()) return;
        if (attrIndex.size() == 1) {
            int index = attrIndex.peekFirst();
            singleAttributeSubscription.add(new int[]{id, index, sub[0][index], sub[1][index]});
        } else {
            int i = attrIndex.pollFirst();
            while (attrIndex.size() > 1) {
                int j = attrIndex.pollFirst();
                updatePairWise(i, j, false, id, sub);
                i = j;
            }
            updatePairWise(i, attrIndex.pollFirst(), true, id, sub);
        }
    }

    public Set<Integer> match(Event event) {
        return match(event.values);
    }

    public Set<Integer> match(int[] eValues) {
        //System.out.println("-----------------match-------------");
        Set<Integer> matchedSubscriptions = new HashSet<>();
        Map<Integer, Integer> counter = new HashMap<>();
        for (int i = 0; i < eValues.length - 1; i += 1) {
            if (eValues[i] == -1) continue;
            for (int j = i + 1; j < eValues.length; j += 1) {
                if (eValues[j] == -1) continue;
                List<Integer> conMatchedSubIDs = matchingBoundList(i, j, eValues);
                if (conMatchedSubIDs.size() == 0) continue;
                //System.out.printf("conMatchedSubIDs[%s/%s]: %s\n", i, j, conMatchedSubIDs);
                for (int id: conMatchedSubIDs) {
                    counter.compute(id, (k, v) -> v == null ? 1 : 1 + v);
                }
            }
        }
        //System.out.println("counter" + counter);
        for (int id: counter.keySet()) {
            if (counter.get(id) == 2 * subscriptions.sizeMap.get(id)) {
                matchedSubscriptions.add(id);
            }
        }
        return matchedSubscriptions;
    }

    private List<Integer> matchingBoundList(int i, int j, int[] event) {
        List<Integer> ret = new ArrayList<>();
        if (subspaces[i][j] != null) {
            ret = subspaces[i][j].search(event[i], event[j]);
        }
        return ret;
    }


    private void updatePairWise(int i, int j, boolean both, int id, int[][] sub) {
        //System.out.printf("Update A%sA%s\n", i, j);
        if (subspaces[i][j] == null) subspaces[i][j] = new Pairwise();
        Pairwise pair = subspaces[i][j];
        int[] boundI = new int[]{sub[0][i], sub[1][i]};
        int[] boundJ = new int[]{sub[0][j], sub[1][j]};
        pair.addSub(both, id, boundI, boundJ);
        //System.out.println("first: " + pair.first);
        //System.out.println("second: " + pair.second);
    }

    public static void main(String[] args) {
        int[][] s0 = new int[][]{{-1, 3, 2, -1, 9, -1}, {-1, 7, 8, -1, 14, -1}};
        int[][] s1 = new int[][]{{-1, 7, 2, 1, -1, -1}, {-1, 12, 7, 8, -1, -1}};
        int[][] s2 = new int[][]{{-1, 2, -1, 13, 6, -1}, {-1, 9, -1, 16, 10, -1}};
        int[][] s3 = new int[][]{{-1, -1, 7, 3, -1, 8}, {-1, -1, 13, 6, -1, 17}};
        int[][] s4 = new int[][]{{-1, 17, 3, -1, -1, 12}, {-1, 19, 9, -1, -1, 18}};
        int[][] s5 = new int[][]{{-1, -1, 16, 13, 4, -1}, {-1, -1, 18, 17, 9, -1}};
        int[][] s6 = new int[][]{{-1, -1, -1, 11, 4, 4}, {-1, -1, -1, 19, 16, 14}};
        int[][] s7 = new int[][]{{-1, 2, -1, -1, 7, 15}, {-1, 13, -1, -1, 12, 20}};
        int[][] s8 = new int[][]{{-1, 6, -1, 11, -1, 13}, {-1, 11, -1, 14, -1, 18}};
        int[][] s9 = new int[][]{{-1, -1, 8, -1, 2, 2}, {-1, -1, 11, -1, 4, 13}};
        ArrayList<int[][]> subscriptions = new ArrayList<>();
        subscriptions.add(s0);
        subscriptions.add(s1);
        subscriptions.add(s2);
        subscriptions.add(s3);
        subscriptions.add(s4);
        subscriptions.add(s5);
        subscriptions.add(s6);
        subscriptions.add(s7);
        subscriptions.add(s8);
        subscriptions.add(s9);
        Matcher matcher = new Matcher(6);
        for (int[][] subscription : subscriptions) {
            matcher.addFilter(subscription);
        }
        /*
        System.out.println("sizeMap: " + matcher.subscriptions.sizeMap);
        for (int i = 1; i < matcher.ATTRIBUTE_SPACE_SIZE - 1; i += 1) {
            for (int j = i + 1; j < matcher.ATTRIBUTE_SPACE_SIZE; j += 1) {
                matching.Pairwise p = matcher.subspaces[i][j];
                if (p != null) {
                    System.out.println("" + i + "/" + j);
                    if (p.first != null) System.out.println("FIRST: \n" + p.first);
                    if (p.second != null) System.out.println("SECOND: \n" + p.second);
                }

            }
        }
                */
        int[] event = new int[]{-1, 5, 4, -1, 13, 19};
        System.out.println(matcher.match(event));
    }
}
