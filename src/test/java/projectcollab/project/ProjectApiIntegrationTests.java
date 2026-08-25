package projectcollab.project;

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
import projectcollab.user.domain.User;
import projectcollab.user.infrastructure.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.seed-data.enabled=false")
@AutoConfigureMockMvc
@Transactional
class ProjectApiIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    User owner;
    User member;
    User outsider;
    Project project;
    ProjectMember ownerMembership;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("소유자", "owner@test.com"));
        member = userRepository.save(new User("멤버", "member@test.com"));
        outsider = userRepository.save(new User("외부인", "outsider@test.com"));

        project = projectRepository.save(
                new Project("테스트 프로젝트", "권한 테스트")
        );
        ownerMembership = projectMemberRepository.save(
                new ProjectMember(project, owner, Role.OWNER)
        );
        projectMemberRepository.save(
                new ProjectMember(project, member, Role.MEMBER)
        );
        projectMemberRepository.flush();
    }

    @Test
    void projectCreatorBecomesOwner() throws Exception {
        User creator = userRepository.save(
                new User("생성자", "creator@test.com")
        );

        mockMvc.perform(post("/api/projects")
                        .param("requesterId", creator.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 프로젝트",
                                  "description": "프로젝트 생성 테스트"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.myRole").value("OWNER"))
                .andExpect(jsonPath("$.name").value("새 프로젝트"));
    }

    @Test
    void nonMemberCannotReadProject() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}", project.getId())
                        .param("requesterId", outsider.getId().toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void memberCannotUpdateProject() throws Exception {
        mockMvc.perform(put("/api/projects/{projectId}", project.getId())
                        .param("requesterId", member.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "권한 없는 수정",
                                  "description": "수정되면 안 됨"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void lastOwnerCannotBeDemoted() throws Exception {
        mockMvc.perform(patch(
                        "/api/projects/{projectId}/members/{memberId}/role",
                        project.getId(),
                        ownerMembership.getId()
                )
                        .param("requesterId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "MEMBER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void invalidRequestHasFieldErrors() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .param("requesterId", owner.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "잘못된 요청"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }
}
