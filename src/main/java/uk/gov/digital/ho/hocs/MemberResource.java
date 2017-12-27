package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.dto.DataListEntityRecord;
import uk.gov.digital.ho.hocs.dto.DataListRecord;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.House;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class MemberResource {
    private final MemberService memberService;

    @Autowired
    public MemberResource(MemberService memberService) {
        this.memberService = memberService;
    }

    @RequestMapping(value = "/members/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<House> getMinisterListByName(@PathVariable("name") String name) {
        log.info("House \"{}\" requested", name);
        try {
            House house = memberService.getHouseByName(name);
            return ResponseEntity.ok(house);
        } catch (ListNotFoundException e) {
            log.info("House \"{}\" not found", name);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // This is legacy behaviour and just returns all entries in the house table.
    // In a proper application House would be a first class object, but in Hocs everything is String!
    @Deprecated()
    @RequestMapping(value = "/list/ukvi_member_list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DataListRecord> getUkviMinisterListByName(@PathVariable("name") String name) {

        log.info("House \"{}\" requested", name);
        try {
            Set<House> houses = memberService.getAllHouses();
            List<DataListEntityRecord> dataListEntityRecords = houses.stream().map(h -> translateToDataListRecord(h)).flatMap(l -> l.stream()).collect(Collectors.toList());
            DataListRecord list = new DataListRecord("ukvi_member_list", dataListEntityRecords );
            return ResponseEntity.ok(list);
        } catch (ListNotFoundException e) {
            log.info("House \"{}\" not found", name);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }

    }

    private List<DataListEntityRecord> translateToDataListRecord(House house){
        List<DataListEntityRecord> dataListEntityRecordSet = house.getMembers().stream().map(m -> DataListEntityRecord.create(m, house.getName())).collect(Collectors.toList());
       return dataListEntityRecordSet;
    }

}