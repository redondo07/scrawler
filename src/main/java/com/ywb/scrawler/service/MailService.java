package com.ywb.scrawler.service;

public interface MailService {

    void sendAttachmentsMail(String[] to, String subject, String content, String filePath, String[] cc) throws Exception ;


}
