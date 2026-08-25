package projectcollab.project.api;

import projectcollab.project.domain.ProjectMember;
import projectcollab.project.domain.Role;

import java.time.LocalDateTime;

public record ProjectMemberResponse(Long memberId,
                                    Long userId,
                                    String userName,
                                    String email,
                                    Role role,
                                    LocalDateTime joinedAt) {
    public static ProjectMemberResponse from(ProjectMember member){
        return new ProjectMemberResponse(member.getId(),
                                        member.getUser().getId(),
                                        member.getUser().getName(),
                                        member.getUser().getEmail(),
                                        member.getRole(),
                                        member.getJoinedAt());
    }

}
