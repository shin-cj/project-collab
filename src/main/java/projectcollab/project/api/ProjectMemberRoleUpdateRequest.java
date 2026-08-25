package projectcollab.project.api;

import jakarta.validation.constraints.NotNull;
import projectcollab.project.domain.Role;

public record ProjectMemberRoleUpdateRequest(@NotNull(message = "변경할 역할은 필수입니다.")Role role) {
}
