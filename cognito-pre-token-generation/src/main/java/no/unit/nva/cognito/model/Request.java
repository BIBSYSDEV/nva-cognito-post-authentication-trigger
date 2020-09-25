package no.unit.nva.cognito.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {

    @JsonProperty("userAttributes")
    private UserAttributes userAttributes;
    @JsonProperty("groupConfiguration")
    private GroupConfiguration groupConfiguration;

    public Request() {
    }

    public UserAttributes getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(UserAttributes userAttributes) {
        this.userAttributes = userAttributes;
    }
}
