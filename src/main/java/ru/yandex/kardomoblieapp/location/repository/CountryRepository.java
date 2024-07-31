package ru.yandex.kardomoblieapp.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.kardomoblieapp.location.model.Country;

public interface CountryRepository extends JpaRepository<Country, Long> {
}
