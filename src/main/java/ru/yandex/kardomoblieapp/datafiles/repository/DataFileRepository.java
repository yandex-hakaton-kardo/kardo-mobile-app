package ru.yandex.kardomoblieapp.datafiles.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;

import java.util.List;

public interface DataFileRepository extends JpaRepository<DataFile, Long> {

    List<DataFile> findAllFilesByPostId(long postId);

    DataFile findByFilePath(String filePath);
}
