package ru.yandex.kardomoblieapp.participation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationDto;
import ru.yandex.kardomoblieapp.participation.dto.ParticipationRequest;
import ru.yandex.kardomoblieapp.participation.model.Participation;
import ru.yandex.kardomoblieapp.user.dto.UserUpdateRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipationMapper {

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "user.id", target = "userId")
    ParticipationDto toDto(Participation participation);

    UserUpdateRequest toUserUpdateRequest(ParticipationRequest participationRequest);

    List<ParticipationDto> toDtoList(List<Participation> participations);
}
