package com.example.issuetracker.project;

import com.example.issuetracker.domain.ProjectMember;
import com.example.issuetracker.domain.User;
import com.example.issuetracker.repository.ProjectMemberRepository;
import com.example.issuetracker.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DefaultProjectMembershipService {

    private static final long DEFAULT_PROJECT_ID = 1L;

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;

    @Transactional
    public void addToDefaultProject(User user) {
        if (memberRepository.existsByProjectIdAndUserId(DEFAULT_PROJECT_ID, user.getId())) {
            return;
        }
        var project = projectRepository.findById(DEFAULT_PROJECT_ID)
                .orElseThrow(() -> new IllegalStateException("DEFAULT project is missing"));
        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        memberRepository.save(member);
    }
}
