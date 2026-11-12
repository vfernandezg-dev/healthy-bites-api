package com.healthybites.community.app;

import com.healthybites.community.GroupDirectory;
import com.healthybites.community.domain.CommunityGroup;
import com.healthybites.community.domain.CommunityGroupRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GroupDirectoryService implements GroupDirectory {

    private final CommunityGroupRepository groups;

    public GroupDirectoryService(CommunityGroupRepository groups) {
        this.groups = groups;
    }

    @Override
    public Optional<String> nameOf(UUID groupId) {
        return groups.findById(groupId).map(CommunityGroup::getName);
    }
}
