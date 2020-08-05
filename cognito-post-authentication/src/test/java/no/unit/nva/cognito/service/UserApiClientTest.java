package no.unit.nva.cognito.service;

import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Optional;
import no.unit.nva.cognito.model.Role;
import no.unit.nva.cognito.model.User;
import nva.commons.utils.Environment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class UserApiClientTest {

    public static final String HTTP = "http";
    public static final String EXAMPLE_ORG = "example.org";
    public static final String GARBAGE_JSON = "{{}";
    public static final String USERNAME = "username";
    public static final String INSTITUTION_ID = "institution.id";
    public static final String CREATOR = "Creator";

    private ObjectMapper objectMapper;
    private UserApiClient userApiClient;
    private HttpClient httpClient;
    private HttpResponse httpResponse;

    /**
     * Set up test environment.
     */
    @BeforeEach
    public void init() {
        objectMapper = new ObjectMapper();
        Environment environment = mock(Environment.class);
        when(environment.readEnv(UserApiClient.USER_API_SCHEME)).thenReturn(HTTP);
        when(environment.readEnv(UserApiClient.USER_API_HOST)).thenReturn(EXAMPLE_ORG);
        httpClient = mock(HttpClient.class);
        httpResponse = mock(HttpResponse.class);

        userApiClient = new UserApiClient(httpClient, new ObjectMapper(), environment);
    }

    @Test
    public void getUserReturnsUserOnValidUsername() throws Exception {
        when(httpResponse.body()).thenReturn(getValidJsonUser());
        when(httpResponse.statusCode()).thenReturn(SC_OK);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);

        Optional<User> user = userApiClient.getUser(USERNAME);

        Assertions.assertTrue(user.isPresent());
    }

    @Test
    public void getUserReturnsEmptyOptionalOnInvalidJsonResponse() throws IOException, InterruptedException {
        when(httpResponse.body()).thenReturn(GARBAGE_JSON);
        when(httpResponse.statusCode()).thenReturn(SC_OK);
        when(httpClient.send(any(), any())).thenReturn(httpResponse);

        Optional<User> user = userApiClient.getUser(USERNAME);

        assertTrue(user.isEmpty());
    }

    @Test
    public void getUserReturnsEmptyOptionalOnInvalidHttpResponse() throws IOException, InterruptedException {
        when(httpClient.send(any(), any())).thenThrow(IOException.class);

        Optional<User> user = userApiClient.getUser(USERNAME);

        assertTrue(user.isEmpty());
    }

    public String getValidJsonUser() throws JsonProcessingException {
        return objectMapper.writeValueAsString(createUser());
    }

    private User createUser() {
        return new User(
            USERNAME,
            INSTITUTION_ID, Collections.singletonList(new Role(CREATOR))
        );
    }
}
