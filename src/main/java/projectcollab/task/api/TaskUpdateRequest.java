package projectcollab.task.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import projectcollab.task.domain.TaskStatus;

public record TaskUpdateRequest(
        @NotBlank(message = "작업 제목은 필수입니다.")
        @Size(max = 200, message = "작업 제목은 200자 이하여야 합니다.")
        String title,

        @Size(max = 2000, message = "작업 설명은 2000자 이하여야 합니다.")
        String description,

        Long assigneeId,

        @NotNull(message = "작업 상태는 필수입니다.")
        TaskStatus status,

        @NotNull(message = "작업 버전은 필수입니다.")
        Long version
) {
}
