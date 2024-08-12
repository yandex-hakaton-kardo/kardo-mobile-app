package ru.yandex.kardomoblieapp.datafiles.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Сущность сохраненного файла")
public class DataFileDto {

    @Schema(description = "Идентификатор файла")
    private Long id;

    @Schema(description = "Имя файла")
    private String fileName;

    @Schema(description = "Путь до файла")
    private String filePath;
}
