package projectcollab.task.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import projectcollab.task.domain.Task;
import projectcollab.task.domain.TaskStatus;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task,Long> {

    Optional<Task> findByIdAndProjectId(Long taskId, Long projectId);

        Page<Task> findAllByProjectId(Long projectId, Pageable pageable);

        Page<Task> findAllByProjectIdAndStatus(Long projectId, TaskStatus status,Pageable pageable);

        Page<Task> findAllByProjectIdAndTitleContainingIgnoreCase(Long projectId,String keyword,Pageable pageable);

    Page<Task> findAllByProjectIdAndStatusAndTitleContainingIgnoreCase(Long projectId, TaskStatus status, String keyword, Pageable pageable
    );

    void deleteAllByProjectId(Long projectId);


}
