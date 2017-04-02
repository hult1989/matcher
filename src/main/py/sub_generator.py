import time
from struct import pack, unpack
from random import Random
from event_generator import EventGenerator

class SubscriptionGenerator(object):
    MAX = EventGenerator.MAX
    HALF_WIDTH = MAX / 16
    def __init__(self, size):
        self.size = size
        self.id = 0
        self.rand = Random()

    def __next_filter__(self):
        filter = [[], []]
        for i in xrange(self.size):
            mid = self.rand.randint(0, self.MAX - 1)
            small = max(0, mid - self.HALF_WIDTH)
            big = min(self.MAX - 1, mid + self.HALF_WIDTH)
            filter[1].append(big)
            filter[0].append(small)
        return filter

    def __random_filter__(self):
        filter = self.__next_filter__()
        length = self.size / 2 - 2
        for i in xrange(length, self.size):
            if self.rand.random() > 0.1:
                filter[1][i] = -1
                filter[0][i] = -1
        return filter

    def nextSub(self):
        sub = Subscription(self.id, self.__next_filter__())
        self.id += 1
        return sub

    def randomSub(self):
        sub = Subscription(self.id, self.__random_filter__())
        self.id += 1
        return sub


class Subscription(object):
    def __init__(self, sid, filter, timestamp=None):
        self.sid = sid
        self.filter = filter
        self.timestamp = int(1000 *time.time()) if timestamp is None else timestamp

    def __str__(self):
        return 'id: {}\n{}\n{}'.format(self.sid, self.filter[1], self.filter[0])

    def toBytes(self):
        ret = [pack('!qii',self.timestamp, self.sid, len(self.filter[0]))]
        for ub, lb in zip(self.filter[1], self.filter[0]):
            ret.append(pack('!ii', ub, lb))
        return ''.join(ret)

    def size(self):
        return sum(filter(lambda f: f != -1, self.filter[1]))

    @staticmethod
    def fromBytes(byteStr):
        fmt = '!qii' + 'i' * ((len(byteStr) - 16) / 4)
        unp = unpack(fmt, byteStr)
        timestamp = unp[0]
        unp = unp[1:]
        ub, lb = [], []
        for i, n in enumerate(unp[2:]):
            ub.append(n) if i % 2 == 0 else lb.append(n)
        return Subscription(unp[0], [lb, ub], timestamp)


if __name__ == '__main__':
    sg = SubscriptionGenerator(10)
    for i in xrange(10):
        sub = sg.nextSub()
        print(sub)
    for i in xrange(10):
        sub = sg.randomSub()
        print(sub)

