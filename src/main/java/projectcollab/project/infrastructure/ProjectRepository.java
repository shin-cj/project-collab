package projectcollab.project.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import projectcollab.project.domain.Project;

public interface ProjectRepository extends JpaRepository<Project,Long> {


}
