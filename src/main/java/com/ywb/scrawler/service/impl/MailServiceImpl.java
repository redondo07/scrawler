package com.ywb.scrawler.service.impl;

import com.ywb.scrawler.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.io.File;

@Slf4j
@Component
public class MailServiceImpl implements MailService {
    public static final String[] toMails = new String[]{"chenlongjiu2@163.com", "yangqi29@hotmail.com"};
    public static final String[] ccMails = new String[]{"redondo.198679@hotmail.com", "ywb_redondo@163.com"};
    public static final String from = "ywb_redondo@163.com";

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendAttachmentsMail(String[] to, String subject, String content, String filePath, String[] cc) throws Exception  {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        helper.setCc(cc);

        FileSystemResource file = new FileSystemResource(new File(filePath));
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator));
        helper.addAttachment(fileName, file);

        mailSender.send(message);

    }
}
