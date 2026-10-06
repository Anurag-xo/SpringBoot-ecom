package in.anurag.CreatorStore.services;

import in.anurag.CreatorStore.entities.Order;
import in.anurag.CreatorStore.entities.Product;
import in.anurag.CreatorStore.entities.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Async
  public void sendOrderConfirmationEmail(User user, Order order) {
    log.info("Preparing to send order confirmation email to: {}", user.getEmail());
    sendEmail(
        user.getEmail(),
        "Order Confirmation - CreatorStore",
        "email/order-confirmation",
        user,
        order);
  }

  @Async
  public void sendOrderCancellationEmail(User user, Order order) {
    log.info("Preparing to send order cancellation email to: {}", user.getEmail());
    sendEmail(
        user.getEmail(), "Order Cancelled - CreatorStore", "email/order-cancellation", user, order);
  }

  private void sendEmail(String to, String subject, String templateName, User user, Order order) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom("noreply@creatorstore.com"); // Use your verified sender email

      // Process Thymeleaf template
      Context context = new Context();
      context.setVariable("user", user);
      context.setVariable("order", order);
      String htmlContent = templateEngine.process(templateName, context);

      helper.setText(htmlContent, true); // true = isHtml
      mailSender.send(message);

      log.info("✅ Email sent successfully to: {}", to);
    } catch (MessagingException e) {
      log.error("❌ Failed to send email to: {}. Error: {}", to, e.getMessage());
      // Note: We don't throw the exception here to prevent it from crashing the main thread,
      // but in production, you might want to log this to a monitoring system (e.g., Sentry).
    }
  }

  @Async
  public void sendLowStockAlertEmail(String adminEmail, List<Product> lowStockProducts) {
    log.info("Preparing to send low stock alert to: {}", adminEmail);
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(adminEmail);
      helper.setSubject("⚠️ Low Stock Alert - CreatorStore");
      helper.setFrom("noreply@creatorstore.com");

      Context context = new Context();
      context.setVariable("products", lowStockProducts);
      String htmlContent = templateEngine.process("email/low-stock-alert", context);

      helper.setText(htmlContent, true);
      mailSender.send(message);

      log.info("✅ Low stock alert sent successfully to: {}", adminEmail);
    } catch (MessagingException e) {
      log.error("❌ Failed to send low stock alert to: {}. Error: {}", adminEmail, e.getMessage());
    }
  }
}
