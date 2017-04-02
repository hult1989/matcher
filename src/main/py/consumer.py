from confluent_kafka import Consumer, KafkaError
from event_generator import Event
from sub_generator import Subscription
from struct import unpack



config = {'bootstrap.servers': 'server', 'group.id': 'pykafka', 'default.topic.config': {'auto.offset.reset': 'latest'}}
consumer = Consumer(config)

consumer.subscribe(['EVENT'])

running = True

while running:
    msg = consumer.poll()
    if not msg.error():
        print Event.fromBytes(msg.value())
    elif msg.error().code() == KafkaError._PARTITION_EOF:
        pass
    else:
        print(msg.error())
        running = False

consumer.close()



