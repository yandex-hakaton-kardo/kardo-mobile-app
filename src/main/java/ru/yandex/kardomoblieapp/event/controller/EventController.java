package ru.yandex.kardomoblieapp.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.kardomoblieapp.event.dto.EventDto;
import ru.yandex.kardomoblieapp.event.mapper.EventMapper;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.user.dto.UserDto;
import ru.yandex.kardomoblieapp.user.mapper.UserMapper;
import ru.yandex.kardomoblieapp.user.model.User;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventMapper eventMapper;






}
