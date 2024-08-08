package ru.yandex.kardomoblieapp.event.mapper;

import org.mapstruct.Mapper;
import ru.yandex.kardomoblieapp.event.dto.ActivityDto;
import ru.yandex.kardomoblieapp.event.model.Activity;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    ActivityDto toDto(Activity activity);

    List<ActivityDto> toDtoList(List<Activity> activityList);
}
