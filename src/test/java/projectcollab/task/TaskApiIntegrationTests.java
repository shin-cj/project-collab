package projectcollab.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.seed-data.enabled=false")
@AutoConfigureMockMvc
@Transactional
class TaskApiIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    TaskRepository taskRepository;

    User owner;
    User assignee;
    User otherMember;
    Project project;
    Task task;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("소유자", "task-owner@test.com"));
        assignee = userRepository.save(new User("담당자", "assignee@test.com"));
        otherMember = userRepository.save(
                new User("다른 멤버", "other-member@test.com")
        );

        project = projectRepository.save(
                new Project("작업 테스트", "작업 권한 테스트")
        );
        projectMemberRepository.save(
                new ProjectMember(project, owner, Role.OWNER)
        );
        projectMemberRepository.save(
                new ProjectMember(project, assignee, Role.MEMBER)
        );
        projectMemberRepository.save(
                new ProjectMember(project, otherMember, Role.MEMBER)
        );

        task = taskRepository.saveAndFlush(
                new Task(project, assignee, "로그인 API", "로그인 구현")
        );
    }

    @Test
    void taskListSupportsKeywordStatusAndPaging() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/tasks", project.getId())
                        .param("requesterId", assignee.getId().toString())
                        .param("keyword", "로그인")
                        .param("status", "TODO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("로그인 API"));
    }

    @Test
    void nonAssigneeMemberCannotUpdateTask() throws Exception {
        mockMvc.perform(put(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        project.getId(),
                        task.getId()
                )
                        .param("requesterId", otherMember.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody(task.getVersion(), "권한 없는 수정")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void staleTaskVersionReturnsConflict() throws Exception {
        Long staleVersion = task.getVersion();

        mockMvc.perform(put(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        project.getId(),
                        task.getId()
                )
                        .param("requesterId", assignee.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody(staleVersion, "첫 번째 수정")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(staleVersion + 1));

        mockMvc.perform(put(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        project.getId(),
                        task.getId()
                )
                        .param("requesterId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody(staleVersion, "오래된 수정")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    private String updateBody(Long version, String title) {
        return """
                {
                  "title": "%s",
                  "description": "수정 내용",
                  "assigneeId": %d,
                  "status": "IN_PROGRESS",
                  "version": %d
                }
                """.formatted(title, assignee.getId(), version);
    }
}
