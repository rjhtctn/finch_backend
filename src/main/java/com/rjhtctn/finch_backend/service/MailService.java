package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.sender.address}")
    private String fromEmail;

    @Value("${app.mail.sender.name}")
    private String senderName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(User user, String token) {
        sendTransactionalEmail(user, token, EmailType.VERIFICATION);
    }

    public void sendPasswordResetEmail(User user, String token) {
        sendTransactionalEmail(user, token, EmailType.PASSWORD_RESET);
    }

    private void sendTransactionalEmail(User user, String token, EmailType emailType) {
        try {
            String recipient = user.getEmail();
            String htmlContent = buildEmailContent(user, token, emailType);

            sendHtmlMail(recipient, emailType.subject, htmlContent);
        } catch (Exception e) {
            throw new MailSendException(emailType.name() + " mail could not be sent", e);
        }
    }

    private String buildEmailContent(User user, String token, EmailType emailType) {
        String username = HtmlUtils.htmlEscape(user.getUsername());
        String link = String.format("%s%s?token=%s", frontendUrl, emailType.path, token);

        return """
                <p>Merhaba <strong>%s</strong>,</p>
                <p>%s</p>
                <p><a href="%s" target="_blank" rel="noopener noreferrer">%s</a></p>
                <p>%s</p>
                """.formatted(username, emailType.title, link, emailType.linkText, emailType.validityInfo);
    }

    private void sendHtmlMail(String to, String subject, String htmlContent) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(fromEmail, senderName);
        };
        mailSender.send(messagePreparator);
    }
}

enum EmailType {
    VERIFICATION("Finch - Hesabınızı Doğrulayın",
            "Finch hesabınızı doğrulamak için aşağıdaki bağlantıya tıklayın:",
            "Hesabımı Doğrula",
            "Bu bağlantı 24 saat geçerlidir.",
            "/api/auth/verify"),
    PASSWORD_RESET("Finch - Şifrenizi Sıfırlayın",
            "Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:",
            "Şifre Sıfırla",
            "Bu bağlantı 10 saat geçerlidir.",
            "/api/auth/reset-password");

    final String subject;
    final String title;
    final String linkText;
    final String validityInfo;
    final String path;

    EmailType(String subject, String title, String linkText, String validityInfo, String path) {
        this.subject = subject;
        this.title = title;
        this.linkText = linkText;
        this.validityInfo = validityInfo;
        this.path = path;
    }
}