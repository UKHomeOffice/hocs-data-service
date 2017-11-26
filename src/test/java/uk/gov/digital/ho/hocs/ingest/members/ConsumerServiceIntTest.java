package uk.gov.digital.ho.hocs.ingest.members;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.DataListRepository;
import uk.gov.digital.ho.hocs.model.DataList;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ComponentScan("uk.gov.digital.ho.hocs")
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ConsumerServiceIntTest {

    private static final int PORT = 9100;

    private WireMockServer mockServer = new WireMockServer(PORT);

    @Autowired
    private DataListRepository repository;

    private ListConsumerService service;

    private RestTemplate restTemplate = new RestTemplate();

    private ListConsumerConfigurator configuration = new ListConsumerConfigurator(
            "http://localhost:9100/membersdataplatform/services/mnis/members/query/House=%s",
            "http://localhost:9100/api/members/scottish",
            "http://localhost:9100/api/members/ni",
            "http://localhost:9100/meps/en/xml.html?query=full&filter=all"
    );

    @Before
    public void SetUp() {

        repository.deleteAll();

        service = new ListConsumerService(repository, restTemplate, configuration);

        mockServer.start();

    }

    @Test
    public void commonsTest() throws Exception{

        String responseBody = IOUtils.toString(getClass().getResourceAsStream("/api_responses/commons.xml"));

        configureFor("localhost", PORT);
        stubFor(get(urlEqualTo("/membersdataplatform/services/mnis/members/query/House=commons"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(responseBody)));

        service.createFromUKParliamentAPI("commons");

        DataList commons = repository.findDataListByName("commons_list");

        Assert.assertNotNull(commons);
        Assert.assertEquals(DataList.class, commons.getClass());
        Assert.assertEquals(3, commons.getEntities().size());

    }

    @Test
    public void lordsTest() throws Exception{

        String responseBody = IOUtils.toString(getClass().getResourceAsStream("/api_responses/lords.xml"));

        configureFor("localhost", PORT);
        stubFor(get(urlEqualTo("/membersdataplatform/services/mnis/members/query/House=lords"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(responseBody)));

        service.createFromUKParliamentAPI("lords");

        DataList lords = repository.findDataListByName("lords_list");

        Assert.assertNotNull(lords);
        Assert.assertEquals(DataList.class, lords.getClass());
        Assert.assertEquals(3, lords.getEntities().size());

    }

    @Test
    public void scottishParliamentTest() throws Exception{

        String responseBody = IOUtils.toString(getClass().getResourceAsStream("/api_responses/scottish.json"));

        configureFor("localhost", PORT);
        stubFor(get(urlEqualTo("/api/members/scottish"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        service.createFromScottishParliamentAPI();

        DataList scottish_parliament = repository.findDataListByName("scottish_parliament_list");

        Assert.assertNotNull(scottish_parliament);
        Assert.assertEquals(DataList.class, scottish_parliament.getClass());
        Assert.assertEquals(3, scottish_parliament.getEntities().size());

    }

    @Test
    public void northernIrishAssemblyTest() throws Exception{

        String responseBody = IOUtils.toString(getClass().getResourceAsStream("/api_responses/ni.xml"));

        configureFor("localhost", PORT);
        stubFor(get(urlEqualTo("/api/members/ni"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(responseBody)));

        service.createFromIrishParliamentAPI();

        DataList northernIrishAssembly = repository.findDataListByName("northern_irish_assembly_list");

        Assert.assertNotNull(northernIrishAssembly);
        Assert.assertEquals(DataList.class, northernIrishAssembly.getClass());
        Assert.assertEquals(3, northernIrishAssembly.getEntities().size());

    }

    @Test
    public void europeanParliamentTest() throws Exception{

        String responseBody = IOUtils.toString(getClass().getResourceAsStream("/api_responses/eu.xml"));

        configureFor("localhost", PORT);
        stubFor(get(urlEqualTo("/meps/en/xml.html?query=full&filter=all"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody(responseBody)));

        service.createFromEuropeanParliamentAPI();

        DataList european_parliament = repository.findDataListByName("european_parliament_list");

        Assert.assertNotNull(european_parliament);
        Assert.assertEquals(DataList.class, european_parliament.getClass());
        Assert.assertEquals(3, european_parliament.getEntities().size());

    }

    @After
    public void TearDown() {
        mockServer.stop();
    }

}
