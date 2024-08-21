package ru.yandex.kardomoblieapp.datafiles.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Сущность сохраненного файла")
public record DataFileDto(@Schema(description = "Идентификатор файла")
                          Long id,
                          @Schema(description = "Имя файла")
                          String fileName,
                          @Schema(description = "Путь до файла")
                          String filePath) {

}
