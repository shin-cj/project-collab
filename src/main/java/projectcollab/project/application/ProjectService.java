package projectcollab.project.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import projectcollab.project.api.ProjectCreateRequest;
import projectcollab.project.api.ProjectResponse;
import projectcollab.project.api.ProjectUpdateRequest;
import projectcollab.project.domain.Project;
import projectcollab.project.domain.ProjectMember;
import projectcollab.project.domain.Role;
import projectcollab.project.infrastructure.ProjectMemberRepository;
import projectcollab.project.infrastructure.ProjectRepository;
import projectcollab.task.infrastructure.TaskRepository;
import projectcollab.user.domain.User;
import projectcollab.user.infrastructure.UserRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse create(
            Long requesterId,
            ProjectCreateRequest request
    ) {
        User creator = findUser(requesterId);

        Project project = new Project(
                request.name(),
                request.description()
        );

        Project savedProject = projectRepository.save(project);

        ProjectMember ownerMember = new ProjectMember(
                savedProject,
                creator,
                Role.OWNER
        );

        projectMemberRepository.save(ownerMember);

        return ProjectResponse.from(savedProject, Role.OWNER);
    }

    public ProjectResponse findById(
            Long projectId,
            Long requesterId
    ) {
        ProjectMember membership = findMembership(
                projectId,
                requesterId
        );

        return ProjectResponse.from(
                membership.getProject(),
                membership.getRole()
        );
    }

    public List<ProjectResponse> findMyProjects(Long requesterId) {
        findUser(requesterId);

        return projectMemberRepository.findAllByUserId(requesterId)
                .stream()
                .map(member -> ProjectResponse.from(
                        member.getProject(),
                        member.getRole()
                ))
                .toList();
    }

    @Transactional
    public ProjectResponse update(
            Long projectId,
            Long requesterId,
            ProjectUpdateRequest request
    ) {
        ProjectMember membership = findMembership(
                projectId,
                requesterId
        );

        requireOwnerOrAdmin(membership);

        Project project = membership.getProject();
        project.update(
                request.name(),
                request.description()
        );
        projectRepository.flush();

        return ProjectResponse.from(
                project,
                membership.getRole()
        );
    }

    @Transactional
    public void delete(
            Long projectId,
            Long requesterId
    ) {
        ProjectMember membership = findMembership(
                projectId,
                requesterId
        );

        if (membership.getRole() != Role.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "프로젝트는 OWNER만 삭제할 수 있습니다."
            );
        }

        taskRepository.deleteAllByProjectId(projectId);
        projectMemberRepository.deleteAllByProjectId(projectId);
        projectRepository.delete(membership.getProject());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "사용자를 찾을 수 없습니다."
                ));
    }

    private ProjectMember findMembership(
            Long projectId,
            Long userId
    ) {
        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "프로젝트에 접근할 권한이 없습니다."
                ));
    }

    private void requireOwnerOrAdmin(ProjectMember membership) {
        Role role = membership.getRole();

        if (role != Role.OWNER && role != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "OWNER 또는 ADMIN 권한이 필요합니다."
            );
        }
    }
}
