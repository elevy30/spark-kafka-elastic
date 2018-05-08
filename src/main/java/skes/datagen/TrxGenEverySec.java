package skes.datagen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import skes.common.Constant;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by eyallevy on 08/12/17
 */
@Slf4j
@Service
public class TrxGenEverySec {

    private final AtomicLong trxId = new AtomicLong(0);

    @PostConstruct
    private  void runTimer() {
        ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(new MyTask(), 1L, 5*1000, TimeUnit.MILLISECONDS);
    }

    public class MyTask implements Runnable{
//        private Long index;
        private String fileName = "trx";
        private String basePath = "./_resources/data/";
        private String header   = Constant.COL_TRX_ID +"," + Constant.COL_DATE + ","+ Constant.COL_PK + "," + Constant.COL_VALUE + "\n";

        @Override
        public void run() {
            try {

                    Date date = new Date();
                    log.info("HHHH " + date + "HHHH");
                    //index = index + 1;
                    String filePath = basePath + fileName + date.getTime();
                    log.info("file path:" + filePath);

                    String content = header;
                    String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(date);

                    content = content + trxId.incrementAndGet() + "," + time + ",123,100\n";
                    content = content + trxId.incrementAndGet() + "," + time + ",145,100\n";
                    content = content + trxId.incrementAndGet() + "," + time + ",156,100\n";
                    content = content + trxId.incrementAndGet() + "," + time + ",178,100\n";
                    content = content + trxId.incrementAndGet() + "," + time + ",199,100\n";
                    //content = content +  i + ",456," + newString + ",100\n";
                    log.info("content:\n" + content);

                    BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
                    bw.write(content);
                    bw.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        TrxGenEverySec trxGen = new TrxGenEverySec();
        trxGen. runTimer();
    }
}
