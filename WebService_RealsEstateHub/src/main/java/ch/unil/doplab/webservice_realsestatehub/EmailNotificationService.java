package ch.unil.doplab.webservice_realsestatehub;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

// Utilisation de IA pour faire cette API d'emailing externe

public class EmailNotificationService {
    
    // Brevo (Sendinblue) API Configuration
    private static final String BREVO_API_KEY = "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";
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String SENDER_EMAIL = "nikhilesh.1305acharya@gmail.com";
    private static final String SENDER_NAME = "RealEstateHub";
    private static final boolean USE_REAL_API = true; // Set to true to send real emails
    
    /**
     * Sends an email notification when an offer status changes
     * Uses external Brevo API to send real emails to the buyer only
     */
    public static boolean sendOfferStatusNotification(
            String offerId,
            String propertyId,
            String oldStatus,
            String newStatus,
            String buyerEmail) {
        
        if (USE_REAL_API) {
            return sendViaBrevoAPI(offerId, propertyId, oldStatus, newStatus, buyerEmail);
        } else {
            // Fallback to mock/log
            System.out.println("=== EMAIL NOTIFICATION (Mock Mode) ===");
            System.out.println("Offer ID: " + offerId);
            System.out.println("Property ID: " + propertyId);
            System.out.println("Status Change: " + oldStatus + " → " + newStatus);
            System.out.println("Buyer Email: " + buyerEmail);
            System.out.println("======================================");
            return true;
        }
    }
    
    /**
     * Sends email via Brevo (Sendinblue) API to buyer only
     */
    private static boolean sendViaBrevoAPI(
            String offerId,
            String propertyId,
            String oldStatus,
            String newStatus,
            String buyerEmail) {
        
        try {
            URL url = new URL(BREVO_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("api-key", BREVO_API_KEY);
            conn.setRequestProperty("content-type", "application/json");
            conn.setDoOutput(true);
            
            // Build email message
            String emailBody = buildEmailMessage(offerId, propertyId, oldStatus, newStatus);
            
            // Create JSON payload for Brevo API - send only to buyer
            String jsonPayload = String.format(
                "{" +
                "  \"sender\": {\"name\": \"%s\", \"email\": \"%s\"}," +
                "  \"to\": [" +
                "    {\"email\": \"%s\", \"name\": \"Buyer\"}" +
                "  ]," +
                "  \"subject\": \"Offer Status Update - %s\"," +
                "  \"htmlContent\": \"%s\"" +
                "}",
                SENDER_NAME,
                SENDER_EMAIL,
                buyerEmail,
                offerId,
                emailBody.replace("\n", "<br>").replace("\"", "\\\"")
            );
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // Check response
            int responseCode = conn.getResponseCode();
            if (responseCode == 201 || responseCode == 200) {
                System.out.println("✅ Email sent successfully via Brevo API!");
                System.out.println("   Recipient: " + buyerEmail);
                return true;
            } else {
                System.err.println("❌ Failed to send email. Response code: " + responseCode);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error sending email via Brevo API: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Builds the email message content
     */
    private static String buildEmailMessage(String offerId, String propertyId, String oldStatus, String newStatus) {
        StringBuilder message = new StringBuilder();
        message.append("<h2>Offer Status Update</h2>");
        message.append("<p><strong>Offer ID:</strong> ").append(offerId).append("</p>");
        message.append("<p><strong>Property ID:</strong> ").append(propertyId).append("</p>");
        message.append("<p><strong>Previous Status:</strong> ").append(oldStatus).append("</p>");
        message.append("<p><strong>New Status:</strong> <span style='color: ");
        
        // Color code based on status
        switch (newStatus) {
            case "ACCEPTED":
                message.append("green'>✓ ").append(newStatus);
                break;
            case "REJECTED":
                message.append("red'>✗ ").append(newStatus);
                break;
            case "WITHDRAWN":
                message.append("orange'>⚠ ").append(newStatus);
                break;
            default:
                message.append("blue'>• ").append(newStatus);
        }
        message.append("</span></p>");
        
        message.append("<hr>");
        message.append("<p><em>This is an automated notification from RealEstateHub</em></p>");
        
        return message.toString();
    }
}
