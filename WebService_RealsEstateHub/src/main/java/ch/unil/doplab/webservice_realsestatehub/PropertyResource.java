package ch.unil.doplab.webservice_realsestatehub;

import ch.unil.doplab.Property;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;

@Path("/properties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PropertyResource {

    @Inject
    private ApplicationState state;

    /**
     * Create a new property
     * POST /api/properties
     */
    @POST
    public Response createProperty(PropertyDTO dto) {
        try {
            Property property = new Property(
                    dto.getTitle(),
                    dto.getOwnerId(),
                    dto.getDescription(),
                    dto.getLocation(),
                    dto.getPrice(),
                    dto.getSize(),
                    Property.PropertyType.valueOf(dto.getType())
            );
            
            // Set status if provided
            if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
                property.setStatus(Property.PropertyStatus.valueOf(dto.getStatus()));
            }
            
            // Set features if provided
            if (dto.getFeatures() != null && !dto.getFeatures().isEmpty()) {
                dto.getFeatures().forEach(property::addFeature);
            }
            
            state.getProperties().put(property.getPropertyId(), property);
            
            return Response.status(Response.Status.CREATED)
                    .entity(property)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid property data: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all properties
     * GET /api/properties
     */
    @GET
    public Response getAllProperties() {
        return Response.ok(new ArrayList<>(state.getProperties().values())).build();
    }

    /**
     * Get property by ID
     * GET /api/properties/{id}
     */
    @GET
    @Path("/{id}")
    public Response getPropertyById(@PathParam("id") String id) {
        try {
            UUID propertyId = UUID.fromString(id);
            Property property = state.getPropertyById(propertyId);
            
            if (property == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Property not found"))
                        .build();
            }
            
            return Response.ok(property).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid property ID"))
                    .build();
        }
    }

    /**
     * Update property
     * PUT /api/properties/{id}
     */
    @PUT
    @Path("/{id}")
    public Response updateProperty(@PathParam("id") String id, PropertyDTO dto) {
        try {
            UUID propertyId = UUID.fromString(id);
            Property property = state.getPropertyById(propertyId);
            
            if (property == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Property not found"))
                        .build();
            }
            
            // Update property fields
            property.updatePropertyDetails(
                    dto.getTitle(),
                    dto.getDescription(),
                    dto.getLocation(),
                    dto.getPrice(),
                    dto.getSize(),
                    dto.getType() != null ? Property.PropertyType.valueOf(dto.getType()) : null
            );
            
            return Response.ok(property).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid data: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * Delete property
     * DELETE /api/properties/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteProperty(@PathParam("id") String id) {
        try {
            UUID propertyId = UUID.fromString(id);
            Property removed = state.getProperties().remove(propertyId);
            
            if (removed == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Property not found"))
                        .build();
            }
            
            return Response.ok()
                    .entity(new SuccessResponse("Property deleted successfully"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Invalid property ID"))
                    .build();
        }
    }

    /**
     * Search properties by location
     * GET /api/properties/search?location=Zurich
     */
    @GET
    @Path("/search")
    public Response searchProperties(@QueryParam("location") String location) {
        List<Property> results = state.getProperties().values().stream()
                .filter(p -> location == null || 
                        (p.getLocation() != null && p.getLocation().equalsIgnoreCase(location)))
                .toList();
        
        return Response.ok(results).build();
    }

    // DTO for creating/updating properties
    public static class PropertyDTO {
        private String title;
        private UUID ownerId;
        private String description;
        private String location;
        private double price;
        private double size;
        private String type; // APARTMENT, HOUSE, etc.
        private String status; // FOR_SALE, PENDING, SOLD, OFF_MARKET
        private Map<String, Object> features; // bedrooms, bathrooms, etc.

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public UUID getOwnerId() { return ownerId; }
        public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public double getSize() { return size; }
        public void setSize(double size) { this.size = size; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Map<String, Object> getFeatures() { return features; }
        public void setFeatures(Map<String, Object> features) { this.features = features; }
    }

    // Helper classes for responses
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
