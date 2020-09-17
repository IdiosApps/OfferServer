<H1>Introduction</H1>

This project is a `Spring` web server application that communicates over `HTTP`, handles data with a `H2` database over `JPA` in a `CrudRepository`, and uses `JUnit` for automated unit and integration testing.

The key data object for this project is an `Offer`, which stores an item's:

- description
- currency
- price
- expiration status
- expiration date
- expiration timeout (in seconds)

Offers can be cancelled before they time out.

The project uses `Gradle` for dependency management. IntelliJ and CLI should automatically grab Gradle 6.6.1 when using the Gradle wrapper.

I used `Lombok` to reduce boilerplate and keep the Offer class short. Annotation processing must be enabled if using IntelliJ, and for Offer methods to be recognised the Lombok plugin will need to be installed too.

In run config for OfferServerApplication, `Use classpath of module` should be set to `com.offer-server.main`

I used JDK 14.0.2 for this application.

_____    
<H1>API usage</H1>

For running HTTP requests, I used `Postman`. The example HTTP requests below add an Offer to the database, query all offers, cancel the added offer, and views the added offer:

<H3>Add an offer </H3>

 - HTTP `PUT`
 - to `localhost:8080/api/offers/new`
 - with body `{"description" : "Sweets","currency" : "GBP","price" : "2","expiryTimeInSeconds" : 60}` as raw JSON
 - with header `Content-Type:application/json`

<H3>See all offers</H3>

- HTTP `GET`
- to `localhost:8080/api/offers`

<H3>Cancel a specific offer</H3>

- HTTP `PUT`
- to `localhost:8080/api/offers/1/cancel`

<H3>See a specific offer</H3>

- HTTP `GET`
- to `localhost:8080/api/offers/1`

After these requests, the following JSON would be returned (`expiryDateTime` depending on the Offer creation time of course):

```
{
    "description": "Sweets",
    "currency": "GBP",
    "price": 4,
    "expiryDateTime": "2020-09-16T23:41:35.915+01:00",
    "expiryTimeInSeconds": 600,
    "expired": true,
}
```

Note that the `expired` state would also be `true` if this entity were to be queried after the expiryDateTime. Here it was cancelled manually, before the planned expiration date.

____
<H1>Automated testing</H1>

There are four automated tests (they run individually, but unfortunately I wasn't able to find the time to figure out why they don't like to run together):

- httpPutOfferTest - makes a PUT HTTP request to the /api/offers/new endpoint, and ensure the databases' values are correct for the entry
- httpGetOfferTest - makes a GET HTTP request to /api/offers/1, saves an Offer to the database, GETs it by HTTP request and ensure it's the same as our expected Offer
- httpCancelOfferTest - saves an Offer with a long expiry time to the database, makes an HTTP PUT request to cancel it, retrieves it, and ensures it has has expired
- OfferTimeoutTest - saves an Offer to the database with a short expiry time, gets the item after expiry time is up, and ensures it has expired

____
<H1>Assumptions</H1>

- Prices can be stored as integers, as e.g. `£1.23` is equivalent to `123p`. BigDecimal would be an option if non-integer prices were desired.
- There can be multiple offers with the same fields - someone could submit two Offers at the same time, with the same data. As possible extension here would be having a `quantity` within every Offer
- The delay between an Offer request reaching the server and the expiration time being calculated is negligible compared to the delay between a client submitting an Offer and it reaching the server
- Expiration times only need to be precise to one second - though this could feasibly be (sub-) millisecond in some contexts
