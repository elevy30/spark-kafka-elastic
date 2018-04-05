package skes.kafka.stream.zookeeper;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZookeeperConnection {

    private ZooKeeper zooKeeper;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    ZooKeeper connect(String host, int timeout) throws InterruptedException, IOException {
        zooKeeper = new ZooKeeper(host, timeout, we -> {

            if (we.getState() == Watcher.Event.KeeperState.SyncConnected) {
                countDownLatch.countDown();
            }
        });

        countDownLatch.await();
        return zooKeeper;
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

}
