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

    //We externalize the property, so we can configure it at runtime
    //We add in a new property for root URL, then we're using spring expression language to get this property
    //This property will be loaded in from application.properties
    //Now, if we have a different URL to go through we can configure a different spring profile
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
//        //return a configured RestTemplateBuilder configured with base URI for API calls
//        return builder.uriTemplateHandler(uriBuilderFactory);

        //utilizing builder pattern
        return configurer.configure(new RestTemplateBuilder())
                .uriTemplateHandler(new DefaultUriBuilderFactory(rootUrl));
    }
}
