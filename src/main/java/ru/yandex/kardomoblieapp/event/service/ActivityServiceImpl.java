package ru.yandex.kardomoblieapp.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.kardomoblieapp.event.model.Activity;
import ru.yandex.kardomoblieapp.event.repository.ActivityRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    /**
     * Получение списка всех направлений.
     *
     * @return список направлений
     */
    @Override
    public List<Activity> findAll() {
        return activityRepository.findAll();
    }
}
