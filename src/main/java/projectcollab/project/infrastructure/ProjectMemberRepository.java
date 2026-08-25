package projectcollab.project.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import projectcollab.project.domain.ProjectMember;
import projectcollab.project.domain.Role;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository
        extends JpaRepository<ProjectMember, Long> {

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMember> findAllByProjectId(Long projectId);

    List<ProjectMember> findAllByUserId(Long userId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectIdAndRole(Long projectId, Role role);

    void deleteAllByProjectId(Long projectId);

    Optional<ProjectMember> findByIdAndProjectId(
            Long memberId,
            Long projectId
    );
}