package projectcollab.project.api;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import projectcollab.project.application.ProjectMemberService;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService){
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public List<ProjectMemberResponse> findAll(@PathVariable Long projectId, @RequestParam Long requesterId){
        return projectMemberService.findAll(projectId,requesterId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectMemberResponse add(@PathVariable Long projectId, @RequestParam Long requesterId, @Valid @RequestBody ProjectMemberAddRequest request){
        return projectMemberService.add(projectId,requesterId,request);
    }

    @PatchMapping("/{memberId}/role")
    public ProjectMemberResponse changeRole(@PathVariable Long projectId,@PathVariable Long memberId, @RequestParam Long requesterId,@Valid @RequestBody ProjectMemberRoleUpdateRequest request){
        return projectMemberService.changeRole(projectId,memberId,requesterId,request);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @RequestParam Long requesterId
    ) {
        projectMemberService.remove(
                projectId,
                memberId,
                requesterId
        );
    }


}
