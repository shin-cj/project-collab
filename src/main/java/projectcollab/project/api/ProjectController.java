package projectcollab.project.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import projectcollab.project.application.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(
            @RequestParam Long requesterId,
            @Valid @RequestBody ProjectCreateRequest request
    ) {
        return projectService.create(requesterId, request);
    }

    @GetMapping
    public List<ProjectResponse> findMyProjects(
            @RequestParam Long requesterId
    ) {
        return projectService.findMyProjects(requesterId);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse findById(
            @PathVariable Long projectId,
            @RequestParam Long requesterId
    ) {
        return projectService.findById(
                projectId,
                requesterId
        );
    }

    @PutMapping("/{projectId}")
    public ProjectResponse update(
            @PathVariable Long projectId,
            @RequestParam Long requesterId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        return projectService.update(
                projectId,
                requesterId,
                request
        );
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long projectId,
            @RequestParam Long requesterId
    ) {
        projectService.delete(projectId, requesterId);
    }
}