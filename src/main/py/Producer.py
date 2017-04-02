from confluent_kafka import Producer
from event_generator import EventGenerator
from sub_generator import SubscriptionGenerator
import json
import time


producer = Producer({'bootstrap.servers': 'server', 'queue.buffering.max.messages': 2000000})

#target = "broker_219.223.196.132"
target = "EVENT"


if __name__ == '__main__':
    eg = EventGenerator(10)
    sg = SubscriptionGenerator(10)
    for i in xrange(10):
        e = eg.randomEvent()
        print(e)
        #producer.produce(target, json.dumps({'a': 33, 'e': time.time()}))
        producer.produce(target, e.toBytes())
    '''
    for i in xrange(1000000):
        sub = sg.randomSub()
        print('pricessing: {}'.format(sub.sid))
        producer.produce('SUBSCRIPTION', sub.toBytes())
    '''

    producer.flush()
