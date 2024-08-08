package ru.yandex.kardomoblieapp.participation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Обновленные данные для заявки")
public class ParticipationUpdateRequest {

    @Size(min = 2, max = 50, message = "Ссылка не может быть пустой и должна содержать от 2 до 50 символов.")
    @Schema(description = "Ссылка на на файл для участия в соревновании")
    @Pattern(regexp = "^[a-z0-9:/@.#$%&?,*]{2,30}$", message = "Ссылка не может быть пустой и должа содержать от 2 до 50 символов.")
    private String linkToContestFile;
}
