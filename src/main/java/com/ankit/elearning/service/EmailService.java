package com.ankit.elearning.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    // The verified sender address in Brevo — set in application.properties as app.mail.from
    // This is the "From:" address shown to recipients.
    // It MUST be verified in your Brevo account under Senders & IP → Add a Sender.
    @Value("${app.mail.from:${spring.mail.username}}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC METHODS
    // ─────────────────────────────────────────────────────────────────────────

    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            send(toEmail, "🎓 Welcome to EduLearn!", buildWelcomeHtml(name));
            log.info("✅ Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Welcome email failed for {} — {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendLoginNotificationEmail(String toEmail, String name) {
        try {
            send(toEmail, "New login to your EduLearn account", buildLoginHtml(name));
            log.info("Login notification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Login notification email failed for {} — {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendEnrollmentConfirmationEmail(String toEmail, String name, String courseTitle) {
        try {
            send(toEmail, "Enrollment confirmed — " + courseTitle,
                    buildEnrollmentHtml(name, courseTitle));
            log.info("Enrollment confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Enrollment confirmation email failed for {} — {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendTestResultEmail(String toEmail, String name, String testTitle,
                                    int score, int total, double percentage, boolean passed) {
        try {
            if (passed) {
                // Try to attach PDF certificate
                try {
                    byte[] cert = CertificateGenerator.generate(name, testTitle, percentage);
                    String filename = "EduLearn_Certificate_"
                            + testTitle.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
                    sendWithAttachment(toEmail,
                            "🏆 You passed! Your Certificate of Completion — " + testTitle,
                            buildPassHtml(name, testTitle, score, total, percentage),
                            cert, filename);
                    log.info("✅ Pass email + certificate sent to: {}", toEmail);
                } catch (Exception pdfErr) {
                    // Certificate generation failed — still send email without PDF
                    log.warn("Certificate PDF failed, sending pass email without attachment: {}",
                            pdfErr.getMessage());
                    send(toEmail,
                            "🏆 You passed! — " + testTitle,
                            buildPassHtml(name, testTitle, score, total, percentage));
                    log.info("✅ Pass email (no cert) sent to: {}", toEmail);
                }
            } else {
                send(toEmail, "📘 Your Test Result — " + testTitle,
                        buildFailHtml(name, testTitle, score, total, percentage));
                log.info("✅ Fail result email sent to: {}", toEmail);
            }
        } catch (Exception e) {
            log.error("❌ Test result email failed for {} — {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String name, String resetLink) {
        try {
            send(toEmail, "🔒 Reset Your EduLearn Password",
                    buildPasswordResetHtml(name, resetLink));
            log.info("✅ Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Password reset email failed for {} — {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendReminderEmail(String toEmail, String name, String courseTitle) {
        try {
            send(toEmail, "⏰ Continue your learning: " + courseTitle,
                    buildReminderHtml(name, courseTitle));
            log.info("✅ Reminder email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Reminder email failed for {} — {}", toEmail, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE SEND HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private void send(String to, String subject, String html) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        // multipart=false because no attachment
        MimeMessageHelper h = new MimeMessageHelper(msg, false, "UTF-8");
        h.setFrom(fromEmail);
        h.setTo(to);
        h.setSubject(subject);
        h.setText(html, true);   // true = isHtml
        mailSender.send(msg);
    }

    private void sendWithAttachment(String to, String subject, String html,
                                    byte[] attachment, String filename) throws Exception {
        MimeMessage msg = mailSender.createMimeMessage();
        // multipart=true for attachment
        MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
        h.setFrom(fromEmail);
        h.setTo(to);
        h.setSubject(subject);
        h.setText(html, true);
        h.addAttachment(filename, new ByteArrayDataSource(attachment, "application/pdf"));
        mailSender.send(msg);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HTML BUILDERS
    // ─────────────────────────────────────────────────────────────────────────

    private String buildWelcomeHtml(String name) {
        return wrap("🎓 Welcome to EduLearn!",
                "<p>Dear <strong>" + esc(name) + "</strong>,</p>" +
                        "<p>Your account has been created successfully. We're excited to have you!</p>" +
                        "<table style='width:100%;margin:20px 0;border-collapse:collapse;'>" +
                        row("📚", "Browse Courses",       "Explore our published course library") +
                        row("🎬", "Video & Reading",      "Watch lectures or read materials") +
                        row("📝", "Take Assessments",     "Test your knowledge after each course") +
                        row("🏆", "Earn Certificates",    "Get a certificate when you pass!") +
                        "</table>" +
                        btn(frontendUrl + "/student/dashboard", "Start Learning →", "#4f46e5")
        );
    }

    private String buildPassHtml(String name, String testTitle,
                                 int score, int total, double pct) {
        return wrap("🏆 Congratulations — You Passed!",
                "<p>Dear <strong>" + esc(name) + "</strong>,</p>" +
                        "<p>You've successfully passed the assessment for:</p>" +
                        "<div style='background:linear-gradient(135deg,#4f46e5,#7c3aed);" +
                        "border-radius:12px;padding:20px;margin:20px 0;text-align:center;'>" +
                        "<div style='color:#c4b5fd;font-size:12px;text-transform:uppercase;letter-spacing:1px;'>Course</div>" +
                        "<div style='color:#fff;font-size:20px;font-weight:800;margin:8px 0;'>" + esc(testTitle) + "</div>" +
                        "</div>" +
                        "<table style='width:100%;border-collapse:collapse;margin:16px 0;'><tr>" +
                        scoreBox("Score",      score + " / " + total,           "#4ade80") +
                        scoreBox("Percentage", String.format("%.1f", pct) + "%","#4ade80") +
                        scoreBox("Status",     "PASSED ✓",                       "#4ade80") +
                        "</tr></table>" +
                        "<p style='color:#4ade80;font-weight:700;font-size:15px;'>🎉 Your certificate of completion is attached!</p>" +
                        "<p>Keep it up and continue expanding your knowledge.</p>" +
                        btn(frontendUrl + "/student/attempts", "View My Results →", "#4f46e5")
        );
    }

    private String buildFailHtml(String name, String testTitle,
                                 int score, int total, double pct) {
        return wrap("📘 Your Assessment Result",
                "<p>Dear <strong>" + esc(name) + "</strong>,</p>" +
                        "<p>You completed the assessment for <strong>" + esc(testTitle) + "</strong>.</p>" +
                        "<table style='width:100%;border-collapse:collapse;margin:20px 0;'><tr>" +
                        scoreBox("Score",      score + " / " + total,           "#f87171") +
                        scoreBox("Percentage", String.format("%.1f", pct) + "%","#f87171") +
                        scoreBox("Status",     "NOT PASSED ✗",                  "#f87171") +
                        "</tr></table>" +
                        "<p>Don't give up! You can retry up to <strong>3 times per day</strong> " +
                        "with a <strong>30-minute cooldown</strong> between attempts.</p>" +
                        "<p>Review the course materials carefully and try again. You've got this! 💪</p>" +
                        btn(frontendUrl + "/student/dashboard", "Back to Courses →", "#6366f1")
        );
    }

    private String buildPasswordResetHtml(String name, String resetLink) {
        return wrap("🔒 Password Reset Request",
                "<p>Dear <strong>" + esc(name) + "</strong>,</p>" +
                        "<p>We received a request to reset your EduLearn password.</p>" +
                        btn(resetLink, "Reset My Password →", "#dc2626") +
                        "<p style='color:#94a3b8;font-size:13px;'>⚠️ This link expires in <strong>1 hour</strong>. " +
                        "If you didn't request this, you can safely ignore this email.</p>"
        );
    }

    private String buildLoginHtml(String name) {
        return wrap("Account Login",
                "<p>Dear <strong>" + esc(name) + "</strong>,</p>" +
                        "<p>Your EduLearn account was just used to sign in.</p>" +
                        "<p style='color:#94a3b8;font-size:13px;'>If this was not you, reset your password immediately.</p>"
        );
    }

    private String buildEnrollmentHtml(String name, String courseTitle) {
        return wrap("Enrollment Confirmed",
                "<p>Dear <strong>" + esc(name) + "</strong>,</p>" +
                        "<p>Your enrollment is confirmed for <strong>" + esc(courseTitle) + "</strong>.</p>" +
                        "<p>Complete all course modules to unlock the final assessment.</p>" +
                        btn(frontendUrl + "/student/dashboard", "Continue Learning →", "#4f46e5")
        );
    }

    private String buildReminderHtml(String name, String courseTitle) {
        return wrap("⏰ Continue Your Learning",
                "<p>Dear <strong>" + esc(name) + "</strong>,</p>" +
                        "<p>You started <strong>" + esc(courseTitle) + "</strong> but haven't finished yet.</p>" +
                        "<p>Just 15 minutes a day makes a huge difference. " +
                        "Complete the course to unlock the assessment and earn your certificate!</p>" +
                        btn(frontendUrl + "/student/dashboard", "Continue Learning →", "#4f46e5")
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEMPLATE UTILITIES
    // ─────────────────────────────────────────────────────────────────────────

    private String wrap(String title, String body) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
                "<body style='margin:0;padding:0;background:#0f0f1a;" +
                "font-family:Segoe UI,Helvetica,Arial,sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' " +
                "style='background:#0f0f1a;padding:40px 20px;'>" +
                "<tr><td align='center'>" +
                "<table width='580' cellpadding='0' cellspacing='0' " +
                "style='background:#1a1a2e;border-radius:16px;overflow:hidden;" +
                "border:1px solid #2d2d4e;max-width:580px;width:100%;'>" +

                // Header
                "<tr><td style='background:linear-gradient(135deg,#4f46e5 0%,#7c3aed 100%);" +
                "padding:32px;text-align:center;'>" +
                "<div style='color:#ffffff;font-size:26px;font-weight:900;letter-spacing:-0.5px;'>" +
                "&#9670; EduLearn</div>" +
                "<div style='color:#c4b5fd;font-size:13px;margin-top:6px;'>Your Learning Journey</div>" +
                "</td></tr>" +

                // Title
                "<tr><td style='background:#16213e;border-bottom:1px solid #2d2d4e;padding:20px 32px;'>" +
                "<h2 style='margin:0;font-size:18px;color:#f1f5f9;font-weight:700;'>" +
                esc(title) + "</h2></td></tr>" +

                // Body
                "<tr><td style='padding:32px;color:#cbd5e1;font-size:15px;line-height:1.8;'>" +
                body + "</td></tr>" +

                // Footer
                "<tr><td style='background:#16213e;border-top:1px solid #2d2d4e;" +
                "padding:20px 32px;text-align:center;'>" +
                "<p style='margin:0;font-size:12px;color:#475569;'>" +
                "&#169; " + java.time.Year.now().getValue() + " EduLearn Platform &mdash; " +
                "This is an automated email, please do not reply.</p>" +
                "</td></tr>" +

                "</table></td></tr></table></body></html>";
    }

    private String btn(String url, String label, String bg) {
        return "<div style='text-align:center;margin:28px 0;'>" +
                "<a href='" + url + "' style='background:" + bg + ";color:#ffffff;" +
                "text-decoration:none;padding:14px 36px;border-radius:10px;" +
                "font-size:15px;font-weight:700;display:inline-block;'>" +
                esc(label) + "</a></div>";
    }

    private String scoreBox(String label, String value, String color) {
        return "<td style='padding:0 6px;'>" +
                "<div style='background:#0f0f1a;border:1px solid #2d2d4e;" +
                "border-radius:10px;padding:16px;text-align:center;'>" +
                "<div style='font-size:11px;color:#64748b;text-transform:uppercase;" +
                "letter-spacing:0.5px;margin-bottom:6px;'>" + esc(label) + "</div>" +
                "<div style='font-size:18px;font-weight:900;color:" + color + ";'>" +
                esc(value) + "</div></div></td>";
    }

    private String row(String icon, String title, String desc) {
        return "<tr><td style='padding:10px 0;border-bottom:1px solid #2d2d4e;'>" +
                "<span style='font-size:18px;margin-right:10px;'>" + icon + "</span>" +
                "<strong style='color:#e2e8f0;'>" + esc(title) + "</strong>" +
                "<span style='color:#64748b;margin-left:8px;font-size:13px;'>" +
                esc(desc) + "</span></td></tr>";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;");
    }
}
