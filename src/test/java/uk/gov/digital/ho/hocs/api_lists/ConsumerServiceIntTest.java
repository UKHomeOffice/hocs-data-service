package uk.gov.digital.ho.hocs.api_lists;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.DataListRepository;
import uk.gov.digital.ho.hocs.model.DataList;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ComponentScan("uk.gov.digital.ho.hocs")
public class ConsumerServiceIntTest {

    private static final int PORT = 9100;

    private WireMockServer mockServer = new WireMockServer(PORT);

    @Autowired
    private DataListRepository repository;

    private ListConsumerService service;

    private RestTemplate restTemplate = new RestTemplate();

    private ListConsumerConfigurator configuration = new ListConsumerConfigurator(
            "http://localhost:9100/membersdataplatform/services/mnis/members/query/House=%s",
            "https://localhost:9100/api/members/scottish",
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

    @After
    public void TearDown() {
        mockServer.stop();
    }

}
