package ru.yandex.kardomoblieapp.datafiles.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;

public interface DataFileRepository extends JpaRepository<DataFile, Long> {
}
