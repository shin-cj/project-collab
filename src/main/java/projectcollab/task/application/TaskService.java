package projectcollab.task.application;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import projectcollab.project.domain.ProjectMember;
import projectcollab.project.domain.Role;
import projectcollab.project.infrastructure.ProjectMemberRepository;
import projectcollab.task.api.TaskCreateRequest;
import projectcollab.task.api.TaskPageResponse;
import projectcollab.task.api.TaskResponse;
import projectcollab.task.api.TaskUpdateRequest;
import projectcollab.task.domain.Task;
import projectcollab.task.domain.TaskStatus;
import projectcollab.task.infrastructure.TaskRepository;
import projectcollab.user.domain.User;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(
            TaskRepository taskRepository,
            ProjectMemberRepository projectMemberRepository
    ) {
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public TaskResponse create(
            Long projectId,
            Long requesterId,
            TaskCreateRequest request
    ) {
        ProjectMember requesterMembership = findMembership(
                projectId,
                requesterId
        );
        User assignee = findAssignee(projectId, request.assigneeId());

        Task task = new Task(
                requesterMembership.getProject(),
                assignee,
                request.title(),
                request.description()
        );

        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse findById(
            Long projectId,
            Long taskId,
            Long requesterId
    ) {
        findMembership(projectId, requesterId);
        return TaskResponse.from(findTask(projectId, taskId));
    }

    public TaskPageResponse findAll(
            Long projectId,
            Long requesterId,
            String keyword,
            TaskStatus status,
            Pageable pageable
    ) {
        findMembership(projectId, requesterId);

        boolean hasKeyword = StringUtils.hasText(keyword);
        Page<Task> tasks;

        if (hasKeyword && status != null) {
            tasks = taskRepository
                    .findAllByProjectIdAndStatusAndTitleContainingIgnoreCase(
                            projectId,
                            status,
                            keyword.trim(),
                            pageable
                    );
        } else if (hasKeyword) {
            tasks = taskRepository
                    .findAllByProjectIdAndTitleContainingIgnoreCase(
                            projectId,
                            keyword.trim(),
                            pageable
                    );
        } else if (status != null) {
            tasks = taskRepository.findAllByProjectIdAndStatus(
                    projectId,
                    status,
                    pageable
            );
        } else {
            tasks = taskRepository.findAllByProjectId(
                    projectId,
                    pageable
            );
        }

        return TaskPageResponse.from(tasks);
    }

    @Transactional
    public TaskResponse update(
            Long projectId,
            Long taskId,
            Long requesterId,
            TaskUpdateRequest request
    ) {
        ProjectMember requesterMembership = findMembership(
                projectId,
                requesterId
        );
        Task task = findTask(projectId, taskId);
        requireTaskManager(task, requesterMembership);

        if (!Objects.equals(task.getVersion(), request.version())) {
            throw conflict();
        }

        User assignee = findAssignee(projectId, request.assigneeId());
        task.update(
                request.title(),
                request.description(),
                assignee,
                request.status()
        );

        try {
            taskRepository.flush();
        } catch (OptimisticLockingFailureException exception) {
            throw conflict();
        }

        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(
            Long projectId,
            Long taskId,
            Long requesterId
    ) {
        ProjectMember requesterMembership = findMembership(
                projectId,
                requesterId
        );
        Task task = findTask(projectId, taskId);
        requireTaskManager(task, requesterMembership);
        taskRepository.delete(task);
    }

    private ProjectMember findMembership(Long projectId, Long userId) {
        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "프로젝트에 접근할 권한이 없습니다."
                ));
    }

    private User findAssignee(Long projectId, Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }

        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, assigneeId)
                .map(ProjectMember::getUser)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "담당자는 프로젝트 멤버여야 합니다."
                ));
    }

    private Task findTask(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "작업을 찾을 수 없습니다."
                ));
    }

    private void requireTaskManager(
            Task task,
            ProjectMember requesterMembership
    ) {
        Role role = requesterMembership.getRole();
        boolean manager = role == Role.OWNER || role == Role.ADMIN;
        boolean assignee = task.getAssignee() != null
                && task.getAssignee().getId()
                .equals(requesterMembership.getUser().getId());

        if (!manager && !assignee) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "작업 담당자 또는 OWNER, ADMIN 권한이 필요합니다."
            );
        }
    }

    private ResponseStatusException conflict() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "다른 사용자가 먼저 작업을 수정했습니다. 최신 작업을 다시 조회하세요."
        );
    }
}
