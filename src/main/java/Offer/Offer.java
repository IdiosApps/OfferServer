package Offer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import javax.persistence.*;

@Entity
@EqualsAndHashCode
public class Offer {
    private @EqualsAndHashCode.Exclude @Id @GeneratedValue Long id;
    @Getter @Setter private String description;
    @Getter @Setter private String currency; // could be e.g. enum
    @Getter @Setter private Integer price; // can't create Offer in OfferController if using "int" type
    private Boolean isExpired = false;

    // h2 database doesn't like storing a Jota DateTime, so tried a Date - but had issues deserializing
    @Getter private String expiryDateTime; // e.g. 2020-09-13T18:52:24.714+01:00
    @Getter private Integer expiryTimeInSeconds;

    // Empty constructor for setter-injection
    private Offer() {}

    // Useful constructor in tests (in actual usage, setter-injection is used)
    public Offer(String description, String currency, Integer price, Integer expirationTimeSeconds) {
        this.description = description;
        this.currency = currency;
        this.price = price;
        this.isExpired = false;
        this.expiryTimeInSeconds = expirationTimeSeconds;

        DateTime now = DateTime.now();
        DateTime expirationDateTime = now.plusSeconds(expirationTimeSeconds);
        this.expiryDateTime = expirationDateTime.toString();
    }

    // When a new Offer is submitted, store both the expiration time and the expected expiration datetime
    public void setExpiryTimeInSeconds(Integer expirationTimeSeconds) {
        this.expiryTimeInSeconds = expirationTimeSeconds;

        DateTime now = DateTime.now();
        DateTime expirationDateTime = now.plusSeconds(expirationTimeSeconds);
        this.expiryDateTime = expirationDateTime.toString();
    }

    public void cancelOffer() {
        this.isExpired = true;
    }

    public boolean isExpired() {
        if (isExpired) // has been cancelled
            return true;

        Seconds timeToExpiry = Seconds.secondsBetween(DateTime.now(), new DateTime(expiryDateTime));
        return timeToExpiry.isLessThan(Seconds.ZERO);

        // Ideally the above result would be sent back to the database, to prevent unncessary calculations
    }
}
