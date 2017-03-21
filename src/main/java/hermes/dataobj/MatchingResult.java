package hermes.dataobj;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by hult on 3/20/17.
 */
public class MatchingResult {
    final Event event;
    final List<Integer> targets;

    public MatchingResult(Event e, List<Integer> t) {
        this.event = e;
        this.targets = t;
    }

    public byte[] toBytes() {
        return toByteBuffer().array();
    }

    public ByteBuffer toByteBuffer() {
        byte[] eBytes = event.toBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + targets.size() * 4 + eBytes.length);
        byteBuffer.putInt(targets.size());
        for (int t: targets) {
            byteBuffer.putInt(t);
        }
        byteBuffer.put(eBytes);
        return byteBuffer;
    }

    @Override
    public String toString() {
        return String.format("event: {%s}, target: %s", event, targets);
    }

    public static MatchingResult fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return fromByteBuffer(buffer);
    }

    public static MatchingResult fromByteBuffer(ByteBuffer buffer) {
        int size = buffer.getInt();
        List<Integer> targets = new ArrayList<>(size);
        for (int i = 0; i < size; i += 1) {
            targets.add(buffer.getInt());
        }
        byte[] eBytes = new byte[buffer.remaining()];
        buffer.get(eBytes);
        return new MatchingResult(Event.fromBytes(eBytes), targets);
    }

    public static void main(String[] args) {
        Random r = new Random();
        EventGenerator generator = new EventGenerator(5);
        Event e = generator.nextEvent();
        List<Integer> target = Stream.generate(r::nextInt).limit(3).collect(Collectors.toList());
        MatchingResult matchingResult = new MatchingResult(e, target);
        System.out.println(matchingResult);
        byte[] bytes = matchingResult.toBytes();
        MatchingResult another = MatchingResult.fromBytes(bytes);
        System.out.println(another);
    }

}
