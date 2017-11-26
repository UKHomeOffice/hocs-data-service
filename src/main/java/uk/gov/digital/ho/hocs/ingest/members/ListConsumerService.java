package uk.gov.digital.ho.hocs.ingest.members;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.exception.IngestException;

import java.util.Arrays;

@Service
@Slf4j
public class ListConsumerService {

    private final RestTemplate restTemplate;

    private final ListConsumerConfigurator configuration;

    @Autowired
    public ListConsumerService(RestTemplate restTemplate,
                               ListConsumerConfigurator configuration) {
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public EuropeMembers createFromEuropeanParliamentAPI() throws IngestException {
        ResponseEntity<EuropeMembers> response = getListFromApi(configuration.API_EUROPEAN_PARLIAMENT, MediaType.APPLICATION_XML, EuropeMembers.class);
        EuropeMembers members;
        if (response != null) {
            members = response.getBody();
        }
        else {
            throw new IngestException("Europe Members Not Found");
        }
        return members;
    }

    public IrishMembers createFromIrishParliamentAPI() throws IngestException {
        ResponseEntity<IrishMembers> response = getListFromApi(configuration.API_NORTHERN_IRISH_ASSEMBLY, MediaType.APPLICATION_XML, IrishMembers.class);
        IrishMembers members;
        if (response != null) {
            members = response.getBody();
        }
        else {
            throw new IngestException("Irish Members Not Found");
        }
        return members;
    }

    public ScottishMembers createFromScottishParliamentAPI() throws IngestException {
        ResponseEntity<ScottishMember[]> response = getListFromApi(configuration.API_SCOTTISH_PARLIAMENT, MediaType.APPLICATION_JSON, ScottishMember[].class);
        ScottishMembers members;
        if (response != null) {
            members = new ScottishMembers(Arrays.asList(response.getBody()));
        }
        else {
            throw new IngestException("Scottish Members Not Found");
        }
        return members;
    }

    public Members createCommonsFromUKParliamentAPI() throws IngestException {
        return createFromUKParliamentAPI(configuration.LIST_COMMONS);
    }

    public Members createLordsFromUKParliamentAPI() throws IngestException {
        return createFromUKParliamentAPI(configuration.LIST_LORDS);
    }

    private Members createFromUKParliamentAPI(final String house) throws IngestException {
        ResponseEntity<Members> response = getListFromApi(getFormattedUkEndpoint(house), MediaType.APPLICATION_XML, Members.class);
        Members members;
        if (response != null) {
            members = response.getBody();
        }
        else {
            throw new IngestException("UK Members Not Found");
        }
        return members;
    }

    private <T> ResponseEntity<T> getListFromApi(String apiEndpoint, MediaType mediaType, Class<T> returnClass) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(mediaType));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        return restTemplate.exchange(apiEndpoint, HttpMethod.GET, entity, returnClass);

    }

    private String getFormattedUkEndpoint(final String house) {
        return String.format(configuration.API_UK_PARLIAMENT, house);
    }

}
