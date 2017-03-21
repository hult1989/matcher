package hermes.matching;

import hermes.dataobj.EventGenerator;
import hermes.dataobj.MatchingResult;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by hult on 3/20/17.
 */
public class DummyMatcher extends AbstractVerticle{
    static KafkaProducer<String, byte[]> producer;

    @Override
    public void start() throws Exception {
        EventGenerator e = new EventGenerator(4);
        Random r = new Random();
        List<Integer> targets = Stream.generate(r::nextInt).limit(5).collect(Collectors.toList());
        MatchingResult res = new MatchingResult(e.nextEvent(), targets);
        System.out.println(res.toBytes().length);
        KafkaProducerRecord<String, byte[]> record = KafkaProducerRecord.create("RESULT",  e.nextEvent().toBytes());

        System.out.println("start write");
        vertx.setPeriodic(2_000, id -> {

            producer.write(record, done -> {
                if (done.succeeded()) {
                    System.out.println(done.result().checksum());
                    System.out.println(done.result().getOffset());
                } else {
                    System.out.println(done.cause().toString());
                }
            });
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "server:9092");
        config.put("acks", "1");
        producer = KafkaProducer.create(vertx, config, String.class, byte[].class);
        System.out.println(producer.toString());
        vertx.deployVerticle(DummyMatcher.class.getName());
    }
}
