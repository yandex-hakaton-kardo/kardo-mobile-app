package ru.yandex.kardomoblieapp.datafiles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataFileDto {

    private Long id;

    private String fileName;
}
