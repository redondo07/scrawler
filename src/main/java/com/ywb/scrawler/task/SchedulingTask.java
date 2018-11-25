package com.ywb.scrawler.task;

import com.ywb.scrawler.model.StockCalculatedRef;
import com.ywb.scrawler.service.MailService;
import com.ywb.scrawler.service.StockCalculateService;
import com.ywb.scrawler.service.impl.MailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class SchedulingTask {
    @Autowired
    StockCalculateService stockCalculateService;
    @Autowired
    MailService mailService;

    @Scheduled(cron = "0 0 0/1 * * *")
    public void calculateAndSendEmail(){
        long ts = System.currentTimeMillis();
        List<StockCalculatedRef> result = stockCalculateService.calculateDiff(ts);

        String[] to = new String[]{};
        String fileName = "bestbuy_" + ts + ".xlsx";
        String filePath = "/Users/wbyin/bestbuy/" + fileName;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String subject = "Recommendation -- " + sdf.format(date);
        try {
            mailService.sendAttachmentsMail(MailServiceImpl.toMails, subject, subject, filePath, MailServiceImpl.ccMails);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
