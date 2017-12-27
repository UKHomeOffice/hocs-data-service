package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.exception.IngestException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.ingest.members.*;
import uk.gov.digital.ho.hocs.model.House;
import uk.gov.digital.ho.hocs.model.Member;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MemberService {

    final String HOUSE_LORDS = "lords";
    final String HOUSE_COMMONS = "commons";
    final String HOUSE_SCOTTISH_PARLIAMENT = "scottish_parliament";
    final String HOUSE_NORTHERN_IRISH_ASSEMBLY = "northern_irish_assembly";
    final String HOUSE_EUROPEAN_PARLIAMENT = "european_parliament";


    private final MemberRepository repo;
    private final ListConsumerService listConsumerService;

    @Autowired
    public MemberService(MemberRepository repo, ListConsumerService listConsumerService) {
        this.repo = repo;
        this.listConsumerService = listConsumerService;
    }

    public void createEuropeanParliament() throws IngestException {
        EuropeMembers members = listConsumerService.createFromEuropeanParliamentAPI();
        Set<Member> dataListEntities = members.getMembers()
                .stream()
                .map(m -> new Member(m.getName()))
                .collect(Collectors.toSet());

        House house = new House(HOUSE_EUROPEAN_PARLIAMENT, dataListEntities);

        repo.save(house);
    }

    public void createIrishParliament() throws IngestException {
        IrishMembers members = listConsumerService.createFromIrishParliamentAPI();
        Set<Member> dataListEntities = members.getMembers()
                .stream()
                .map(m -> new Member(m.getName()))
                .collect(Collectors.toSet());

        House house = new House(HOUSE_NORTHERN_IRISH_ASSEMBLY, dataListEntities);

        repo.save(house);
    }

    public void createScottishParliament() throws IngestException {
        ScottishMembers members = listConsumerService.createFromScottishParliamentAPI();
        Set<Member> dataListEntities = members.getMembers()
                .stream()
                .map(m -> new Member(m.getName()))
                .collect(Collectors.toSet());

        House house = new House(HOUSE_SCOTTISH_PARLIAMENT, dataListEntities);

        repo.save(house);
    }

    public void createCommonsUKParliament() throws IngestException {
        Members members = listConsumerService.createCommonsFromUKParliamentAPI();

        Set<Member> dataListEntities = members.getMembers()
                .stream()
                .map(m -> new Member(m.getName()))
                .collect(Collectors.toSet());

        House house = new House(HOUSE_COMMONS, dataListEntities);

        repo.save(house);
    }

    public void createLordsUKParliament() throws IngestException {
        Members members = listConsumerService.createLordsFromUKParliamentAPI();

        Set<Member> dataListEntities = members.getMembers()
                .stream()
                .map(m -> new Member(m.getName()))
                .collect(Collectors.toSet());

        House house = new House(HOUSE_LORDS, dataListEntities);

        repo.save(house);
    }

    public House getHouseByName(String name) throws ListNotFoundException {
        House house = repo.findOneByNameAndDeletedIsFalse(name);
        if (house == null) {
            throw new ListNotFoundException();
        }
        return house;
    }

    public Set<House> getAllHouses() throws ListNotFoundException {
        Set<House> houses = repo.findAllByDeletedIsFalse();
        if(houses.isEmpty()){
            throw new ListNotFoundException();
        }
        return houses;
    }

}