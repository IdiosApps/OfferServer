package Offer;

import org.assertj.core.util.Lists;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value="/api")
public class OfferController {

    private final OfferRepository repository;

    OfferController(OfferRepository repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/offers/new", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void newOffer(@RequestBody Offer newOffer) {
        // don't check for duplicate offers in database, as could feasibly have same description, price etc.

        repository.save(newOffer);
    }

    @GetMapping("/offers/")
    List<Offer> getAllOffers() {
        return Lists.newArrayList(repository.findAll());
    }

    @PutMapping("/offers/{id}/cancel")
    void cancelOffer(@PathVariable Long id) {
        Optional<Offer> offerOptional = repository.findById(id);

        if (offerOptional.isEmpty())
            return;

        Offer offer = offerOptional.get();
        offer.cancelOffer();
        repository.save(offer);
    }
}
