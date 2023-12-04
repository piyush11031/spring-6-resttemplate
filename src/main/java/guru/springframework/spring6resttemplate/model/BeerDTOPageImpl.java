package guru.springframework.spring6resttemplate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created by jt, Spring Framework Guru.
 */
//Our implementation will return a "pagable" property in response which we don't need ,
// We can tell Jackson to ignore that property using this annotation
@JsonIgnoreProperties(ignoreUnknown = true, value = "pageable")
public class BeerDTOPageImpl<BeerDTO> extends PageImpl<guru.springframework.spring6resttemplate.model.BeerDTO> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)//Tells jackson to use this constructor to binding different properties to it.
    public BeerDTOPageImpl(@JsonProperty("content") List<guru.springframework.spring6resttemplate.model.BeerDTO> content, //Binds Beer data in "content" to List<BeerDTO>
                           @JsonProperty("number") int page, //Binds page number to page
                           @JsonProperty("size") int size, //page size
                           @JsonProperty("totalElements") long total) {
        super(content, PageRequest.of(page, size), total); //Creates instance of PageImpl
    }
}
