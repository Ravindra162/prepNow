package com.Auth.AuthService.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("noreply@prepnow.com");
        helper.setTo(to);
        helper.setSubject("Your OTP for PrepNow Registration");

        String emailContent = String.format(
            """
            <html>
                <body>
                    <h2>Welcome to PrepNow!</h2>
                    <p>Your OTP for email verification is: <strong>%s</strong></p>
                    <p>This OTP will expire in 5 minutes.</p>
                    <p>If you didn't request this OTP, please ignore this email.</p>
                </body>
            </html>
            """,
            otp
        );

        helper.setText(emailContent, true);
        mailSender.send(message);
    }
}
