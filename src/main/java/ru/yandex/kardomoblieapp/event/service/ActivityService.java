package ru.yandex.kardomoblieapp.event.service;

import ru.yandex.kardomoblieapp.event.model.Activity;

import java.util.List;

public interface ActivityService {
    List<Activity> findAll();
}
