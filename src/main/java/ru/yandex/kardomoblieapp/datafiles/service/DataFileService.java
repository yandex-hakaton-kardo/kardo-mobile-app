package ru.yandex.kardomoblieapp.datafiles.service;

import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;

import java.util.List;

public interface DataFileService {

    DataFile uploadFile(MultipartFile file, long userId);

    List<DataFile> uploadMultipleFiles(List<MultipartFile> files, long userId);

    void deleteFile(long fileId);

    byte[] downloadFileBytesById(long fileId);

    DataFile findDataFileById(long fileId);

    List<DataFile> findFilesFromPost(long postId);
}