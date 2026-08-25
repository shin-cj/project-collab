package projectcollab.task.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import projectcollab.task.application.TaskService;
import projectcollab.task.domain.TaskStatus;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(
            @PathVariable Long projectId,
            @RequestParam Long requesterId,
            @Valid @RequestBody TaskCreateRequest request
    ) {
        return taskService.create(projectId, requesterId, request);
    }

    @GetMapping("/{taskId}")
    public TaskResponse findById(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestParam Long requesterId
    ) {
        return taskService.findById(projectId, taskId, requesterId);
    }

    @GetMapping
    public TaskPageResponse findAll(
            @PathVariable Long projectId,
            @RequestParam Long requesterId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return taskService.findAll(
                projectId,
                requesterId,
                keyword,
                status,
                pageable
        );
    }

    @PutMapping("/{taskId}")
    public TaskResponse update(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestParam Long requesterId,
            @Valid @RequestBody TaskUpdateRequest request
    ) {
        return taskService.update(
                projectId,
                taskId,
                requesterId,
                request
        );
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestParam Long requesterId
    ) {
        taskService.delete(projectId, taskId, requesterId);
    }
}
