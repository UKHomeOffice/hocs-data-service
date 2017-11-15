package uk.gov.digital.ho.hocs.api_lists;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateProvider {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
