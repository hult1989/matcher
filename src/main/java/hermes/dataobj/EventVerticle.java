package hermes.dataobj;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hult on 3/25/17.
 */
public class EventVerticle extends AbstractVerticle{
    KafkaProducer<String, byte[]> producer;
    Map<String, String> conf = new HashMap<>();
    EventGenerator eventGenerator;
    //final int nEVENT = 50_000;
    int nEVENT;
    //String target = "broker_219.223.192.216;
    String target;
    int nTime = 0;
    //String target = "EVENT";

    public EventVerticle(String zookafka) {
        conf.put("bootstrap.servers", zookafka + ":9092");
        conf.put("acks", "1");
        eventGenerator = new EventGenerator(10);

    }

    @Override
    public void start() throws Exception {
        this.producer = KafkaProducer.create(vertx, this.conf, String.class, byte[].class);
        vertx.setPeriodic(1_000, id-> {
            ArrayList<Future> futureArrayList = new ArrayList<>();
            long start = System.currentTimeMillis();
            System.out.println("start: " + start);
            for (int i = 0; i < this.nEVENT; i += 1) {
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
                    System.out.println("finish: " + this.nTime++);
                    System.out.println("use: " + (System.currentTimeMillis() - start));
                    if (this.nTime == 10) {
                        System.exit(0);
                    }
                }
            });
        });
}

    public static void main(String[] args) {
        Arrays.stream(args).forEach(System.out::println);
        String target = "EVENT";
        int num = 1;
        String zooHost = "server";
        if (args.length > 0) {
            target = args[0];
            num = Integer.valueOf(args[1]);
            zooHost = args[2];
        }
        EventVerticle g = new EventVerticle(zooHost);
        g.nEVENT = num;
        g.target = target;
        Vertx.vertx().deployVerticle(g);
    }
}
