package projectcollab.project.api;

import jakarta.validation.constraints.NotNull;
import projectcollab.project.domain.Role;

public record ProjectMemberAddRequest

    (@NotNull(message = "추가할 사용자 ID는 필수입니다.")
    Long userId,

    @NotNull(message = "역할은 필수 입니다.")
    Role role
    ){

}


