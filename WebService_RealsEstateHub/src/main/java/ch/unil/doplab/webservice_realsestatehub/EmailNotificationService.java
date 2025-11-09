package ch.unil.doplab.webservice_realsestatehub;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EmailNotificationService {

    // Brevo (Sendinblue) API - FREE 300 emails/day
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    
    // Brevo API Key - Get it from: https://app.brevo.com/settings/keys/api
    private static final String BREVO_API_KEY = "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";
    
    // Set to true to use real API, false to simulate
    private static final boolean USE_REAL_API = true;
    
    /**
     * Send email notification when offer status changes
     */
    public static boolean sendOfferStatusNotification(String offerId, String propertyId, 
                                                       String oldStatus, String newStatus,
                                                       String buyerEmail, String sellerEmail) {
        try {
            // Build email content
            String subject = "Offer Status Update - Real Estate Hub";
            String message = buildEmailMessage(offerId, propertyId, oldStatus, newStatus);
            
            // Log to console
            System.out.println("=== EMAIL NOTIFICATION ===");
            System.out.println("To: " + buyerEmail + ", " + sellerEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Message: " + message);
            System.out.println("External API: " + (USE_REAL_API ? "Brevo (REAL)" : "Brevo (simulated)"));
            System.out.println("========================");
            
            // Send via real API or simulate
            boolean success;
            if (USE_REAL_API && !BREVO_API_KEY.equals("YOUR_BREVO_API_KEY_HERE")) {
                success = sendViaBrevoAPI(buyerEmail, subject, message);
                if (success && !buyerEmail.equals(sellerEmail)) {
                    sendViaBrevoAPI(sellerEmail, subject, message);
                }
            } else {
                success = simulateEmailAPICall(buyerEmail, sellerEmail, subject, message);
            }
            
            return success;
            
        } catch (Exception e) {
            System.err.println("Error sending email notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Build email message content
     */
    private static String buildEmailMessage(String offerId, String propertyId, 
                                            String oldStatus, String newStatus) {
        StringBuilder message = new StringBuilder();
        message.append("Dear User,\n\n");
        message.append("The status of your offer has been updated.\n\n");
        message.append("Details:\n");
        message.append("- Offer ID: ").append(offerId).append("\n");
        message.append("- Property ID: ").append(propertyId).append("\n");
        message.append("- Previous Status: ").append(oldStatus).append("\n");
        message.append("- New Status: ").append(newStatus).append("\n\n");
        
        if ("ACCEPTED".equals(newStatus)) {
            message.append("Congratulations! Your offer has been accepted.\n");
        } else if ("REJECTED".equals(newStatus)) {
            message.append("Unfortunately, your offer has been rejected.\n");
        } else if ("PENDING".equals(newStatus)) {
            message.append("Your offer is currently under review.\n");
        }
        
        message.append("\nBest regards,\n");
        message.append("Real Estate Hub Team");
        
        return message.toString();
    }
    
    /**
     * Simulate email API call (for demo purposes)
     * In production, this would make an actual HTTP request to EmailJS or SendGrid
     */
    private static boolean simulateEmailAPICall(String toEmail1, String toEmail2, 
                                                String subject, String message) {
        try {
            // Simulate API response time
            Thread.sleep(100);
            
            // In real implementation, you would do:
            // 1. Create JSON payload with email details
            // 2. Send POST request to EmailJS/SendGrid API
            // 3. Parse response and return success/failure
            
            // For now, we'll just simulate success
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Send email via Brevo API (REAL email sending)
     */
    private static boolean sendViaBrevoAPI(String toEmail, String subject, String message) {
        HttpURLConnection connection = null;
        java.io.BufferedReader reader = null;
        
        try {
            java.net.URI uri = new java.net.URI(BREVO_API_URL);
            java.net.URL url = uri.toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("api-key", BREVO_API_KEY);
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            // Escape message for JSON
            String escapedMessage = message.replace("\n", "\\n").replace("\"", "\\\"");
            
            // Build JSON payload for Brevo
            String jsonPayload = String.format(
                "{\"sender\":{\"name\":\"Nikhilesh Acharya\",\"email\":\"nikhilesh.1305acharya@gmail.com\"}," +
                "\"to\":[{\"email\":\"%s\"}]," +
                "\"subject\":\"%s\"," +
                "\"textContent\":\"%s\"}",
                toEmail, subject, escapedMessage
            );
            
            System.out.println("Sending to Brevo API...");
            
            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            System.out.println("Brevo API Response Code: " + responseCode);
            
            // Read response
            if (responseCode >= 200 && responseCode < 300) {
                reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                System.out.println("Brevo API Response: " + response.toString());
                System.out.println("✅ REAL EMAIL SENT to " + toEmail);
                return true;
            } else {
                reader = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                System.err.println("Brevo API Error: " + errorResponse.toString());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error calling Brevo API: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
