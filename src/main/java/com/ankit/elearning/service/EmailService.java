package com.ankit.elearning.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String LOGO_URL = "https://dbp.com.sg/wp-content/uploads/2020/09/dbp-logo.svg";
    private static final String DASHBOARD_URL = "http://localhost:3000/dashboard";

    // ----- WELCOME EMAIL (HTML) -----
    @Async
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Welcome to E-Learning Platform!");

            String htmlContent = buildWelcomeHtml(name);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Welcome email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email: " + e.getMessage());
        }
    }

    // ----- TEST RESULT EMAIL (HTML) -----
    @Async
    public void sendTestResultEmail(String toEmail, String name, String testTitle,
                                    int score, int total, double percentage, boolean passed) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(passed ? "🎉 Test Passed!" : "📚 Test Result");

            String htmlContent = buildTestResultHtml(name, testTitle, score, total, percentage, passed);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("✅ Test result email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send test result email: " + e.getMessage());
        }
    }

    // ---------- HTML TEMPLATES ----------
    private String buildWelcomeHtml(String name) {
        return wrapInTemplate(
                "Welcome to E-Learning!",
                "<p style='font-size:12px; color:#333;'>Dear <b>" + escapeHtml(name) + "</b>,</p>" +
                        "<p style='font-size:12px; color:#555;'>Your account has been created successfully.</p>" +
                        "<p style='font-size:12px; color:#555;'>You can now:</p>" +
                        "<ul style='font-size:12px; color:#555;'>" +
                        "<li>Browse and enroll in courses</li>" +
                        "<li>Complete modules and take tests</li>" +
                        "<li>Track your progress</li>" +
                        "</ul>" +
                        "<p style='font-size:12px; color:#555;'>" +
                        "<a href='" + DASHBOARD_URL + "' style='color:#007bff; text-decoration:underline;'>Click here</a> " +
                        "to login to your dashboard and start learning.</p>" +
                        "<p style='font-size:12px; color:#555;'>Happy Learning!</p>"
        );
    }

    private String buildTestResultHtml(String name, String testTitle, int score, int total,
                                       double percentage, boolean passed) {
        String resultText = passed ? "Congratulations! You passed the test." : "You did not pass this time. Keep learning and try again.";
        return wrapInTemplate(
                passed ? "🎉 Test Passed!" : "📚 Test Result",
                "<p style='font-size:12px; color:#333;'>Dear <b>" + escapeHtml(name) + "</b>,</p>" +
                        "<p style='font-size:12px; color:#555;'>You have completed the test:</p>" +
                        "<p style='font-size:14px; font-weight:bold; color:#d44a8a;'>" + escapeHtml(testTitle) + "</p>" +
                        "<p style='font-size:12px; color:#555;'>Your score: <b>" + score + "/" + total + "</b> (" + String.format("%.1f", percentage) + "%)</p>" +
                        "<p style='font-size:12px; color:#555;'>" + resultText + "</p>" +
                        "<p style='font-size:12px; color:#555;'>" +
                        "<a href='" + DASHBOARD_URL + "' style='color:#007bff; text-decoration:underline;'>Click here</a> " +
                        "to view your detailed results.</p>" +
                        "<p style='font-size:12px; color:#555;'>Thank you for using E-Learning!</p>"
        );
    }

    // ----- Common email wrapper with color strips and logo -----
    private String wrapInTemplate(String title, String bodyContent) {
        return "<table style='border-collapse: collapse; width: 100%; max-width: 600px; margin: 0 auto; font-family: Verdana, Arial, sans-serif;'>" +
                "<!-- Top Color Strip -->" +
                "<tr><td>" +
                "<table style='width:100%; border-collapse:collapse;'>" +
                "<tr>" +
                "<td style='background-color:#00d4ff;height:3px;width:16%;'></td>" +
                "<td style='background-color:#29c4f2;height:3px;width:17%;'></td>" +
                "<td style='background-color:#6a9fd6;height:3px;width:17%;'></td>" +
                "<td style='background-color:#a872b8;height:3px;width:17%;'></td>" +
                "<td style='background-color:#d44a8a;height:3px;width:17%;'></td>" +
                "<td style='background-color:#e91e63;height:3px;width:16%;'></td>" +
                "</tr></table></td></tr>" +

                "<!-- Logo -->" +
                "<tr><td style='background-color:#ffffff; padding:25px 20px; text-align:center;'>" +
                "<img src='" + LOGO_URL + "' width='115' height='40' alt='DBP Logo' />" +
                "<p style='margin:10px 0 0 0; font-size:15px; font-weight:bold; color:#333; letter-spacing:1px;'>" +
                "DIGITAL BUSINESS PEOPLE</p></td></tr>" +

                "<!-- Content -->" +
                "<tr><td style='background-color:#f9f9f9; padding:30px 25px;'>" +
                "<table style='width:100%; background-color:#ffffff; border-collapse:collapse;'>" +
                "<!-- Title -->" +
                "<tr><td style='padding:18px 25px; border-bottom:1px solid #eeeeee;'>" +
                "<table style='width:100%;'><tr>" +
                "<td style='width:4px; background-color:#d44a8a;'></td>" +
                "<td style='padding-left:15px;'><p style='margin:0; font-size:15px; font-weight:bold; color:#333;'>" +
                escapeHtml(title) + "</p></td></tr></table></td></tr>" +

                "<!-- BODY -->" +
                "<tr><td style='padding:25px;'>" + bodyContent + "</td></tr>" +

                "<!-- Footer -->" +
                "<tr><td style='padding:0 25px 25px 25px;'>" +
                "<table style='width:100%; background-color:#f9f9f9;'>" +
                "<tr><td style='padding:12px 15px;'>" +
                "<p style='font-size:10px; color:#888;'>This is a system-generated email. Please do not reply directly to this message.</p>" +
                "</td></tr></table></td></tr>" +
                "</table></td></tr>" +

                "<!-- Bottom Strip -->" +
                "<tr><td>" +
                "<table style='width:100%;'>" +
                "<tr>" +
                "<td style='background-color:#00d4ff;height:2px;width:16%;'></td>" +
                "<td style='background-color:#29c4f2;height:2px;width:17%;'></td>" +
                "<td style='background-color:#6a9fd6;height:2px;width:17%;'></td>" +
                "<td style='background-color:#a872b8;height:2px;width:17%;'></td>" +
                "<td style='background-color:#d44a8a;height:2px;width:17%;'></td>" +
                "<td style='background-color:#e91e63;height:2px;width:16%;'></td>" +
                "</tr></table></td></tr>" +
                "</table>";
    }

    // Helper to escape HTML special characters (basic)
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}