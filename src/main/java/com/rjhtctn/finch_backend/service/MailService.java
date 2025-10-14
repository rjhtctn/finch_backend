package com.rjhtctn.finch_backend.service;

import com.rjhtctn.finch_backend.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${app.mail.sender.address}")
    private String fromEmail;

    @Value("${app.mail.sender.name}")
    private String senderName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendVerificationEmail(User user, String token) {
        String recipientAddress = user.getEmail();
        String subject = "Finch - Hesabınızı Doğrulayın";
        String confirmationUrl = String.format("%s/api/auth/verify?token=%s", frontendUrl, token);
        String message = """
                            <p>Finch hesabınızı doğrulamak için aşağıdaki bağlantıya tıklayın:</p>
                            <a href="%s">Hesabımı Doğrula</a>
                            """.formatted(confirmationUrl);

            MimeMessagePreparator mailMessage = mimeMessage -> {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
                helper.setTo(recipientAddress);
                helper.setSubject(subject);
                helper.setText(message, true);
                helper.setFrom(fromEmail, senderName);
            };
            mailSender.send(mailMessage);
    }
}