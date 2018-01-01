package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminService {

    private final BusinessGroupService businessGroupService;
    private final DataListService dataListService;
    private final HouseService houseService;
    private final TopicsService topicsService;
    private final UserService userService;

    @Autowired
    public AdminService(BusinessGroupService businessGroupService,
                        DataListService dataListService,
                        HouseService houseService,
                        TopicsService topicsService,
                        UserService userService) {
        this.businessGroupService = businessGroupService;
        this.dataListService = dataListService;
        this.houseService = houseService;
        this.topicsService = topicsService;
        this.userService = userService;
    }

    public void clearCache() {
        businessGroupService.clearCache();
        dataListService.clearCache();
        houseService.clearCache();
        topicsService.clearCache();
        userService.clearCache();
    }
}