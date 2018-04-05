package skes.kafka.stream.zookeeper;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;


@Service
public class ZookeeperService  {

    @Value("${zookeeper.servers}")
    private String zkHosts;

    @Value("${zookeeper.conn.timeout}")
    private Integer connectionTimeout;

    private static ZooKeeper zooKeeper;

    private static ZookeeperConnection zookeeperConnection;

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        zookeeperConnection = new ZookeeperConnection();
        zooKeeper = zookeeperConnection.connect(zkHosts, connectionTimeout);
    }

    @PreDestroy
    public void closeConnection() throws InterruptedException {
        zookeeperConnection.close();
    }

    public void create(String path, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    }

    @SuppressWarnings("SameParameterValue")
    public Object getZNodeData(String path, boolean watchFlag) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, watchFlag);
        String data = null;
        if (stat != null) {
            byte[] dataAsBytes = zooKeeper.getData(path, null, null);
            data = new String(dataAsBytes);
        }

        return data;
    }

    @SuppressWarnings("unused")
    public List<String> getZNodeChildren(String path) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(path, true);
        List<String> znodeChildren = null;
        if (stat != null) {
            znodeChildren = zooKeeper.getChildren(path, false);
        }

        return znodeChildren;
    }

    public void delete(String path) throws KeeperException, InterruptedException {
        int version = zooKeeper.exists(path, true).getVersion();
        zooKeeper.delete(path, version);
    }
}
