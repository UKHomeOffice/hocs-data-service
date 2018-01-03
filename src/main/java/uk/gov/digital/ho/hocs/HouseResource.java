package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.dto.dataList.DataListEntityRecord;
import uk.gov.digital.ho.hocs.dto.dataList.DataListRecord;
import uk.gov.digital.ho.hocs.dto.house.HouseRecord;
import uk.gov.digital.ho.hocs.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.exception.IngestException;
import uk.gov.digital.ho.hocs.exception.ListNotFoundException;
import uk.gov.digital.ho.hocs.model.House;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class HouseResource {
    private final HouseService houseService;

    @Autowired
    public HouseResource(HouseService houseService) {
        this.houseService = houseService;
    }

    @RequestMapping(value = "/houses", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity updateHouse(@RequestBody House house) {
        if (house != null) {
            log.info("Parsing House {}", house.getName());
            try {
                houseService.updateHouse(house);
                return ResponseEntity.ok().build();
            } catch (EntityCreationException e) {
                log.info("{} House not created", house.getName());
                log.info(e.getMessage());
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value = "/houses/refresh", method = RequestMethod.GET)
    public ResponseEntity getFromApi() {
        try {
            houseService.updateWebMemberLists();
            return ResponseEntity.ok().build();
        } catch (IngestException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/houses/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<HouseRecord> getHouseByName(@PathVariable("name") String name) {
        log.info("House \"{}\" requested", name);
        try {
            House house = houseService.getHouseByName(name);
            return ResponseEntity.ok(HouseRecord.create(house));
        } catch (ListNotFoundException e) {
            log.info("House \"{}\" not found", name);
            log.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(value = "/houses", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Set<HouseRecord>> getAllHouses() {
        log.info(" All Houses requested");
        Set<House> houses = houseService.getAllHouses();
        return ResponseEntity.ok(houses.stream().map(HouseRecord::create).collect(Collectors.toSet()));
    }

    // This is legacy behaviour and just returns all entries in the house table.
    @Deprecated()
    @RequestMapping(value = "/list/ukvi_member_list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DataListRecord> getUkviMinisterListByName() {
    log.info("ukvi_member_list \"{}\" requested");
    Set<House> houses = houseService.getAllHouses();
    List<DataListEntityRecord> dataListEntityRecords = houses.stream().map(h -> translateToDataListRecord(h)).flatMap(l -> l.stream()).collect(Collectors.toList());
    DataListRecord list = new DataListRecord("ukvi_member_list", dataListEntityRecords );
    return ResponseEntity.ok(list);

    }

    private List<DataListEntityRecord> translateToDataListRecord(House house){
        List<DataListEntityRecord> dataListEntityRecordSet = house.getMembers().stream().map(m -> DataListEntityRecord.create(m, house.getName())).collect(Collectors.toList());
       return dataListEntityRecordSet;
    }

}