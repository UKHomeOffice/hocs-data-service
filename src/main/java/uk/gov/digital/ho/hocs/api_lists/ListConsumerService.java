package uk.gov.digital.ho.hocs.api_lists;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.DataListRepository;
import uk.gov.digital.ho.hocs.model.DataList;
import uk.gov.digital.ho.hocs.model.DataListEntity;
import uk.gov.digital.ho.hocs.model.DataListEntityProperty;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ListConsumerService {

    private DataListRepository listRepository;

    private final static String PROP_HOUSE = "house";

    private final static String API_UK_PARLIAMENT = "http://data.parliament.uk/membersdataplatform/services/mnis/members/query/House=%s";
    private final static String API_SCOTTISH_PARLIAMENT = "https://data.parliament.scot/api/members";
    private final static String API_IRISH_PARLIAMENT = "http://data.niassembly.gov.uk/api/members/";
    private final static String API_EUROPEAN_PARLIAMENT = "http://www.europarl.europa.eu/meps/en/xml.html?query=full&filter=all";

    private final static String HOUSE_LORDS = "lords";
    private final static String HOUSE_COMMONS = "commons";
    private final static String HOUSE_SCOTTISH_PARLIAMENT = "scottish_parliament";
    private final static String HOUSE_IRISH_PARLIAMENT = "irish_parliament";
    private final static String HOUSE_EUROPEAN_PARLIAMENT = "european_parliament";
    private final static String HOUSE_WELSH_ASSEMBLY = "welsh_assembly";

    private final static String LIST_LORDS = "lords_list";
    private final static String LIST_COMMONS = "commons_list";
    private final static String LIST_SCOTTISH_PARLIAMENT = "scottish_parliament_list";
    private final static String LIST_IRISH_PARLIAMENT = "irish_parliament_list";
    private final static String LIST_EUROPEAN_PARLIAMENT = "european_parliament_list";
    private final static String LIST_WELSH_ASSEMBLY = "welsh_assembly_list";

    @Autowired
    public ListConsumerService(DataListRepository listRepository) {
        this.listRepository = listRepository;

        refreshListsFromAPI();
    }

    public void refreshListsFromAPI() {

        removeExisitingLists();

        createFromUKParliamentAPI(HOUSE_LORDS);
        createFromUKParliamentAPI(HOUSE_COMMONS);
        createFromScottishParliamentAPI();
        createFromIrishParliamentAPI();
        createFromEuropeanParliamentAPI();

    }

    @Transactional
    private void removeExisitingLists() {

        List<String> listsToRemove = new ArrayList<>();
        listsToRemove.add(LIST_LORDS);
        listsToRemove.add(LIST_COMMONS);
        listsToRemove.add(LIST_SCOTTISH_PARLIAMENT);
        listsToRemove.add(LIST_EUROPEAN_PARLIAMENT);
        listsToRemove.add(LIST_IRISH_PARLIAMENT);

        List<DataList> entitiesToRemove = new ArrayList<>();

        for (String list:
             listsToRemove) {
            DataList entity = listRepository.findDataListByName(list);
            if (entity != null)
                entitiesToRemove.add(entity);
        }

        if(!entitiesToRemove.isEmpty())
            listRepository.delete(entitiesToRemove);
    }

    @Transactional
    private void createFromEuropeanParliamentAPI() {

        ResponseEntity<EuropeMembers> response = getListFromApi(API_EUROPEAN_PARLIAMENT, MediaType.APPLICATION_XML, EuropeMembers.class);

        List<EuropeMember> members = response.getBody().getMembers();

        Set<DataListEntity> dataListEntities = members
                .stream()
                .filter(m -> m.getCountry().contains("United Kingdom") )
                .map(m -> {
                    String[] names = m.getName().split(" ");
                    String lastName = names[names.length - 1];
                    String[] otherNames = Arrays.copyOf(names, names.length - 1);

                    String reference = String.format("%s %s", lastName, String.join(" ", otherNames));
                    String displayName = String.format("%s %s", String.join(" ", otherNames), lastName);

                    displayName = WordUtils.capitalizeFully(displayName.toLowerCase(), new char[]{' ', '\'', '-', '('});

                    DataListEntity listEntity = new DataListEntity(displayName, reference);
                    Set<DataListEntityProperty> properties = new HashSet<>();
                    properties.add(new DataListEntityProperty(PROP_HOUSE, HOUSE_EUROPEAN_PARLIAMENT));
                    listEntity.setProperties(properties);
                    return listEntity;

                })
                .collect(Collectors.toSet());

        DataList dataList = new DataList(LIST_EUROPEAN_PARLIAMENT, dataListEntities);

        listRepository.save(dataList);

    }

    @Transactional
    private void createFromIrishParliamentAPI() {

        ResponseEntity<IrishMembers> response = getListFromApi(API_IRISH_PARLIAMENT, MediaType.APPLICATION_XML, IrishMembers.class);

        List<IrishMember> members = response.getBody().getMembers();

        Set<DataListEntity> dataListEntities = members
                .stream()
                .map(m -> ListConsumerService.createListEntity(m.getName(), HOUSE_IRISH_PARLIAMENT))
                .collect(Collectors.toSet());

        DataList dataList = new DataList(LIST_IRISH_PARLIAMENT, dataListEntities);

        listRepository.save(dataList);

    }

    @Transactional
    private void createFromScottishParliamentAPI() {

        ResponseEntity<ScottishMember[]> response = getListFromApi(API_SCOTTISH_PARLIAMENT, MediaType.APPLICATION_JSON, ScottishMember[].class);

        List<ScottishMember> members = Arrays.asList(response.getBody());

        Set<DataListEntity> dataListEntities = members
                .stream()
                .map(m -> ListConsumerService.createListEntity(m.getName(), HOUSE_SCOTTISH_PARLIAMENT))
                .collect(Collectors.toSet());

        DataList dataList = new DataList(LIST_SCOTTISH_PARLIAMENT, dataListEntities);

        listRepository.save(dataList);


    }

    @Transactional
    private void createFromUKParliamentAPI(final String HOUSE) {

        ResponseEntity<Members> response = getListFromApi(getFormattedUkEndpoint(HOUSE), MediaType.APPLICATION_XML, Members.class);

        List<Member> members = response.getBody().getMembers();

        Set<DataListEntity> dataListEntities = members
                .stream()
                .map(m -> {
                    DataListEntity listEntity = new DataListEntity(m.getDisplayName(), m.getListName());
                    Set<DataListEntityProperty> properties = new HashSet<>();
                    properties.add(new DataListEntityProperty(PROP_HOUSE, m.getHouse()));
                    listEntity.setProperties(properties);
                    return listEntity;
                })
                .collect(Collectors.toSet());

        DataList dataList = new DataList(HOUSE + "_list", dataListEntities);

        listRepository.save(dataList);

    }

    private static <T> DataListEntity createListEntity(String name, final String HOUSE) {
        String[] names = name.split(",");
        String reference = String.join(" ", names);
        Collections.reverse(Arrays.asList(names));
        String displayName = String.join(" ", names);

        DataListEntity listEntity = new DataListEntity(displayName, reference);
        Set<DataListEntityProperty> properties = new HashSet<>();
        properties.add(new DataListEntityProperty(PROP_HOUSE, HOUSE));
        listEntity.setProperties(properties);
        return listEntity;
    }

    private static <T> ResponseEntity<T> getListFromApi(String apiEndpoint, MediaType mediaType, Class<T> returnClass) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(mediaType));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        return restTemplate.exchange(apiEndpoint, HttpMethod.GET, entity, returnClass);

    }

    private static String getFormattedUkEndpoint(final String HOUSE) {
        return String.format(API_UK_PARLIAMENT, HOUSE);
    }

}
