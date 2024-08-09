package ru.yandex.kardomoblieapp.participation.mapper;

import org.mapstruct.Mapper;
import ru.yandex.kardomoblieapp.participation.dto.NewScoreRequest;
import ru.yandex.kardomoblieapp.participation.model.Score;

@Mapper(componentModel = "spring")
public interface ScoreMapper {
    Score toModel(NewScoreRequest newScoreRequest);
}
