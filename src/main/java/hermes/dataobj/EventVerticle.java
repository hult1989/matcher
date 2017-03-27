package hermes.dataobj;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hult on 3/25/17.
 */
public class EventVerticle extends AbstractVerticle{
    KafkaProducer<String, byte[]> producer;
    Map<String, String> conf = new HashMap<>();
    EventGenerator eventGenerator;

    public EventVerticle() {
        conf.put("bootstrap.servers", "server:9092");
        conf.put("acks", "1");
        eventGenerator = new EventGenerator(10);

    }

    @Override
    public void start() throws Exception {
        this.producer = KafkaProducer.create(vertx, this.conf, String.class, byte[].class);
        System.out.println("All sub sent");
        ArrayList<Future> futureArrayList = new ArrayList<>();
        for (int i = 0; i < 1_00; i += 1) {
            Future f = Future.future();
            futureArrayList.add(f);
            Event event = eventGenerator.nextEvent();
            KafkaProducerRecord<String, byte[]> record =
                    KafkaProducerRecord.create("EVENT", event.toBytes());
            System.out.println("EVENT: " + event.toString());
            this.producer.write(record, ret -> {
                if (!ret.succeeded()) {
                    System.out.println(ret.cause());
                } else {
                    f.complete();
                }
            });
        }
        CompositeFuture.all(futureArrayList).setHandler(ret-> {
            if (ret.succeeded()) {
                System.out.println("all event sent...");
                System.exit(0);
            }
        });
}

    public static void main(String[] args) {
        EventVerticle g = new EventVerticle();
        Vertx.vertx().deployVerticle(g);
    }
}
