package ch.unil.doplab.webservice_realsestatehub;

import ch.unil.doplab.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

/**
 * ApplicationState - Centralized state management bean
 * Replaces static HashMaps with injectable singleton
 * Following Jakarta EE best practices
 */
@ApplicationScoped
public class ApplicationState {

    private final Map<UUID, Property> properties = new HashMap<>();
    private final Map<UUID, Offer> offers = new HashMap<>();
    private final Map<UUID, Buyer> buyers = new HashMap<>();
    private final Map<UUID, Seller> sellers = new HashMap<>();

    /**
     * Initialize with demo data
     */
    @PostConstruct
    public void init() {
        // Pre-load demo buyers
        Buyer alice = new Buyer("Alice", "Martin", "alice@demo.com", "alice", "pass123", 350000);
        Buyer jonathan = new Buyer("Jonathan", "Grossrieder", "jonathan.grossrieder@unil.ch", "Jon", "pass456", 550000);
        buyers.put(alice.getUserID(), alice);
        buyers.put(jonathan.getUserID(), jonathan);

        // Pre-load demo seller
        Seller demoSeller = new Seller("Demo", "Seller", "seller@demo.com", "seller", "pass789");
        sellers.put(demoSeller.getUserID(), demoSeller);

        System.out.println("ApplicationState initialized with " + buyers.size() + " buyers and " + sellers.size() + " sellers");
    }

    // Properties
    public Map<UUID, Property> getProperties() {
        return properties;
    }

    public Property getPropertyById(UUID id) {
        return properties.get(id);
    }

    // Offers
    public Map<UUID, Offer> getOffers() {
        return offers;
    }

    public Offer getOfferById(UUID id) {
        return offers.get(id);
    }

    // Buyers
    public Map<UUID, Buyer> getBuyers() {
        return buyers;
    }

    public Buyer getBuyerById(UUID id) {
        return buyers.get(id);
    }

    // Sellers
    public Map<UUID, Seller> getSellers() {
        return sellers;
    }

    public Seller getSellerById(UUID id) {
        return sellers.get(id);
    }
}
