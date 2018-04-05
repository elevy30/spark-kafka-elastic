package skes.spark.streaming;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.elasticsearch.hadoop.cfg.ConfigurationOptions;

@Slf4j
public class SparkManagement {

    public static void main(String[] args) {
        try {

            SparkSession spark = SparkSession.builder()
                    .config("spark.sql.shuffle.partitions", 20)
                    .config(ConfigurationOptions.ES_NET_HTTP_AUTH_USER, "elastic")
                    .config(ConfigurationOptions.ES_NET_HTTP_AUTH_PASS, "elasticpassword")
                    .config(ConfigurationOptions.ES_NODES, "127.0.0.1")
                    .config(ConfigurationOptions.ES_PORT, "9200")
                    .appName("Streaming112")
                    .master("local[*]")
                    .getOrCreate();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
