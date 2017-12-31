package uk.gov.digital.ho.hocs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminService {

    private final BusinessGroupService businessGroupService;
    private final DataListService dataListService;
    private final MemberService memberService;
    private final TopicsService topicsService;
    private final UserService userService;

    @Autowired
    public AdminService(BusinessGroupService businessGroupService,
                        DataListService dataListService,
                        MemberService memberService,
                        TopicsService topicsService,
                        UserService userService) {
        this.businessGroupService = businessGroupService;
        this.dataListService = dataListService;
        this.memberService = memberService;
        this.topicsService = topicsService;
        this.userService = userService;
    }

    public void clearCache() {
        businessGroupService.clearCache();
        dataListService.clearCache();
        memberService.clearCache();
        topicsService.clearCache();
        userService.clearCache();
    }
}