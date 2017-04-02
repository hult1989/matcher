import random
import time
import struct


class EventGenerator(object):
    MAX = 1000
    def __init__(self, size):
        self.size = size
        self.rand = random.Random()
        self.id = 0

    def __next_values__(self):
        return [self.rand.randint(0, EventGenerator.MAX - 1) for i in xrange(self.size)]

    def __random_values(self):
        return [self.rand.randint(0, EventGenerator.MAX - 1) if self.rand.random() > 0.5
        else -1 for i in xrange(self.size)]

    def randomEvent(self):
        e = Event(self.id, self.__random_values())
        self.id += 1
        return e



    def nextEvent(self):
        e = Event(self.id, self.__next_values__())
        self.id += 1
        return e



class Event(object):

    def __init__(self, eid, values, timestamp=None):
        self.timestamp = int(1000 *time.time()) if timestamp is None else timestamp
        self.eid = eid
        self.values = values

    def toBytes(self):
        return struct.pack('!qii', self.timestamp, self.eid, len(self.values))\
       + struct.pack('!' + 'i'*len(self.values), *self.values)

    def size(self):
        return len(filter(lambda v: v != -1, self.values))

    def __str__(self):
        return 'id: {}, values: {}, timestamp: {}'.format(self.eid, self.values, self.timestamp)

    @staticmethod
    def fromBytes(bytes):
        val_len = (len(bytes) - 8 - 4 - 4) / 4
        fmt = '!qii' + 'i' * val_len
        unp = struct.unpack(fmt, bytes)
        ret = Event(unp[1], unp[3:], unp[0])
        return ret




if __name__ == '__main__':
    import random
    eg = EventGenerator(1)
    e = eg.nextEvent()
    for s in e.toBytes():
        print(int(s, 10))


