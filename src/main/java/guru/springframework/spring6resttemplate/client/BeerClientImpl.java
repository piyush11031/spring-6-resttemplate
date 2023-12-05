package guru.springframework.spring6resttemplate.client;

import com.fasterxml.jackson.databind.JsonNode;
import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerDTOPageImpl;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BeerClientImpl implements BeerClient {

    //Spring Boot provides us with RestTemplateBuilder. It get pre-configured with sensible defaults.
    private final RestTemplateBuilder restTemplateBuilder;

    public static final String GET_BEER_PATH = "/api/v1/beer";
    public static final String GET_BEER_BY_ID_PATH = "/api/v1/beer/{beerId}";

    @Override
    public void deleteBeer(UUID id) {

        RestTemplate restTemplate = restTemplateBuilder.build();

        restTemplate.delete(GET_BEER_BY_ID_PATH, id);
    }

    @Override
    public BeerDTO updateBeer(BeerDTO beerDTO) {

        RestTemplate restTemplate = restTemplateBuilder.build();

        UUID beerId = beerDTO.getId();

        //Perform PUT operation to the Url path
        //The beerId variable gets bind to our path variable
        restTemplate.put(GET_BEER_BY_ID_PATH, beerDTO, beerId);

        //Returns back the populated object
        return getBeerById(beerId);
    }


    @Override
    public BeerDTO createBeer(BeerDTO newDto) {

        RestTemplate restTemplate = restTemplateBuilder.build();


        //Create a new resource by POSTing the given object to the path, and returns the value of the Location header.
        URI uri = restTemplate.postForLocation(GET_BEER_PATH, newDto);

        //We use the Location header from "response" to return back created BeerDTO object in the database
        return  restTemplate.getForObject(uri.getPath(), BeerDTO.class);

    }


    @Override
    public BeerDTO getBeerById(UUID beerId) {

        RestTemplate restTemplate = restTemplateBuilder.build();

        //.getForObject method Retrieve a BeerDTO.class by doing a GET on the id path
        //The beerId variable gets bind to our path variable
        return restTemplate.getForObject(GET_BEER_BY_ID_PATH, BeerDTO.class, beerId);
    }


    //Overloaded method to List Beers with no query parameters
    @Override
    public Page<BeerDTO> listBeers() {
        return this.listBeers(null, null, null, null, null);
    }

    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {

        //Get instance of RestTemplate using RestTemplateBuilder
        RestTemplate restTemplate = restTemplateBuilder.build();

        //UriComponentsBuilder allows us build the path to include the query parameters
        //.fromPath method utilizes the Base Path we set in our configuration
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath(GET_BEER_PATH);

        //It will be an optional parameter
        if(beerName != null) {
            uriComponentsBuilder.queryParam("beerName", beerName);
        }
        if(beerStyle != null) {
            uriComponentsBuilder.queryParam("beerStyle", beerStyle);
        }
        if(pageNumber != null) {
            uriComponentsBuilder.queryParam("pageNumber", pageNumber);
        }
        if(pageSize != null) {
            uriComponentsBuilder.queryParam("pageSize", pageSize);
        }
        if(showInventory != null) {
            uriComponentsBuilder.queryParam("showInventory", showInventory);
        }

        //Response entity gives us everything in the response
        //We use PageImpl instead of Page, bcz Page  is an interface not a hard implementation
        ResponseEntity<BeerDTOPageImpl> pageResponseEntity =
                restTemplate.getForEntity(uriComponentsBuilder.toUriString(), BeerDTOPageImpl.class);

        return pageResponseEntity.getBody();
    }


}
