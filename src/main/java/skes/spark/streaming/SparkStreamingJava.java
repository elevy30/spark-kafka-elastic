package skes.spark.streaming;

import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.DataStreamWriter;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import skes.common.Constant;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.spark.sql.functions.*;

@Slf4j
//@Service
public class SparkStreamingJava {

    @Value("${spark.input.dir}")
    private String inputDir;

    @Value("${zookeeper.servers}")
    private String zookeeperServer;

    @Value("${spark.checkpoint.dir}")
    private String checkpointDir;

    @Value("${kafka.bootstrap.servers}")
    private String kafkaBootstrapServer;

    @Value("${trx.topic}")
    private String topic;

    private static String bootstrapServerStatic;
    private static String topicStatic;

    @PostConstruct
    public void init(){
        bootstrapServerStatic = kafkaBootstrapServer;
        topicStatic = topic;
        start(inputDir, checkpointDir);
    }

    public void start(String inputDir, String checkpointDir) {
        try {
            deleteTopic();

            SparkSession spark = SparkSession.builder().config("spark.sql.shuffle.partitions", 20)
//                    .config(ConfigurationOptions.ES_NET_HTTP_AUTH_USER, "elastic")
//                    .config(ConfigurationOptions.ES_NET_HTTP_AUTH_PASS, "elasticpassword")
//                    .config(ConfigurationOptions.ES_NODES, "127.0.0.1")
//                    .config(ConfigurationOptions.ES_PORT, "9200")
                    .appName("Streaming112").master("local[*]").getOrCreate();


            startQuery(inputDir, checkpointDir, spark);


            System.out.println("Waiting...");
            //noinspection InfiniteLoopStatement
            while (true) {
                //StreamingQuery[] active = spark.streams().active();
                //todo -  check this query object - the triggering flag
                //if(active.length > 0) {
                //    StreamingQuery streamingQuery = active[0];
                //    streamingQuery.stop();
                //}else{
                //    startQuery(args[0], args[1], spark);
                //}
                Thread.sleep(10000);
            }
        } catch (IOException | StreamingQueryException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deleteTopic() {
        ZkClient zkClient = new ZkClient(zookeeperServer);
        ZkConnection zkConnection = new ZkConnection(zookeeperServer, 10000);
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false );
        AdminUtils.deleteTopic(zkUtils, topic);
    }

    private static void startQuery(String inputDir, String checkpointDir, SparkSession spark) throws IOException, URISyntaxException, org.apache.spark.sql.streaming.StreamingQueryException {
        FileSystem fs = FileSystem.get(new URI(inputDir), new Configuration());
        Path inputDirPath = new Path(inputDir);
        fs.delete(inputDirPath, true);
        fs.mkdirs(inputDirPath);
        log.info("Using Input Dir dir: {}", inputDirPath);

        Path checkpointDirPath = new Path(checkpointDir);
        fs.delete(checkpointDirPath, true);
        log.info("Using checkpoint dir: {}", checkpointDirPath);


        /////// ON MESSAGE ///////
        StructType schema = new StructType(new StructField[]{
                //new StructField("batch", DataTypes.StringType, false, Metadata.empty())
                new StructField(Constant.COL_TRX_ID, DataTypes.LongType, false, Metadata.empty()),
                new StructField(Constant.COL_DATE, DataTypes.TimestampType, false, Metadata.empty()),
                new StructField(Constant.COL_PK, DataTypes.StringType, false, Metadata.empty()),
                new StructField(Constant.COL_VALUE, DataTypes.LongType, false, Metadata.empty())});


        /////////////////////////   Read stream   ////////////////////////
        log.info("-------  Reading Stream  --------");
        //https://stackoverflow.com/questions/45092445/how-to-display-a-streaming-dataframe-as-show-fails-with-analysisexception#answer-45092517
        Dataset<Row> file = spark.readStream() // .read()
                .schema(schema)
                .csv(inputDirPath.toString())
                .withWatermark(Constant.COL_DATE, "1 seconds");
        //*************************************************************************************


        /////////////////////////////////  Aggregate data //////////////////////////////
        log.info("-------  Aggregate data  --------");
        Dataset<Row> grouped = file.groupBy(window(col(Constant.COL_DATE), "1 minutes", "1 minutes"), col(Constant.COL_PK))
                //.groupBy(window(col("datetime"), "1 months", "1 days"), col("s_action"))
                .agg(
                        lit("TRX").as(Constant.COL_MSG_TYPE),
                        first(col(Constant.COL_TRX_ID)).as(Constant.COL_TRX_ID),
                        sum(col(Constant.COL_VALUE)).as("sum"),
                        count("*").as("count")
                );

        //*************************************************************************************
        //this two line cause this exception
        //http://www.waitingforcode.com/apache-spark-structured-streaming/org.apache.spark.sql.analysisexception-queries-with-streaming-sources-must-be-executed-with-writestream.start-explained/read
        //grouped.printSchema();
        //grouped.show(false);
        //*************************************************************************************

        /////////////////////////  prepare msg to kafka   ////////////////////////
        log.info("Create dataset KEY, VALUE for sending msg to kafka  (KEY - format for saving profiles to elastic from kafka)");
        Column litKey = lit("KAFKA_KEY::");
        Column primaryKeyCol = new Column(Constant.COL_PK);
        Column[] schemaProfiles = {new Column(Constant.COL_MSG_TYPE), new Column(Constant.COL_TRX_ID), new Column(Constant.COL_WIN), new Column(Constant.COL_PK), new Column(Constant.COL_SUM), new Column(Constant.COL_COUNT)};
        grouped = grouped.select(concat(litKey, to_json(struct(primaryKeyCol))).alias("key"), to_json(struct(schemaProfiles)).alias("value"));


        /////////////////////////   Write stream   ////////////////////////
        log.info("-------  Writing Stream  --------");
        DataStreamWriter<Row> dataStreamWriter = grouped.writeStream();
        //complete - will send out --- ALL ---  Aggregated window
        //append   - will send out --- ONLY --- the closed window
        //update   - will send out --- NEW WIN ONLY  ---
        dataStreamWriter = dataStreamWriter.outputMode("complete");
        dataStreamWriter = dataStreamWriter
                .option("truncate", "false")
                .option("checkpointLocation", checkpointDirPath.toString());


//        log.info("-------  Writing To Console   --------");
//        dataStreamWriter = dataStreamWriter
//                .format("console");

        log.info("-------  Writing To Kafka   --------");
        dataStreamWriter = dataStreamWriter
                .format("kafka")
                .option("kafka.bootstrap.servers", bootstrapServerStatic)
                .option("topic", topicStatic)
                .option("startingOffsets", "earliest")
                .option("endingOffsets", "latest");
        boolean isSSL = false;
        if (isSSL) {
            dataStreamWriter = dataStreamWriter
                    .option("kafka.security.protocol", "SSL")
                    .option("kafka.ssl.keystore.location", "/run/secrets/client.kafka.keystore.jks")
                    .option("kafka.ssl.keystore.password", "test123")
                    .option("kafka.ssl.key.password", "test123")
                    .option("kafka.ssl.truststore.location", "/run/secrets/kafka.truststore.jks")
                    .option("kafka.ssl.truststore.password", "test123");
        }

        log.info("-------  Start Stream  --------");
        StreamingQuery query = dataStreamWriter.trigger(Trigger.ProcessingTime("1 minutes")).start();
        //*************************************************************************************

        query.awaitTermination(1);
        //Add query to cache
        /////// ON END MESSAGE ///////
    }


    public static void main(String[] args) {
        if (args.length != 2) {
            log.info("Usage: SparkJavaStreamTest <input_dir> <checkpoint_dir>");
            System.exit(-1);
        }

        SparkStreamingJava sparkStreamingJava = new SparkStreamingJava();
        sparkStreamingJava.start(args[0], args[1]);
    }

}
