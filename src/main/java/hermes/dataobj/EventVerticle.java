package hermes.dataobj;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by hult on 3/25/17.
 */
public class EventVerticle extends AbstractVerticle{
    KafkaProducer<String, byte[]> producer;
    Map<String, String> conf = new HashMap<>();
    EventGenerator eventGenerator;
    //final int nEVENT = 50_000;
    //String target = "broker_219.223.192.216;
    String target;
    int nLast = 0;
    int nQPS = 0;
    int time = 0;
    //String target = "EVENT";

    public EventVerticle(Properties properties) {
        String zooHost = properties.getProperty("zooHost");
        conf.put("bootstrap.servers", zooHost + ":9092");
        conf.put("acks", "1");
        eventGenerator = new EventGenerator(10);
        this.nLast = Integer.valueOf(properties.getProperty("nEventLast"));
        this.nQPS = Integer.valueOf(properties.getProperty("nEventQPS"));
        this.target = "EVENT";
    }

    @Override
    public void start() throws Exception {
        this.producer = KafkaProducer.create(vertx, this.conf, String.class, byte[].class);
        vertx.setPeriodic(1_000, id -> {
            ArrayList<Future> futureArrayList = new ArrayList<>();
            for (int i = 0; i < this.nQPS; i += 1) {
                Future f = Future.future();
                futureArrayList.add(f);
                Event event = eventGenerator.randomEvent();
                KafkaProducerRecord<String, byte[]> record =
                        KafkaProducerRecord.create(target, event.toBytes());
                //System.out.println(target + ": " + event.toString());
                this.producer.write(record, ret -> {
                    if (!ret.succeeded()) {
                        System.out.println(ret.cause());
                    } else {
                        f.complete();
                    }
                });
            }
            CompositeFuture.all(futureArrayList).setHandler(ret -> {
                if (ret.succeeded()) {
                    System.out.println("send: " + futureArrayList.size() + ", finished: " + this.time++);
                    if (this.time == this.nLast) {
                        System.out.println("all sent");
                        System.exit(0);
                    }
                }
            });
        });
    }

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("generator.cfg"));
        System.out.println(properties);
        EventVerticle g = new EventVerticle(properties);
        Vertx.vertx().deployVerticle(g);
    }
}
