package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.IngestException;
import uk.gov.digital.ho.hocs.ingest.members.*;
import uk.gov.digital.ho.hocs.model.DataList;
import uk.gov.digital.ho.hocs.model.DataListEntity;
import uk.gov.digital.ho.hocs.model.DataListEntityProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemberService {

    private final String PROP_HOUSE = "house";

    final String HOUSE_LORDS = "lords";
    final String HOUSE_COMMONS = "commons";
    final String HOUSE_SCOTTISH_PARLIAMENT = "scottish_parliament";
    final String HOUSE_NORTHERN_IRISH_ASSEMBLY = "northern_irish_assembly";
    final String HOUSE_EUROPEAN_PARLIAMENT = "european_parliament";


    private final DataListRepository repo;
    private final ListConsumerService listConsumerService;

    @Autowired
    public MemberService(DataListRepository repo, ListConsumerService listConsumerService) {
        this.repo = repo;
        this.listConsumerService = listConsumerService;
    }

    public void createEuropeanParliament() throws IngestException {
        EuropeMembers members = listConsumerService.createFromEuropeanParliamentAPI();
        Set<DataListEntity> dataListEntities = members.getMembers()
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

        DataList dataList = new DataList(HOUSE_EUROPEAN_PARLIAMENT + "_list", dataListEntities);

        repo.save(dataList);
    }

    public void createIrishParliament() throws IngestException {
        IrishMembers members = listConsumerService.createFromIrishParliamentAPI();
        Set<DataListEntity> dataListEntities = members.getMembers()
                .stream()
                .map(m -> createListEntity(m.getName(), HOUSE_NORTHERN_IRISH_ASSEMBLY))
                .collect(Collectors.toSet());

        DataList dataList = new DataList(HOUSE_NORTHERN_IRISH_ASSEMBLY + "_list", dataListEntities);

        repo.save(dataList);
    }

    public void createScottishParliament() throws IngestException {
        ScottishMembers members = listConsumerService.createFromScottishParliamentAPI();
        Set<DataListEntity> dataListEntities = members.getMembers()
                .stream()
                .map(m -> createListEntity(m.getName(), HOUSE_SCOTTISH_PARLIAMENT))
                .collect(Collectors.toSet());

        DataList dataList = new DataList(HOUSE_SCOTTISH_PARLIAMENT + "_list", dataListEntities);

        repo.save(dataList);
    }

    public void createCommonsUKParliament() throws IngestException {
        Members members = listConsumerService.createCommonsFromUKParliamentAPI();
        createUKParliament(members, HOUSE_COMMONS);
    }

    public void createLordsUKParliament() throws IngestException {
        Members members = listConsumerService.createLordsFromUKParliamentAPI();
        createUKParliament(members, HOUSE_LORDS);
    }

    private void createUKParliament(Members members, String house) {

        Set<DataListEntity> dataListEntities = members.getMembers()
                .stream()
                .map(m -> {
                    DataListEntity listEntity = new DataListEntity(m.getDisplayName(), m.getListName());
                    Set<DataListEntityProperty> properties = new HashSet<>();
                    properties.add(new DataListEntityProperty(PROP_HOUSE, m.getHouse()));
                    listEntity.setProperties(properties);
                    return listEntity;
                })
                .collect(Collectors.toSet());

        DataList dataList = new DataList(house + "_list", dataListEntities);

        repo.save(dataList);
    }

    private DataListEntity createListEntity(String name, final String house) {

        String[] names = name.split(",");
        String reference = String.join(" ", names).trim();
        Collections.reverse(Arrays.asList(names));
        String displayName = String.join(" ", names).trim();

        DataListEntity listEntity = new DataListEntity(displayName, reference);
        Set<DataListEntityProperty> properties = new HashSet<>();
        properties.add(new DataListEntityProperty(PROP_HOUSE, house));
        listEntity.setProperties(properties);
        return listEntity;

    }

}