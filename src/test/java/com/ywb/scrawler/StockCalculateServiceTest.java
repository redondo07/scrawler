package com.ywb.scrawler;


import com.ywb.scrawler.model.NiceSaleListModel;
import com.ywb.scrawler.model.NiceStockInfo;
import com.ywb.scrawler.model.StockCalculatedRef;
import com.ywb.scrawler.model.StockXShoeListModel;
import com.ywb.scrawler.service.*;
import com.ywb.scrawler.service.impl.MailServiceImpl;
import com.ywb.scrawler.service.impl.StockXServiceImpl;
import com.ywb.scrawler.task.SchedulingTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StockCalculateServiceTest {

    @Resource
    private StockCalculateService stockCalculateService;
    @Resource
    private NiceStockInfoPageService niceStockInfoPageService;
    @Resource
    private NiceApiService niceApiService;
    @Resource
    private StockXService stockXService;
    @Resource
    private MailService mailService;
    @Autowired
    private SchedulingTask task;

    @Test
    public void sendMailTest() {
        task.calculateAndSendEmail();
    }

    @Test
    public void calculateDiffTest() {
        Long start = System.currentTimeMillis();
        List<StockCalculatedRef> result = stockCalculateService.calculateDiff(System.currentTimeMillis());

        System.out.println(result.size());
        System.out.println("[calculateDiffTest] calculate cost " + (System.currentTimeMillis() - start) + " ms.");

        String[] to = new String[]{};
        String fileName = "bestbuy_" + start + ".xlsx";
        String filePath = "/Users/wbyin/bestbuy/" + fileName;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String subject = "Recommendation -- " + sdf.format(date);
        try {
            mailService.sendAttachmentsMail(MailServiceImpl.toMails, subject, subject, filePath, MailServiceImpl.ccMails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // logger.info("[calculateAndSendEmail] end: {}", System.currentTimeMillis());

    }

    @Test
    public void getProductDetail2Test() {
        StockXShoeListModel model = new StockXShoeListModel();
        model.setObjectID("e90e1888-61f0-4681-8379-a4706e491235");
        stockXService.getProductDetail2(model);
    }

    @Test
    public void sendMail() {
        String[] to = new String[]{};
        String fileName = "bestbuy_1543055441438.xlsx";
        String filePath = "/Users/wbyin/bestbuy/" + fileName;
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String subject = "Recommendation Test -- " + sdf.format(date);
        try {
            mailService.sendAttachmentsMail(MailServiceImpl.toMails, subject, subject, filePath, MailServiceImpl.ccMails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // niceApiService.sendEmail();

    }


}
