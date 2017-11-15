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

    private final DataListRepository listRepository;

    private final RestTemplate restTemplate;

    private final ListConsumerConfigurator configuration;

    @Autowired
    public ListConsumerService(DataListRepository listRepository,
                               RestTemplate restTemplate,
                               ListConsumerConfigurator configuration) {
        this.listRepository = listRepository;
        this.restTemplate = restTemplate;
        this.configuration = configuration;
    }

    public void refreshListsFromAPI() {

        removeExistingLists();

        createFromUKParliamentAPI(configuration.HOUSE_COMMONS);
        createFromUKParliamentAPI(configuration.HOUSE_LORDS);
        createFromScottishParliamentAPI();
        createFromIrishParliamentAPI();
        createFromEuropeanParliamentAPI();

    }

    @Transactional
    void removeExistingLists() {

        List<String> listsToRemove = new ArrayList<>();
        listsToRemove.add(configuration.LIST_LORDS);
        listsToRemove.add(configuration.LIST_COMMONS);
        listsToRemove.add(configuration.LIST_SCOTTISH_PARLIAMENT);
        listsToRemove.add(configuration.LIST_EUROPEAN_PARLIAMENT);
        listsToRemove.add(configuration.LIST_NORTHERN_IRISH_ASSEMBLY);
        listsToRemove.add(configuration.LIST_WELSH_ASSEMBLY);

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
    void createFromEuropeanParliamentAPI() {

        ResponseEntity<EuropeMembers> response = getListFromApi(configuration.API_EUROPEAN_PARLIAMENT, MediaType.APPLICATION_XML, EuropeMembers.class);

        if (response != null) {

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
                        properties.add(new DataListEntityProperty(configuration.PROP_HOUSE, configuration.HOUSE_EUROPEAN_PARLIAMENT));
                        listEntity.setProperties(properties);
                        return listEntity;

                    })
                    .collect(Collectors.toSet());

            DataList dataList = new DataList(configuration.LIST_EUROPEAN_PARLIAMENT, dataListEntities);

            listRepository.save(dataList);

        }
    }

    @Transactional
    void createFromIrishParliamentAPI() {

        ResponseEntity<IrishMembers> response = getListFromApi(configuration.API_NORTHERN_IRISH_ASSEMBLY, MediaType.APPLICATION_XML, IrishMembers.class);

        if (response != null) {

            List<IrishMember> members = response.getBody().getMembers();

            Set<DataListEntity> dataListEntities = members
                    .stream()
                    .map(m -> createListEntity(m.getName(), configuration.HOUSE_NORTHERN_IRISH_ASSEMBLY))
                    .collect(Collectors.toSet());

            DataList dataList = new DataList(configuration.LIST_NORTHERN_IRISH_ASSEMBLY, dataListEntities);

            listRepository.save(dataList);

        }
    }

    @Transactional
    void createFromScottishParliamentAPI() {

        ResponseEntity<ScottishMember[]> response = getListFromApi(configuration.API_SCOTTISH_PARLIAMENT, MediaType.APPLICATION_JSON, ScottishMember[].class);

        if (response != null) {

            List<ScottishMember> members = Arrays.asList(response.getBody());

            Set<DataListEntity> dataListEntities = members
                    .stream()
                    .map(m -> createListEntity(m.getName(), configuration.HOUSE_SCOTTISH_PARLIAMENT))
                    .collect(Collectors.toSet());

            DataList dataList = new DataList(configuration.LIST_SCOTTISH_PARLIAMENT, dataListEntities);

            listRepository.save(dataList);

        }
    }

    @Transactional
    void createFromUKParliamentAPI(final String HOUSE) {

        ResponseEntity<Members> response = getListFromApi(getFormattedUkEndpoint(HOUSE), MediaType.APPLICATION_XML, Members.class);

        if (response != null) {

            List<Member> members = response.getBody().getMembers();

            Set<DataListEntity> dataListEntities = members
                    .stream()
                    .map(m -> {
                        DataListEntity listEntity = new DataListEntity(m.getDisplayName(), m.getListName());
                        Set<DataListEntityProperty> properties = new HashSet<>();
                        properties.add(new DataListEntityProperty(configuration.PROP_HOUSE, m.getHouse()));
                        listEntity.setProperties(properties);
                        return listEntity;
                    })
                    .collect(Collectors.toSet());

            DataList dataList = new DataList(HOUSE + "_list", dataListEntities);

            listRepository.save(dataList);

        }
    }

    private <T> DataListEntity createListEntity(String name, final String HOUSE) {

        String[] names = name.split(",");
        String reference = String.join(" ", names).trim();
        Collections.reverse(Arrays.asList(names));
        String displayName = String.join(" ", names).trim();

        DataListEntity listEntity = new DataListEntity(displayName, reference);
        Set<DataListEntityProperty> properties = new HashSet<>();
        properties.add(new DataListEntityProperty(configuration.PROP_HOUSE, HOUSE));
        listEntity.setProperties(properties);
        return listEntity;

    }

    private <T> ResponseEntity<T> getListFromApi(String apiEndpoint, MediaType mediaType, Class<T> returnClass) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(mediaType));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        return restTemplate.exchange(apiEndpoint, HttpMethod.GET, entity, returnClass);

    }

    private String getFormattedUkEndpoint(final String HOUSE) {
        return String.format(configuration.API_UK_PARLIAMENT, HOUSE);
    }

}
