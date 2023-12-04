package guru.springframework.spring6resttemplate.client;

import guru.springframework.spring6resttemplate.model.BeerDTO;
import guru.springframework.spring6resttemplate.model.BeerStyle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.core.Is.is;

@SpringBootTest
class BeerClientImplTest {

    @Autowired
    BeerClientImpl beerClient;

    @Test
    void listBeersNoBeerName() {

        beerClient.listBeers(null, null, false, null, null);
    }

    @Test
    void listBeersByName() {

        beerClient.listBeers("ALE", null, true, 0, 300);
    }

    @Test
    void listBeersByStyle() {

        beerClient.listBeers(null, BeerStyle.IPA, true, 0, 2000);
    }

    @Test
    void getBeerById() {

        //Fetch a BeerDTO for testing
        Page<BeerDTO> page = beerClient.listBeers();
        BeerDTO dto = page.getContent().get(0);

        BeerDTO byId = beerClient.getBeerById(dto.getId());
        assertNotNull(byId);
    }

    @Test
    void testCreateBeer() {

        BeerDTO newDto = BeerDTO.builder()
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("12345")
                .build();

        //createBeer method returs a Populated BeerDTO object
        BeerDTO savedDto = beerClient.createBeer(newDto);
        assertNotNull(savedDto);
        assertThat(savedDto.getId()).isNotNull(); //Id property gets populated
    }

    @Test
    void testUpdateBeer() {

        BeerDTO newDto = BeerDTO.builder()
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("12345")
                .build();

        //Create a new BeerDTO and retrieve the saved object
        BeerDTO beerDTO = beerClient.createBeer(newDto);

        //Update the name field
        final String newName = "Mango Bobs 3";
        beerDTO.setBeerName(newName);

        //Perform PUT operation and Retrieve the updated BeerDTO object
        BeerDTO updatedBeer = beerClient.updateBeer(beerDTO);

        assertEquals(newName, updatedBeer.getBeerName());
    }

    @Test
    void testDeleteBeer() {

        BeerDTO newDto = BeerDTO.builder()
                .price(new BigDecimal("10.99"))
                .beerName("Mango Bobs")
                .beerStyle(BeerStyle.IPA)
                .quantityOnHand(500)
                .upc("12345")
                .build();

        //Create a new BeerDTO and retrieve the saved object
        BeerDTO beerDTO = beerClient.createBeer(newDto);

        beerClient.deleteBeer(beerDTO.getId());

        //After deletion, if we try to reterive our beer object we get 404 Not found.
        // The way rest client works is it throws an exception if we get 400 status back.
        assertThrows(HttpClientErrorException.class, () -> {
            beerClient.getBeerById(beerDTO.getId());
        });
    }
}



















