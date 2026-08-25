package projectcollab.common.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@ConditionalOnProperty(
        prefix = "app.seed-data",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class InitialDataConfig {

    @Bean
    CommandLineRunner initialDataRunner(
            InitialDataLoader initialDataLoader
    ) {
        return args -> initialDataLoader.load();
    }

    @Bean
    InitialDataLoader initialDataLoader(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            TaskRepository taskRepository
    ) {
        return new InitialDataLoader(
                userRepository,
                projectRepository,
                projectMemberRepository,
                taskRepository
        );
    }

    static class InitialDataLoader {

        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
        private final ProjectMemberRepository projectMemberRepository;
        private final TaskRepository taskRepository;

        InitialDataLoader(
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

        @Transactional
        public void load() {
            if (userRepository.count() > 0) {
                return;
            }

            User owner = new User("홍길동", "owner@example.com");
            User admin = new User("김관리", "admin@example.com");
            User member = new User("이멤버", "member@example.com");
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

            Task firstTask = new Task(
                    project,
                    member,
                    "사용자 API 구현",
                    "사용자 등록 및 조회 API 구현"
            );
            Task secondTask = new Task(
                    project,
                    admin,
                    "프로젝트 권한 테스트",
                    "OWNER, ADMIN, MEMBER 권한 검증"
            );
            taskRepository.saveAll(List.of(firstTask, secondTask));
        }
    }
}
