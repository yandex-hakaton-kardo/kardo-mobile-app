package ru.yandex.kardomoblieapp.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.kardomoblieapp.event.model.Activity;

public interface  ActivityRepository extends JpaRepository<Activity, Long> {
}
