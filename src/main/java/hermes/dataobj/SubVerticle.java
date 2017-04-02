package hermes.dataobj;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hult on 3/23/17.
 */
public class SubVerticle extends AbstractVerticle{
    KafkaProducer<String, byte[]> producer;
    Map<String, String> conf = new HashMap<>();
    SubscriptionGenerator subscriptionGenerator;

    public SubVerticle() {
        conf.put("bootstrap.servers", "server:9092");
        conf.put("acks", "1");
        subscriptionGenerator = new SubscriptionGenerator(10);

    }
    @Override
    public void start() throws Exception {
        int TOTAL_SUB = 20;
        this.producer = KafkaProducer.create(vertx, this.conf, String.class, byte[].class);
        List<Future> subs = new ArrayList<>();
            for( int i = 0; i < TOTAL_SUB; i += 1) {
                Future<Void> future = Future.future();
                subs.add(future);
                Subscription sub = subscriptionGenerator.randomSub();
                KafkaProducerRecord<String, byte[]> record =
                        KafkaProducerRecord.create("SUBSCRIPTION", sub.toBytes());
                //System.out.println("SUB: " + sub.toString());
                this.producer.write(record, ret -> {
                    if (!ret.succeeded()) {
                        System.out.println(ret.cause());
                    } else {
                        future.complete();
                    }
                });
            }
            CompositeFuture.all(subs).setHandler(allSub-> {
                if (allSub.succeeded()) {
                    System.out.println("all sent waiting...");
                    System.exit(0);
                }
            });
    }

    public static void main(String[] args) {
        SubVerticle g = new SubVerticle();
        Vertx.vertx().deployVerticle(g);
    }

}
