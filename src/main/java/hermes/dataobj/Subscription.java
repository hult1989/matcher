package hermes.dataobj;

/**
 * Created by hult on 3/19/17.
 */
public class Subscription {
    public final int id;
    public final int[][] filter;

    public Subscription(int id, int[][] filter) {
        this.id = id;
        this.filter = filter;
    }
}
