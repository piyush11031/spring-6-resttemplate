package guru.springframework.spring6resttemplate.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import static java.util.Objects.isNull;

@Component
public class OAuthClientInterceptor implements ClientHttpRequestInterceptor {
    private final OAuth2AuthorizedClientManager manager; //Previously implemented
    private final Authentication principal;
    private final ClientRegistration clientRegistration;

    public OAuthClientInterceptor(OAuth2AuthorizedClientManager manager,
                                  ClientRegistrationRepository clientRegistrationRepository) {
        this.manager = manager;
        this.principal = createPrincipal();
        this.clientRegistration = clientRegistrationRepository.findByRegistrationId("springauth"); //Reg. ID From app.properties
    }

    //Standard interceptor which intercepts a request and work with the ClientManager
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        //Represents a request which contains info about client we want to authorize
        OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistration.getRegistrationId()) //configered from application.prop
                .principal(createPrincipal()) //It Represents an Authenticated client
                .build();

        //OAuth2AuthorizedClient(ClientRegistration clientRegistration, String principalName, OAuth2AccessToken accessToken)
        // method Attempts to authorize or re-authorize the client identified by the provided clientRegistrationId.
        //Returns: the OAuth2AuthorizedClient or null if authorization is not supported for the specified client
        OAuth2AuthorizedClient client = manager.authorize(oAuth2AuthorizeRequest);

        //if client isn't authorized
        if (isNull(client)) {
            throw new IllegalStateException("Missing credentials");
        }

        //The manager will provide client with token
        //We bind the token to the header
        request.getHeaders().add(HttpHeaders.AUTHORIZATION,
                "Bearer " + client.getAccessToken().getTokenValue());

        //Return the execution of the request
        return execution.execute(request, body);
    }

    //Authentication is spring security component, contains information about the authenticated principal.
    // The principal is the entity (user, system, or application) that has been authenticated.
    // the client ID is used as the name of the principal.
    private Authentication createPrincipal() {
        return new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.emptySet();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return this;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
            }

            @Override
            public String getName() {
                return clientRegistration.getClientId();
            }
        };
    }
}
