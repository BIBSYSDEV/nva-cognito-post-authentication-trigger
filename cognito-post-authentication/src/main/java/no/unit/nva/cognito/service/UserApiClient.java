package no.unit.nva.cognito.service;

import static nva.commons.utils.attempt.Try.attempt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import no.unit.nva.cognito.model.User;
import nva.commons.utils.Environment;
import nva.commons.utils.JacocoGenerated;
import nva.commons.utils.attempt.ConsumerWithException;
import nva.commons.utils.attempt.Failure;
import nva.commons.utils.attempt.Try;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserApiClient implements UserApi {

    public static final String PATH = "/users/";
    public static final String USER_API_SCHEME = "USER_API_SCHEME";
    public static final String USER_API_HOST = "USER_API_HOST";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String userApiScheme;
    private final String userApiHost;

    private static final Logger logger = LoggerFactory.getLogger(UserApiClient.class);

    /**
     * Constructor for UserApiClient.
     *
     * @param httpClient   httpClient
     * @param objectMapper objectMapper
     * @param environment  environment
     */
    public UserApiClient(HttpClient httpClient, ObjectMapper objectMapper, Environment environment) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.userApiScheme = environment.readEnv(USER_API_SCHEME);
        this.userApiHost = environment.readEnv(USER_API_HOST);
    }

    @Override
    public Optional<User> getUser(String username) {
        logger.info("Requesting user information for username: " + username);
        return fetchUserInformation(username)
            .stream()
            .filter(responseIsSuccessful())
            .map(tryParsingCustomer())
            .findAny()
            .flatMap(this::getValueOrLogError);
    }

    @Override
    @JacocoGenerated
    public void createUser(User user) {
    }

    private Optional<HttpResponse<String>> fetchUserInformation(String orgNumber) {
        return Try.of(formUri(orgNumber))
            .map(URIBuilder::build)
            .map(this::buildHttpRequest)
            .map(this::sendHttpRequest)
            .toOptional(logResponseError());
    }

    private Function<HttpResponse<String>, Try<User>> tryParsingCustomer() {
        return attempt(this::parseUser);
    }

    private Predicate<HttpResponse<String>> responseIsSuccessful() {
        return resp -> resp.statusCode() == HttpStatus.SC_OK;
    }

    private Optional<User> getValueOrLogError(Try<User> valueTry) {
        return valueTry.toOptional(logErrorParsingUserInformation());
    }

    private ConsumerWithException<Failure<User>, RuntimeException> logErrorParsingUserInformation() {
        return failure -> logger.error("Error parsing user information");
    }

    private ConsumerWithException<Failure<HttpResponse<String>>, RuntimeException> logResponseError() {
        return failure -> logger.error("Error fetching user information");
    }

    private User parseUser(HttpResponse<String> response)
        throws JsonProcessingException {
        return objectMapper.readValue(response.body(), User.class);
    }

    private HttpResponse<String> sendHttpRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
        return httpClient.send(httpRequest, BodyHandlers.ofString());
    }

    private URIBuilder formUri(String username) {
        return new URIBuilder()
            .setScheme(userApiScheme)
            .setHost(userApiHost)
            .setPath(PATH + username);
    }

    private HttpRequest buildHttpRequest(URI uri) {
        return HttpRequest.newBuilder()
            .uri(uri)
            .GET()
            .build();
    }
}
