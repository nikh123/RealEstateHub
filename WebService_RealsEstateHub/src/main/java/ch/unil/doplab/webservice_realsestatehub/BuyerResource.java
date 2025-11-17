package ch.unil.doplab.webservice_realsestatehub;

import ch.unil.doplab.Buyer;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

@Path("/buyers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BuyerResource {

    @Inject
    private ApplicationState state;

    /**
     * Créer un nouvel acheteur
     * POST /api/buyers
     */
    @POST
    public Response createBuyer(BuyerDTO dto) {
        try {
            // Validation simple
            if (dto.getBudget() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Budget must be positive"))
                        .build();
            }
            if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Email is required"))
                        .build();
            }

            Buyer buyer = new Buyer(
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.getEmail(),
                    dto.getUsername(),
                    dto.getPassword(),
                    dto.getBudget()
            );

            state.getBuyers().put(buyer.getUserID(), buyer);

            return Response.status(Response.Status.CREATED)
                    .entity(buyer)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid buyer data: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Obtenir tous les acheteurs
     * GET /api/buyers
     */
    @GET
    public Response getAllBuyers() {
        return Response.ok(new ArrayList<>(state.getBuyers().values())).build();
    }

    /**
     * Obtenir un acheteur par son ID
     * GET /api/buyers/{id}
     */
    @GET
    @Path("/{id}")
    public Response getBuyerById(@PathParam("id") String id) {
        try {
            UUID buyerId = UUID.fromString(id);
            Buyer buyer = state.getBuyers().get(buyerId);

            if (buyer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Buyer not found"))
                        .build();
            }

            return Response.ok(buyer).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid buyer ID"))
                    .build();
        }
    }

    /**
     * Mettre à jour le budget d'un acheteur (similaire à /status pour Offer)
     * PUT /api/buyers/{id}/budget
     */
    @PUT
    @Path("/{id}/budget")
    public Response updateBuyerBudget(@PathParam("id") String id, BudgetDTO dto) {
        try {
            UUID buyerId = UUID.fromString(id);
            Buyer buyer = state.getBuyers().get(buyerId);

            if (buyer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Buyer not found"))
                        .build();
            }

            if (dto.getBudget() <= 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Budget must be positive"))
                        .build();
            }

            buyer.setBudget(dto.getBudget());

            return Response.ok(buyer).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid buyer ID"))
                    .build();
        }
    }

    /**
     * Supprimer un acheteur
     * DELETE /api/buyers/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteBuyer(@PathParam("id") String id) {
        try {
            UUID buyerId = UUID.fromString(id);
            Buyer removed = state.getBuyers().remove(buyerId);

            if (removed == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Buyer not found"))
                        .build();
            }

            return Response.ok()
                    .entity(new SuccessResponse("Buyer deleted successfully"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid buyer ID"))
                    .build();
        }
    }

    // --- DTOs et Classes de Réponse ---

    /**
     * DTO pour la création d'un Buyer
     */
    public static class BuyerDTO {
        private String firstName;
        private String lastName;
        private String email;
        private String username;
        private String password;
        private double budget;

        // Getters et Setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public double getBudget() { return budget; }
        public void setBudget(double budget) { this.budget = budget; }
    }

    /**
     * DTO pour mettre à jour le budget (similaire au StatusDTO)
     */
    public static class BudgetDTO {
        private double budget;

        public double getBudget() { return budget; }
        public void setBudget(double budget) { this.budget = budget; }
    }


    // (Vous pouvez réutiliser les mêmes classes ErrorResponse et SuccessResponse)
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