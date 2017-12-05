package uk.gov.digital.ho.hocs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AlfrescoConfiguration {

    final static String API_ENDPOINT_USERS = "/alfresco/s/importUsersAndGroups/";
    final static Integer API_CHUNK_SIZE = 50;

    final String API_USERNAME;
    final String API_PASSWORD;
    final String API_HOST;

    public AlfrescoConfiguration(@Value("${hocs.api.user}") String apiUsername,
                                 @Value("${hocs.api.pass}") String apiPassword,
                                 @Value("${hocs.api.endpoint}") String apiHost) {

        this.API_USERNAME = apiUsername;
        this.API_PASSWORD = apiPassword;
        this.API_HOST = apiHost;

    }

}
