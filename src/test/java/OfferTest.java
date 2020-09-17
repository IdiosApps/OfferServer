import Offer.Offer;
import Offer.OfferRepository;
import Offer.OfferServerApplication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// Please run the tests individually -
// unfortunately I wasn't able to find the time to figure out why they don't like to run together
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OfferServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OfferTest {

    @LocalServerPort
    private int port;

    @Autowired
    final TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private OfferRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll(); // ensure we have a clean slate for each test
    }

    private Offer getOfferFromHttpResonse() throws JsonProcessingException, JSONException {
        HttpEntity<String> entity = new HttpEntity<>(null, new HttpHeaders());

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("http://localhost:" + port + "api/offers/1");

        JSONObject actualJson = new JSONObject(this.restTemplate.exchange(builder.build().toUri(),
                HttpMethod.GET,
                entity,
                String.class)
                .getBody());
        actualJson.remove("_links");
        actualJson.remove("expired"); // expired state is calculated on the fly, so don't need to initialise Offer with it

        return new ObjectMapper().readValue(actualJson.toString(), Offer.class);
    }

    private void putOfferWithHttpRequest(String request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("http://localhost:" + port + "api/offers/new");

        restTemplate.exchange(builder.build().toUri(),
                HttpMethod.PUT,
                new HttpEntity<>(request, httpHeaders),
                String.class);
    }

    private void cancelOfferWithHttpRequest() {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("http://localhost:" + port + "api/offers/1/cancel");

        restTemplate.exchange(builder.build().toUri(),
                HttpMethod.PUT,
                new HttpEntity<>(null, new HttpHeaders()),
                String.class);
    }

    @Test
    public void httpPutOfferTest() {
        String offerJson = "{\"description\" : \"Sweets\"," +
                "\"currency\" : \"GBP\"," +
                "\"price\" : 4," +
                "\"expiryTimeInSeconds\" : 60}";

        putOfferWithHttpRequest(offerJson);

        Offer storedOffer = repository.findById(1L)
                .orElseThrow(NoSuchElementException::new);

        assertEquals("Sweets", storedOffer.getDescription());
        assertEquals("GBP", storedOffer.getCurrency());
        assertEquals(4, storedOffer.getPrice());
        assertEquals(60, storedOffer.getExpiryTimeInSeconds());
    }

    @Test
    public void httpGetOfferTest() throws JSONException, JsonProcessingException {
        Offer expectedOffer = new Offer("The answer to the ultimate question of life, the universe, and everything",
                "Euro",
                42,
                60); // long expiry, shouldn't expire (or be cancelled)

        repository.save(expectedOffer);

        Offer actualOffer = getOfferFromHttpResonse();

        assertEquals(expectedOffer, actualOffer);
    }

    @Test
    public void offerTimeoutTest() throws InterruptedException {
        Offer offer = new Offer("Luxury IDE", "USD", 120, 1);

        repository.save(offer);

        TimeUnit.SECONDS.sleep(3); // sleep for 2 seconds longer than our timeout

        Offer actualOffer = repository.findById(1L)
                .orElseThrow(NoSuchElementException::new);

        assertTrue(actualOffer.isExpired());
    }

    @Test
    public void httpCancelOfferTest() {
        Offer initialOffer = new Offer("2080ti", "GBP", 400, 604800);

        repository.save(initialOffer);

        cancelOfferWithHttpRequest();

        Offer storedOffer = repository.findById(1L)
                .orElseThrow(NoSuchElementException::new);

        assertTrue(storedOffer.isExpired());
    }

    @AfterAll
    void tearDown() {
        repository.deleteAll();
    }
}
