package ch.unil.doplab.webservice_realsestatehub;

import ch.unil.doplab.Property;
import ch.unil.doplab.Seller;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SellerResource - REST API for Seller CRUD operations
 * Third extra feature for grade improvement
 */
@Path("/sellers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SellerResource {

    @Inject
    private ApplicationState state;

    // ===== CREATE =====
    @POST
    public Response createSeller(SellerDTO sellerDTO) {
        if (sellerDTO.firstName == null || sellerDTO.lastName == null || sellerDTO.email == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("First name, last name, and email are required"))
                    .build();
        }

        Seller seller = new Seller(
                sellerDTO.firstName,
                sellerDTO.lastName,
                sellerDTO.email,
                sellerDTO.username,
                sellerDTO.password
        );

        state.getSellers().put(seller.getUserID(), seller);

        return Response.status(Response.Status.CREATED)
                .entity(seller)
                .build();
    }

    // ===== READ ALL =====
    @GET
    public Response getAllSellers() {
        List<Seller> sellerList = new ArrayList<>(state.getSellers().values());
        return Response.ok(sellerList).build();
    }

    // ===== READ ONE =====
    @GET
    @Path("/{id}")
    public Response getSellerById(@PathParam("id") String id) {
        try {
            UUID sellerId = UUID.fromString(id);
            Seller seller = state.getSellerById(sellerId);

            if (seller == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Seller not found"))
                        .build();
            }

            return Response.ok(seller).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid seller ID format"))
                    .build();
        }
    }

    // ===== UPDATE =====
    @PUT
    @Path("/{id}")
    public Response updateSeller(@PathParam("id") String id, SellerDTO sellerDTO) {
        try {
            UUID sellerId = UUID.fromString(id);
            Seller seller = state.getSellerById(sellerId);

            if (seller == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Seller not found"))
                        .build();
            }

            // Update fields
            if (sellerDTO.firstName != null) seller.setFirstName(sellerDTO.firstName);
            if (sellerDTO.lastName != null) seller.setLastName(sellerDTO.lastName);
            if (sellerDTO.email != null) seller.setEmail(sellerDTO.email);
            if (sellerDTO.username != null) seller.setUsername(sellerDTO.username);
            if (sellerDTO.password != null) seller.setPassword(sellerDTO.password);

            return Response.ok(seller).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid seller ID format"))
                    .build();
        }
    }

    // ===== DELETE =====
    @DELETE
    @Path("/{id}")
    public Response deleteSeller(@PathParam("id") String id) {
        try {
            UUID sellerId = UUID.fromString(id);
            Seller removed = state.getSellers().remove(sellerId);

            if (removed == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Seller not found"))
                        .build();
            }

            return Response.status(Response.Status.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid seller ID format"))
                    .build();
        }
    }

    // ===== GET SELLER'S PROPERTIES =====
    @GET
    @Path("/{id}/properties")
    public Response getSellerProperties(@PathParam("id") String id) {
        try {
            UUID sellerId = UUID.fromString(id);
            Seller seller = state.getSellerById(sellerId);

            if (seller == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Seller not found"))
                        .build();
            }

            List<Property> ownedProperties = state.getProperties().values().stream()
                    .filter(p -> p.getOwnerId() != null && p.getOwnerId().equals(sellerId))
                    .collect(Collectors.toList());

            return Response.ok(ownedProperties).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid seller ID format"))
                    .build();
        }
    }

    // ===== DTOs =====
    public static class SellerDTO {
        public String firstName;
        public String lastName;
        public String email;
        public String username;
        public String password;
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}
