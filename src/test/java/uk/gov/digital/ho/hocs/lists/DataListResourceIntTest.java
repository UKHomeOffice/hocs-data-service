package uk.gov.digital.ho.hocs.lists;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.lists.dto.DataListEntityRecord;
import uk.gov.digital.ho.hocs.lists.dto.DataListRecord;
import uk.gov.digital.ho.hocs.lists.model.DataList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class DataListResourceIntTest {

    @Value("${auth.user}")
    private String API_USERNAME;

    @Value("${auth.pass}")
    private String API_PASSWORD;

    @Autowired
    private DataListRepository repository;
    @Autowired
    private TestRestTemplate restTemplate;


    @Transactional
    @Before
    public void setup() {
        repository.deleteAll();

        List<DataListEntityRecord> list = new ArrayList<>();
        DataListEntityRecord dle = new DataListEntityRecord("TopText", "top_val");
        list.add(dle);
        DataList datalist = new DataList(new DataListRecord("TestList", list));

        repository.save(datalist);
    }

    @Test
    public void shouldRetrieveAllEntities() throws IOException, JSONException {
        String actualList = restTemplate.withBasicAuth(API_USERNAME, API_PASSWORD).getForObject("/list/TestList", String.class);
        String expectedList = IOUtils.toString(getClass().getResourceAsStream("/DataListResourceIntTestListExpected.json"));

        JSONAssert.assertEquals(actualList, expectedList, false);
    }

    @Test
    public void shouldThrowNotFoundException() throws IOException, JSONException {
        ResponseEntity<String> response = restTemplate.withBasicAuth(API_USERNAME, API_PASSWORD).getForEntity("/list/TestLisNOTFOUND", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldCreateEntityOnValidPost() throws IOException {
        String jsonString = IOUtils.toString(getClass().getResourceAsStream("/DataListResourceIntValidPost.json"));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonString, httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.withBasicAuth(API_USERNAME, API_PASSWORD).postForEntity("/list", httpEntity, String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        DataList dataList = repository.findOneByName("ValidList");
        assertThat(dataList).isNotNull();
        assertThat(dataList.getName()).isEqualTo("ValidList");
        assertThat(dataList.getEntities()).hasSize(1);
        assertThat(new ArrayList<>(dataList.getEntities()).get(0).getText()).isEqualTo("Entity");
        assertThat(new ArrayList<>(dataList.getEntities()).get(0).getValue()).isEqualTo("VALUE_1");
    }

    @Test
    public void shouldThrowOnInvalidPost() {
        ResponseEntity<DataListRecord> responseEntity = restTemplate.withBasicAuth(API_USERNAME, API_PASSWORD).postForEntity("/list", new DataListRecord(null, null), DataListRecord.class);

        assertThat(HttpStatus.BAD_REQUEST).isEqualTo(responseEntity.getStatusCode());
    }

    @Test
    public void shouldRetrieveAllEntitiesAllLists() throws IOException, JSONException {
        String actualList = restTemplate.withBasicAuth(API_USERNAME, API_PASSWORD).getForObject("/list", String.class);
        String expectedList = IOUtils.toString(getClass().getResourceAsStream("/DataListResourceIntTestListExpectedArray.json"));

        JSONAssert.assertEquals(actualList,expectedList, false);
    }

}
