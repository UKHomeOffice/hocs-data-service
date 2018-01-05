package uk.gov.digital.ho.hocs.ingest.members;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.exception.IngestException;
import uk.gov.digital.ho.hocs.model.House;
import uk.gov.digital.ho.hocs.model.Member;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ListConsumerService {

    final String HOUSE_LORDS = "lords";
    final String HOUSE_COMMONS = "commons";
    final String HOUSE_SCOTTISH_PARLIAMENT = "scottish_parliament";
    final String HOUSE_NORTHERN_IRISH_ASSEMBLY = "northern_irish_assembly";
    final String HOUSE_EUROPEAN_PARLIAMENT = "european_parliament";
    final String HOUSE_WELSH_ASSEMBLY = "welsh_assembly";

    final String API_UK_PARLIAMENT;
    final String API_SCOTTISH_PARLIAMENT;
    final String API_NORTHERN_IRISH_ASSEMBLY;
    final String API_EUROPEAN_PARLIAMENT;
    final String API_WELSH_ASSEMBLY;

    @Autowired
    public ListConsumerService(@Value("${api.uk.parliament}") String apiUkParliament,
                               @Value("${api.scottish.parliament}") String apiScottishParliament,
                               @Value("${api.ni.assembly}") String apiNorthernIrishAssembly,
                               @Value("${api.european.parliament}") String apiEuropeanParliament,
                               @Value("${api.welsh.assembly}") String apiWelshAssembly) {
        this.API_UK_PARLIAMENT = apiUkParliament;
        this.API_SCOTTISH_PARLIAMENT = apiScottishParliament;
        this.API_NORTHERN_IRISH_ASSEMBLY = apiNorthernIrishAssembly;
        this.API_EUROPEAN_PARLIAMENT = apiEuropeanParliament;
        this.API_WELSH_ASSEMBLY = apiWelshAssembly;
    }

    public House createFromEuropeanParliamentAPI() throws IngestException {
        EuropeMembers europeMembers = getMembersFromAPI(API_EUROPEAN_PARLIAMENT, MediaType.APPLICATION_XML, EuropeMembers.class);
        Set<Member> members = europeMembers.getMembers().stream().map(m -> new Member(m.getName())).collect(Collectors.toSet());
        return new House(HOUSE_EUROPEAN_PARLIAMENT, members);
    }

    public House createFromIrishAssemblyAPI() throws IngestException {
        IrishMembers irishMembers = getMembersFromAPI(API_NORTHERN_IRISH_ASSEMBLY, MediaType.APPLICATION_XML, IrishMembers.class);
        Set<Member> members = irishMembers.getMembers().stream().map(m -> new Member(m.getName())).collect(Collectors.toSet());
        return new House(HOUSE_NORTHERN_IRISH_ASSEMBLY, members);
    }

    public House createFromScottishParliamentAPI() throws IngestException {
       ScottishMember[] scottishMembers = getMembersFromAPI(API_SCOTTISH_PARLIAMENT, MediaType.APPLICATION_JSON, ScottishMember[].class);
        Set<Member> members = Arrays.stream(scottishMembers).map(m -> new Member(m.getName())).collect(Collectors.toSet());
        return new House(HOUSE_SCOTTISH_PARLIAMENT, members);
    }

    public House createCommonsFromUKParliamentAPI() throws IngestException {
        UKMembers ukUKMembers = getMembersFromAPI(getFormattedUkEndpoint(HOUSE_COMMONS), MediaType.APPLICATION_XML, UKMembers.class);
        Set<Member> members = ukUKMembers.getMembers().stream().map(m -> new Member(m.getName())).collect(Collectors.toSet());
        return new House(HOUSE_COMMONS, members);
    }

    public House createLordsFromUKParliamentAPI() throws IngestException {
        UKMembers ukUKMembers = getMembersFromAPI(getFormattedUkEndpoint(HOUSE_LORDS), MediaType.APPLICATION_XML, UKMembers.class);
        Set<Member> members = ukUKMembers.getMembers().stream().map(m -> new Member(m.getName())).collect(Collectors.toSet());
        return new House(HOUSE_LORDS, members);
    }

    public House createFromWelshAssemblyAPI() throws IngestException {
        WelshWards welshWards = getMembersFromAPI(API_WELSH_ASSEMBLY, MediaType.APPLICATION_XML, WelshWards.class);
        Set<WelshMembers> welshMembers = welshWards.getWards().stream().map(w -> w.getMembers()).collect(Collectors.toSet());
        Set<WelshMember> welhMemberSet = welshMembers.stream().map(m -> m.getMembers()).flatMap(l -> l.stream()).collect(Collectors.toSet());
        Set<Member> members = welhMemberSet.stream().map(m -> new Member(m.getName())).collect(Collectors.toSet());
        return new House(HOUSE_LORDS, members);
    }

    private <T> T getMembersFromAPI(String apiEndpoint, MediaType mediaType, Class<T> returnClass) throws IngestException {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(mediaType));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<T> response = new RestTemplate().exchange(apiEndpoint, HttpMethod.GET, entity, returnClass);

        if (response == null || response.getStatusCodeValue() != 200) {
            throw new IngestException("UKMembers Not Found at " + apiEndpoint);
        }
        return response.getBody();
    }

    private String getFormattedUkEndpoint(final String house) {
        return String.format(API_UK_PARLIAMENT, house);
    }

}
