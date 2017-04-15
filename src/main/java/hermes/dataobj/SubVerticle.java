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
 * Created by hult on 3/23/17.
 */
public class SubVerticle extends AbstractVerticle{
    KafkaProducer<String, byte[]> producer;
    Map<String, String> conf = new HashMap<>();
    SubscriptionGenerator subscriptionGenerator;
    Properties cfg = new Properties();
    int time = 0;
    int nLast = 0;
    int nQPS = 0;


    public SubVerticle(Properties properties) {
        this.cfg = properties;
        String zooHost = cfg.getProperty("zooHost");
        conf.put("bootstrap.servers", zooHost + ":9092");
        conf.put("acks", "1");
        subscriptionGenerator = new SubscriptionGenerator(10);
        this.nLast = Integer.parseInt(cfg.getProperty("nSubLast"));
        this.nQPS = Integer.parseInt(cfg.getProperty("nSubQPS"));
    }
    @Override
    public void start() throws Exception {
        this.producer = KafkaProducer.create(vertx, this.conf, String.class, byte[].class);
        vertx.setPeriodic(1_000, id-> {
            List<Future> subs = new ArrayList<>();
            for (int i = 0; i < nQPS; i += 1) {
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
            CompositeFuture.all(subs).setHandler(allSub -> {
                if (allSub.succeeded()) {
                    System.out.println("send: " + subs.size() + ", finished: " + this.time++);
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
        SubVerticle g = new SubVerticle(properties);
        System.out.println(properties);
        Vertx.vertx().deployVerticle(g);
    }

}
