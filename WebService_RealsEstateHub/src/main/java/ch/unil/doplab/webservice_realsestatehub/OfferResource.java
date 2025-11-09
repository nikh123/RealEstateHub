package ch.unil.doplab.webservice_realsestatehub;

import ch.unil.doplab.Offer;
import ch.unil.doplab.Buyer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

@Path("/offers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OfferResource {

    // In-memory storage
    private static final Map<UUID, Offer> offers = new HashMap<>();

    /**
     * Create a new offer
     * POST /api/offers
     */
    @POST
    public Response createOffer(OfferDTO dto) {
        try {
            if (dto.getAmount() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Amount must be positive"))
                        .build();
            }
            
            Offer offer = new Offer(
                    dto.getPropertyId(),
                    dto.getBuyerId(),
                    dto.getAmount()
            );
            
            offers.put(offer.getOfferId(), offer);
            
            return Response.status(Response.Status.CREATED)
                    .entity(offer)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid offer data: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all offers
     * GET /api/offers
     */
    @GET
    public Response getAllOffers() {
        return Response.ok(new ArrayList<>(offers.values())).build();
    }

    /**
     * Get offer by ID
     * GET /api/offers/{id}
     */
    @GET
    @Path("/{id}")
    public Response getOfferById(@PathParam("id") String id) {
        try {
            UUID offerId = UUID.fromString(id);
            Offer offer = offers.get(offerId);
            
            if (offer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Offer not found"))
                        .build();
            }
            
            return Response.ok(offer).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid offer ID"))
                    .build();
        }
    }

    /**
     * Update offer status (accept/reject)
     * PUT /api/offers/{id}/status
     */
    @PUT
    @Path("/{id}/status")
    public Response updateOfferStatus(@PathParam("id") String id, StatusDTO statusDto) {
        try {
            UUID offerId = UUID.fromString(id);
            Offer offer = offers.get(offerId);
            
            if (offer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Offer not found"))
                        .build();
            }
            
            // Store old status for email notification
            Offer.Status oldStatus = offer.getStatus();
            Offer.Status newStatus = Offer.Status.valueOf(statusDto.getStatus());
            offer.setStatus(newStatus);
            
            // Get buyer's email from BuyerResource
            String buyerEmail = "nikhilesh.acharya@unil.ch"; // Default fallback
            try {
                Buyer buyer = BuyerResource.getBuyerById(offer.getBuyerId());
                if (buyer != null && buyer.getEmail() != null) {
                    buyerEmail = buyer.getEmail();
                }
            } catch (Exception e) {
                System.out.println("Could not fetch buyer email, using default");
            }
            
            boolean emailSent = EmailNotificationService.sendOfferStatusNotification(
                offerId.toString(),
                offer.getPropertyId().toString(),
                oldStatus.toString(),
                newStatus.toString(),
                buyerEmail
            );
            
            // Add email status to response
            Map<String, Object> response = new HashMap<>();
            response.put("offer", offer);
            response.put("emailNotificationSent", emailSent);
            response.put("message", emailSent ? 
                "Offer status updated and email notifications sent via external API" : 
                "Offer status updated but email notification failed");
            
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid status. Use: PENDING, ACCEPTED, REJECTED, WITHDRAWN"))
                    .build();
        }
    }

    /**
     * Delete/cancel offer
     * DELETE /api/offers/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteOffer(@PathParam("id") String id) {
        try {
            UUID offerId = UUID.fromString(id);
            Offer removed = offers.remove(offerId);
            
            if (removed == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Offer not found"))
                        .build();
            }
            
            return Response.ok()
                    .entity(new SuccessResponse("Offer deleted successfully"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid offer ID"))
                    .build();
        }
    }

    /**
     * Get offers by property
     * GET /api/offers/property/{propertyId}
     */
    @GET
    @Path("/property/{propertyId}")
    public Response getOffersByProperty(@PathParam("propertyId") String propertyId) {
        try {
            UUID propId = UUID.fromString(propertyId);
            List<Offer> propertyOffers = offers.values().stream()
                    .filter(o -> o.getPropertyId().equals(propId))
                    .toList();
            
            return Response.ok(propertyOffers).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid property ID"))
                    .build();
        }
    }

    // DTOs
    public static class OfferDTO {
        private UUID propertyId;
        private UUID buyerId;
        private double amount;

        public UUID getPropertyId() { return propertyId; }
        public void setPropertyId(UUID propertyId) { this.propertyId = propertyId; }
        
        public UUID getBuyerId() { return buyerId; }
        public void setBuyerId(UUID buyerId) { this.buyerId = buyerId; }
        
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }

    public static class StatusDTO {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class ErrorResponse {
        private String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
    }

    public static class SuccessResponse {
        private String message;
        public SuccessResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
