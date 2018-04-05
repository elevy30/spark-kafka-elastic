package skes.datagen;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;

import org.springframework.stereotype.Service;
import skes.common.Constant;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by eyallevy on 08/12/17
 */
@Slf4j
@Service
public class OCBCDataStream {

    private final AtomicLong counter = new AtomicLong(0);

    @PostConstruct
    private  void runTimer() {
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        //run only in round minutes
        scheduledExecutor.scheduleAtFixedRate(new MyTask(), millisToNextMinutes(), 60*1000, TimeUnit.MILLISECONDS);

        scheduledExecutor.scheduleAtFixedRate(new MyTask(), 1L, 5000, TimeUnit.MILLISECONDS);
        // scheduledExecutor.scheduleAtFixedRate(TrxGen::writeFile, millisToNextMinutes(), 10*1000, TimeUnit.MILLISECONDS);
    }

    private long millisToNextMinutes() {
        LocalDateTime nextMinutes = LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
        return LocalDateTime.now().until(nextMinutes, ChronoUnit.MILLIS);
    }

    public class MyTask implements Runnable{

        //        private Long index;
        private String fileName = "trx";
        private String basePath = "./_resources/data/";
        private String header   = Constant.COL_TRX_ID +"," + Constant.COL_DATE + ","+ Constant.COL_PK + "," + Constant.COL_VALUE + "\n";

        @Override
        public void run() {

                long offset = counter.incrementAndGet();

                SparkSession spark = SparkSession.builder()
                        .appName("Streaming112")
                        .master("local[*]").getOrCreate();

                //ReadFile
                Dataset<Row> csv = spark.read().csv("./_resources/ocbc/fot.csv");

                /*UDF1 increasMonthByOffset = new UDF1<Date, String>() {
                    public String call(final Date date) throws Exception {
                        return str.toUpperCase();
                    }
                };*/

                //change data
                csv.withColumn("datetime", col("BUSINESSDATE").plus(offset));
                //write file to spark streaming location

                csv.show();
/*
                for (int i = 0; i < 3; i++) {
                    Date date = new Date();
                    log.info("HHHH " + date + "HHHH");
                    //index = index + 1;
                    String filePath = basePath + fileName + date.getTime();
                    log.info("file path:" + filePath);

                    String content = header;
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(date);
                    if( i%3 == 1 ){
                        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                        time = localDateTime.minusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
                    }
                    content = content + trxId.incrementAndGet() + "," + time + ",123,100\n";
                    //content = content +  i + ",456," + newString + ",100\n";
                    log.info("content:\n" + content);

                    BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
                    bw.write(content);
                    bw.close();

                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/

        }
    }

    @SuppressWarnings("unused")
    private String roundMinutes(int sec) {
        Clock minuteTickingClock = Clock.tickMinutes(ZoneId.systemDefault());
        LocalDateTime now = LocalDateTime.now(minuteTickingClock);
        LocalDateTime roundCeiling = now.plusSeconds(sec);
        return roundCeiling.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
    }

    public static void main(String[] args) {
        long offset = 10;

        SparkSession spark = SparkSession.builder()
                .appName("Streaming112")
                .master("local[*]").getOrCreate();

        //ReadFile
        Dataset<Row> csv = spark.read().option("header",true).csv("./_resources/ocbc/fot.csv");

                /*UDF1 increasMonthByOffset = new UDF1<Date, String>() {
                    public String call(final Date date) throws Exception {
                        return str.toUpperCase();
                    }
                };*/

        //change data
        csv = csv.withColumn("datetime", col("BUSINESSDATE").plus(offset));
        //write file to spark streaming location

        csv.show();
    }
}
