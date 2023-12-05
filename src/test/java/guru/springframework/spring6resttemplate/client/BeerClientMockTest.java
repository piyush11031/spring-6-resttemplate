package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6resttemplate.config.RestTemplateBuilderConfig;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;

@RestClientTest
@Import(RestTemplateBuilderConfig.class) //We want our RestTemplateBuilder properly configured
class BeerClientMockTest {

    static final String URL = "http://localhost:8080";

    @Autowired
    ObjectMapper objectMapper; //We get ObjectMapper because need to setup mock to return back a JSON payload

    @Autowired
    RestTemplateBuilder restTemplateBuilderConfigured; //We get a configured RestTemplateBuilder

    //Main entry point for client-side REST testing. Used for tests that involve direct or indirect use of the RestTemplate.
    // Provides a way to set up expected requests that will be performed through the RestTemplate as well as mock responses
    // to send back thus removing the need for an actual server.
    MockRestServiceServer server;

    BeerClient beerClient;

    //MockServerRestTemplateCustomizer can be applied to a RestTemplateBuilder instances to add MockRestServiceServer support.
    @Mock
    RestTemplateBuilder mockRestTemplateBuilder = new RestTemplateBuilder(new MockServerRestTemplateCustomizer());

    BeerDTO beerDTO;
    String dtoJson;
    @BeforeEach
    void setUp() throws JsonProcessingException {
        RestTemplate restTemplate = restTemplateBuilderConfigured.build(); //Creates an instance of RestTemplate from our default builder
        server = MockRestServiceServer.bindTo(restTemplate).build(); //Binds the restTemplate to our mock server
        when(mockRestTemplateBuilder.build()).thenReturn(restTemplate); //mocking behaviour of .build() method
        beerClient = new BeerClientImpl(mockRestTemplateBuilder); //inject the mocked builder to BeerClientImpl
        beerDTO = getBeerDto();
        dtoJson = objectMapper.writeValueAsString(beerDTO);

    }
    //Setting up methods to get test data
    BeerDTO getBeerDto(){
        return BeerDTO.builder()
                .id(UUID.randomUUID())
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("123245")
                .build();
    }

    BeerDTOPageImpl getPage(){
        return new BeerDTOPageImpl(Arrays.asList(getBeerDto()), 1, 25, 1);
    }

    //Using Intellij refactor feature to Extract the mocked GET operation
    private void mockGetOperation() {
        server.expect(method(HttpMethod.GET))
                .andExpect(requestToUriTemplate(URL +
                        BeerClientImpl.GET_BEER_BY_ID_PATH, beerDTO.getId()))
                .andRespond(withSuccess(dtoJson, MediaType.APPLICATION_JSON));
    }

    @Test
    void testListBeers() throws JsonProcessingException {

        //We need to create a JSON payload that our mock server will be returning
        String payload = objectMapper.writeValueAsString(getPage());

        //Setting up the mock server to respond back with the "payload" when it receives an HTTP GET method with the specified URL
        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        Page<BeerDTO> dtos = beerClient.listBeers();
        assertThat(dtos.getContent().size()).isGreaterThan(0);

    }

    @Test
    void testListBeersByName() throws JsonProcessingException {

        String response = objectMapper.writeValueAsString(getPage());

        //Builds a URI with specified query parameter
        URI uri = UriComponentsBuilder.fromHttpUrl(URL + BeerClientImpl.GET_BEER_PATH)
                .queryParam("beerName", "ALE")
                .build().toUri();

        server.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andExpect(queryParam("beerName", "ALE"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        Page<BeerDTO> responsePage = beerClient.
                listBeers("ALE", null, null, null, null);

        assertThat(responsePage.getContent().size()).isEqualTo(1);
    }

    @Test
    void testGetBeerById(){
        
        //requestToUriTemplate: we can pass the id value to our URI path.
        mockGetOperation();

        BeerDTO responseDto = beerClient.getBeerById(beerDTO.getId());
        assertThat(responseDto.getId()).isEqualTo(beerDTO.getId());
    }

    @Test
    void testCreateBeer(){

        //We use UriComponentsBuilder to bind the id to the path
        //We create a URI for passing to the location header of POST request
        URI uri = UriComponentsBuilder.fromPath(BeerClientImpl.GET_BEER_BY_ID_PATH).build(beerDTO.getId());

        //Handles the POST method
        server.expect(method(HttpMethod.POST))
                .andExpect(requestTo(URL + BeerClientImpl.GET_BEER_PATH))
                                .andRespond(withAccepted().location(uri)); //setting location

        //Handles the GET method
        mockGetOperation();

        BeerDTO newDto = beerClient.createBeer(beerDTO);
        assertThat(newDto.getId()).isEqualTo(beerDTO.getId());
    }


    @Test
    void testUpdateBeer() {

        server.expect(method(HttpMethod.PUT))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, beerDTO.getId()))
                .andRespond(withNoContent());

        //We Extract method for get operation
        mockGetOperation();

         BeerDTO responseDto = beerClient.updateBeer(beerDTO);
         assertThat(responseDto.getId()).isEqualTo(beerDTO.getId());
    }

    //Testing for Id not found
    @Test
    void testDeleteNotFound() {
        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, beerDTO.getId()))
                .andRespond(withResourceNotFound()); //Respond with resource not found

        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.deleteBeer(beerDTO.getId());
        });

        server.verify();
    }

    @Test
    void testDeleteBeer() {

        server.expect(method(HttpMethod.DELETE))
                .andExpect(requestToUriTemplate(URL + BeerClientImpl.GET_BEER_BY_ID_PATH, beerDTO.getId()))
                .andRespond(withNoContent());

        beerClient.deleteBeer(beerDTO.getId());

        //Verifies that Delete operation on the server occured.
        server.verify();
    }

}