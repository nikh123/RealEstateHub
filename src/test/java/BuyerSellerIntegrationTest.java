import ch.unil.doplab.Buyer;
import ch.unil.doplab.Seller;
import ch.unil.doplab.Property;
import ch.unil.doplab.Offer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete real estate transaction flow.
 * Tests interaction between Buyer, Seller, Property, and Offer.
 */
class BuyerSellerIntegrationTest {

    private Buyer buyer;
    private Seller seller;
    private Property property;

    @BeforeEach
    void setUp() {
        // Create a buyer with budget
        buyer = new Buyer("John", "Doe", "john@buyer.com", "johndoe", "pass123", 600000);

        // Create a seller
        seller = new Seller("Jane", "Smith", "jane@seller.com", "janesmith", "pass456");

        // Create a property owned by seller
        property = new Property(
                "Modern Apartment",
                seller.getUserID(),
                "Beautiful 2BR apartment with lake view",
                "Zurich",
                500000,
                75.5,
                Property.PropertyType.APARTMENT
        );
        property.addFeature("bedrooms", 2);
        property.addFeature("bathrooms", 1);
    }

    @Test
    void testCompleteTransactionFlow_Success() {
        // 1. Seller publishes property
        seller.publishProperty(property);

        // Verify property is published
        assertEquals(1, seller.getOwnedProperties().size());
        assertEquals(Property.PropertyStatus.FOR_SALE, property.getStatus());
        assertTrue(seller.getOwnedProperties().contains(property));

        // 2. Buyer places offer
        Offer offer = buyer.placeOffer(property, 480000);

        // Verify offer created correctly
        assertNotNull(offer);
        assertEquals(property.getPropertyId(), offer.getPropertyId());
        assertEquals(buyer.getUserID(), offer.getBuyerId());
        assertEquals(480000, offer.getAmount());
        assertEquals(Offer.Status.PENDING, offer.getStatus());

        // 3. Seller receives and accepts offer
        seller.respondToOffer(offer, true);

        // Verify offer accepted
        assertEquals(Offer.Status.ACCEPTED, offer.getStatus());
        assertEquals(1, seller.getReceivedOffers().size());
        assertTrue(seller.getReceivedOffers().contains(offer));
    }

    @Test
    void testCompleteTransactionFlow_Rejection() {
        // Seller publishes property
        seller.publishProperty(property);

        // Buyer places offer (low-ball)
        Offer offer = buyer.placeOffer(property, 400000);

        // Seller rejects offer
        seller.respondToOffer(offer, false);

        // Verify offer rejected
        assertEquals(Offer.Status.REJECTED, offer.getStatus());
        assertTrue(seller.getReceivedOffers().contains(offer));
        
        // Property still for sale
        assertEquals(Property.PropertyStatus.FOR_SALE, property.getStatus());
    }

    @Test
    void testMultipleOffersOnSameProperty() {
        seller.publishProperty(property);

        // Multiple buyers make offers
        Buyer buyer2 = new Buyer("Bob", "Jones", "bob@buyer.com", "bobjones", "pass789", 550000);
        
        Offer offer1 = buyer.placeOffer(property, 480000);
        Offer offer2 = buyer2.placeOffer(property, 490000);

        // Seller receives both offers
        seller.respondToOffer(offer1, false); // Reject first
        seller.respondToOffer(offer2, true);  // Accept second

        // Verify both offers tracked
        assertEquals(2, seller.getReceivedOffers().size());
        assertEquals(Offer.Status.REJECTED, offer1.getStatus());
        assertEquals(Offer.Status.ACCEPTED, offer2.getStatus());
    }

    @Test
    void testBuyerCannotPlaceOfferOnNullProperty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> buyer.placeOffer(null, 500000));
        assertTrue(ex.getMessage().contains("Property is required"));
    }

    @Test
    void testBuyerCannotPlaceOfferWithZeroAmount() {
        seller.publishProperty(property);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> buyer.placeOffer(property, 0));
        assertTrue(ex.getMessage().contains("Amount must be positive"));
    }

    @Test
    void testBuyerCannotPlaceOfferWithNegativeAmount() {
        seller.publishProperty(property);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> buyer.placeOffer(property, -100000));
        assertTrue(ex.getMessage().contains("Amount must be positive"));
    }

    @Test
    void testSellerCannotPublishNullProperty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> seller.publishProperty(null));
        assertTrue(ex.getMessage().contains("property is required"));
    }

    @Test
    void testSellerCannotRespondToNullOffer() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> seller.respondToOffer(null, true));
        assertTrue(ex.getMessage().contains("offer is required"));
    }

    @Test
    void testSellerPublishesMultipleProperties() {
        Property property2 = new Property(
                "Villa",
                seller.getUserID(),
                "Luxury villa",
                "Geneva",
                1200000,
                200,
                Property.PropertyType.VILLA
        );

        seller.publishProperty(property);
        seller.publishProperty(property2);

        assertEquals(2, seller.getOwnedProperties().size());
        assertTrue(seller.getOwnedProperties().contains(property));
        assertTrue(seller.getOwnedProperties().contains(property2));
    }

    @Test
    void testBuyerBudgetTracking() {
        assertEquals(600000, buyer.getBudget());
        
        buyer.setBudget(700000);
        assertEquals(700000, buyer.getBudget());
    }

    @Test
    void testPropertyTypesOfInterest() {
        assertTrue(buyer.getPropertyTypesOfInterest().isEmpty());
        
        buyer.addPropertyTypeOfInterest("APARTMENT");
        buyer.addPropertyTypeOfInterest("HOUSE");
        
        assertEquals(2, buyer.getPropertyTypesOfInterest().size());
    }

    @Test
    void testSellerAndBuyerRoles() {
        assertEquals("Seller", seller.getRole());
        assertEquals("Buyer", buyer.getRole());
    }

    @Test
    void testOfferReferencesCorrectEntities() {
        seller.publishProperty(property);
        Offer offer = buyer.placeOffer(property, 500000);

        // Verify all IDs match
        assertEquals(property.getPropertyId(), offer.getPropertyId());
        assertEquals(buyer.getUserID(), offer.getBuyerId());
        assertEquals(seller.getUserID(), property.getOwnerId());
    }
}
