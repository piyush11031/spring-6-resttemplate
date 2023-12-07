package guru.springframework.spring6resttemplate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.client.RestTemplateBuilderConfigurer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration //Annotate our config file with Configuration annotation
public class RestTemplateBuilderConfig {

    //Externalizing the property, so we can configure it at runtime
    @Value("${rest.template.rootUrl}")
    String rootUrl;

    //We'll be returning back a bean of RestTemplateBuilder which is configured with spring boot defaults.
    //But because spring boot provides us with lot of defaults it gets a bit complext to configure it
    @Bean
    RestTemplateBuilder restTemplateBuilder(RestTemplateBuilderConfigurer configurer){

//    // The "configurer" takes the new instance of RestTemplateBuilder and configures it with spring boot defaults
//        RestTemplateBuilder builder = configurer.configure(new RestTemplateBuilder());
//
//        //To setup the default base path for API calls, we use "DefaultUriBuilderFactory"
//        DefaultUriBuilderFactory uriBuilderFactory = new
//                                                    DefaultUriBuilderFactory(rootUrl);
//
//        RestTemplateBuilder builderWithAuth = builder.basicAuthentication(USERNAME, PASSWORD); //Create a builder with Auth

//
//        //Utilizing Builder pattern to setup HTTP Basic Auth and Setting root url
        return configurer.configure(new RestTemplateBuilder())
                .uriTemplateHandler(new DefaultUriBuilderFactory(rootUrl));
    }
}
