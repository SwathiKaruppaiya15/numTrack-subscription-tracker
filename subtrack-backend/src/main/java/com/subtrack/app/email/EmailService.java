package com.subtrack.app.email;

import com.subtrack.app.entity.Reminder;
import com.subtrack.app.entity.Subscription;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ── Reminder email (Prompt 37) ───────────────────────────
    @Async
    public void sendReminderEmail(Reminder reminder) {
        Subscription sub = reminder.getSubscription();
        String userEmail = reminder.getUser().getEmail();
        String userName  = reminder.getUser().getFullName();

        int daysLeft = reminder.getReminderType().name().equals("ONE_DAY") ? 1 : 3;

        Context ctx = new Context();
        ctx.setVariable("userName",    userName);
        ctx.setVariable("subName",     sub.getName());
        ctx.setVariable("amount",      sub.getCurrency() + sub.getAmount());
        ctx.setVariable("billingDate", sub.getNextBillingDate().toString());
        ctx.setVariable("daysLeft",    daysLeft);
        ctx.setVariable("dashboardUrl", frontendUrl + "/dashboard");

        String subject = "⏰ " + sub.getName() + " billing in " + daysLeft + " day" + (daysLeft > 1 ? "s" : "");
        sendHtmlEmail(userEmail, subject, "reminder-email", ctx);
    }

    // ── Payment failure alert (Prompt 38) ────────────────────
    @Async
    public void sendPaymentFailureAlert(Subscription sub, String failureReason) {
        String userEmail = sub.getUser().getEmail();
        String userName  = sub.getUser().getFullName();

        Context ctx = new Context();
        ctx.setVariable("userName",      userName);
        ctx.setVariable("subName",       sub.getName());
        ctx.setVariable("amount",        sub.getCurrency() + sub.getAmount());
        ctx.setVariable("failureReason", failureReason);
        ctx.setVariable("dashboardUrl",  frontendUrl + "/subscriptions");

        String subject = "❌ Payment failed for " + sub.getName();
        sendHtmlEmail(userEmail, subject, "payment-failure-email", ctx);
    }

    // ── Welcome email ────────────────────────────────────────
    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        Context ctx = new Context();
        ctx.setVariable("userName",    fullName);
        ctx.setVariable("dashboardUrl", frontendUrl + "/dashboard");

        sendHtmlEmail(toEmail, "Welcome to SubTrack 🎉", "welcome-email", ctx);
    }

    // ── Core send method ─────────────────────────────────────
    private void sendHtmlEmail(String to, String subject, String template, Context ctx) {
        try {
            String html = templateEngine.process(template, ctx);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent: '{}' → {}", subject, to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Email send failed", e);
        }
    }
}
