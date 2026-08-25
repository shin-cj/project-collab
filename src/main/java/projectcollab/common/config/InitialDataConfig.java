package projectcollab.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import projectcollab.project.domain.Project;
import projectcollab.project.domain.ProjectMember;
import projectcollab.project.domain.Role;
import projectcollab.project.infrastructure.ProjectMemberRepository;
import projectcollab.project.infrastructure.ProjectRepository;
import projectcollab.task.domain.Task;
import projectcollab.task.infrastructure.TaskRepository;
import projectcollab.user.domain.User;
import projectcollab.user.infrastructure.UserRepository;

import java.util.List;

@Component
@ConditionalOnProperty(
        prefix = "app.seed-data",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class InitialDataConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    public InitialDataConfig(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        User owner = new User("신재형1", "shin1@example.com");
        User admin = new User("신재형2", "shin2@example.com");
        User member = new User("신재형3", "shin3@example.com");
        userRepository.saveAll(List.of(owner, admin, member));

        Project project = projectRepository.save(
                new Project(
                        "Project Collab 개발",
                        "프로젝트 협업 서비스 예시 프로젝트"
                )
        );

        projectMemberRepository.saveAll(List.of(
                new ProjectMember(project, owner, Role.OWNER),
                new ProjectMember(project, admin, Role.ADMIN),
                new ProjectMember(project, member, Role.MEMBER)
        ));

        taskRepository.saveAll(List.of(
                new Task(
                        project,
                        member,
                        "사용자 API 구현",
                        "사용자 등록 및 조회 API 구현"
                ),
                new Task(
                        project,
                        admin,
                        "프로젝트 권한 테스트",
                        "OWNER, ADMIN, MEMBER 권한 검증"
                )
        ));
    }
}
