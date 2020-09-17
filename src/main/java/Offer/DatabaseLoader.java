package Offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseLoader {

    private final OfferRepository repository;

    @Autowired
    public DatabaseLoader(OfferRepository repository) {
        this.repository = repository;
    }
}
