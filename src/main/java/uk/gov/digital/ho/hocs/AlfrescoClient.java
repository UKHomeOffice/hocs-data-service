package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.hocs.businessGroups.dto.PublishUnitRecord;
import uk.gov.digital.ho.hocs.businessGroups.model.BusinessUnit;
import uk.gov.digital.ho.hocs.exception.AlfrescoPostException;
import uk.gov.digital.ho.hocs.user.dto.PublishUserListRecord;
import uk.gov.digital.ho.hocs.user.dto.PublishUserRecord;
import uk.gov.digital.ho.hocs.user.model.User;

import java.util.*;

@Service
@Slf4j
public class AlfrescoClient {

    private final static String API_ENDPOINT_UNITS = "/alfresco/s/homeoffice/ctsv2/manageGroups";

    private final static int CHUNK_SIZE = 50;
    private final static String API_ENDPOINT_USERS = "/alfresco/s/importUsersAndGroups";

    private final String API_USERNAME;
    private final String API_PASSWORD;
    private final String API_HOST;


    @Autowired
    public AlfrescoClient(@Value("${alf.api.user}") String apiUsername,
                          @Value("${alf.api.pass}") String apiPassword,
                          @Value("${alf.api.host}") String apiHost) {

        this.API_USERNAME = apiUsername;
        this.API_PASSWORD = apiPassword;
        this.API_HOST = apiHost;
    }

    public void postUnits(Set<BusinessUnit> businessUnits) throws AlfrescoPostException {
        final String url = API_HOST + API_ENDPOINT_UNITS;

        log.info("Posting to: " + url);

        PublishUnitRecord publishUnitRecord = PublishUnitRecord.create(businessUnits);

        int statusCode = postRequest(url, publishUnitRecord).getStatusCodeValue();
        if (statusCode != HttpStatus.OK.value()) {
            throw new AlfrescoPostException("Failed to post request payload to Alfresco");
        }
    }

    public void postUsers(List<User> users) throws AlfrescoPostException {

        User[] userArray = new User[users.size()];
        userArray = users.toArray(userArray);

        List<PublishUserListRecord> userList = new ArrayList<>();

        for (int i = 0; i < userArray.length; i += CHUNK_SIZE) {
            User[] usersInChunk = Arrays.copyOfRange(userArray, i, Math.min(userArray.length,i+CHUNK_SIZE));
            userList.add(PublishUserListRecord.create(new HashSet<>(Arrays.asList(usersInChunk))));
        }

        postBatchedUsers(userList);
    }

    private void postBatchedUsers(List<PublishUserListRecord> recordList) throws AlfrescoPostException {

        final String url = API_HOST + API_ENDPOINT_USERS;

        log.info("Posting to: " + url);

        int batch = 1;
        for (PublishUserListRecord records : recordList) {

            log.info("Creating batch number: " + batch + " of " + recordList.size());
            Set<PublishUserRecord> users = records.getUsers();
            users.forEach(i -> log.info("Adding user -> " + i.getEmail()));

            int statusCode = postRequest(url, records).getStatusCodeValue();
            if (statusCode != HttpStatus.OK.value()) {
                throw new AlfrescoPostException("Failed to post request payload to Alfresco");
            }
            batch++;
        }

    }

    private <T> ResponseEntity postRequest(String url, T payload) {
        HttpEntity<T> request = new HttpEntity<>(payload, getBasicAuthHeaders());
        return new RestTemplate().exchange(url, HttpMethod.POST, request, String.class);
    }

    private HttpHeaders getBasicAuthHeaders() {
        return new HttpHeaders() {{
            String auth = String.format("%s:%s", API_USERNAME, API_PASSWORD);
            byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
            String authHeader = String.format("Basic %s", new String(encodedAuth));
            set("Authorization", authHeader);
            set("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        }};
    }
}
