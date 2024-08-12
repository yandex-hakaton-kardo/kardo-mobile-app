package ru.yandex.kardomoblieapp.shared.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Schema(description = "Сообщение об ошибках")
public class ErrorResponse {

    @Schema(description = "Ошибки")
    private final Map<String, String> errors = new HashMap<>();

    @Schema(description = "Статус ответа")
    private int status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Время возникновения ошибки")
    private final LocalDateTime timestamp = LocalDateTime.now();
}
