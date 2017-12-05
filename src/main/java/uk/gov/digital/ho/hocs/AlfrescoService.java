package uk.gov.digital.ho.hocs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;

import java.util.Base64;
import java.util.List;

@Service
public class AlfrescoService {

    private final AlfrescoConfiguration configuration;
    private final RestTemplate restTemplate;

    @Autowired
    public AlfrescoService(RestTemplate restTemplate, AlfrescoConfiguration configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public <T> void postBatchedRecords(List<T> recordList) throws AlfrescoPostException{

        final String url = configuration.API_HOST + AlfrescoConfiguration.API_ENDPOINT_USERS;

        for (T records : recordList) {
             if ( postRequest(url, records).getStatusCode() != HttpStatus.OK)
                 throw new AlfrescoPostException("Failed to post request payload to Alfresco");
        }

    }

    private <T> ResponseEntity postRequest(String url, T payload) {
        HttpEntity<T> request = new HttpEntity<>(payload, getBasicAuthHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }

    private HttpHeaders getBasicAuthHeaders() {
        return new HttpHeaders() {{
            String auth = String.format("%s:%s", configuration.API_USERNAME, configuration.API_PASSWORD);
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = String.format("Basic %s", new String(encodedAuth));
            set("Authorization", authHeader);
            set("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        }};
    }
}
