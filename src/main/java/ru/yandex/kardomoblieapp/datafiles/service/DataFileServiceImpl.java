package ru.yandex.kardomoblieapp.datafiles.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.datafiles.repository.DataFileRepository;
import ru.yandex.kardomoblieapp.shared.exception.DataFileStorageException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataFileServiceImpl implements DataFileService {

    @Value("${server.file-storage.directory}")
    private String baseFileDirectory;

    private final DataFileRepository dataFileRepository;

    /**
     * Загрузка и сохранение файла.
     *
     * @param fileToUpload файл
     * @param userId       идентификатор пользователя, загружающий файл
     * @return данные о загруженном файле.
     */
    @Override
    public DataFile uploadFile(MultipartFile fileToUpload, long userId) {
        final DataFile uploadedFile = createDataFileAndMoveToUserDirectory(fileToUpload, userId);
        final DataFile savedFile = dataFileRepository.save(uploadedFile);
        log.info("Пользователь с id '{}' загрузил фото профиля c id '{}'.", userId, savedFile.getId());
        return savedFile;
    }

    /**
     * Загрузка и сохранение нескольких файлов.
     *
     * @param files  список файлов для сохранения
     * @param userId идентификатор пользователя, загружающий файлы
     * @return данные о загруженных файлах.
     */
    @Override
    public List<DataFile> uploadMultipleFiles(List<MultipartFile> files, long userId) {
        final List<DataFile> dataFiles = new ArrayList<>();
        files.forEach(file -> dataFiles.add(createDataFileAndMoveToUserDirectory(file, userId)));
        final List<DataFile> savedFiles = dataFileRepository.saveAll(dataFiles);
        log.info("Пользователь с id '{}' загрузил список файлов в количестве: '{}'.", userId, savedFiles.size());
        return savedFiles;
    }

    /**
     * Удаление файла по идентификатору.
     *
     * @param fileId идентификатор файла
     */
    @Override
    @Transactional
    public void deleteFile(long fileId) {
        final DataFile fileToDelete = findFile(fileId);
        dataFileRepository.deleteById(fileId);
        deleteFileFromLocalStorage(fileToDelete);
    }

    /**
     * Скачивание файла.
     *
     * @param fileId идентификатор файла
     * @return массив данных
     */
    @Override
    public byte[] downloadFileBytesById(long fileId) {
        try {
            DataFile file = findFile(fileId);
            byte[] fileBytes = Files.readAllBytes(Paths.get(file.getFilePath()));
            log.info("Получение файла с id '{}' в байтовом представлении.", file.getFileName());
            return fileBytes;
        } catch (IOException e) {
            throw new DataFileStorageException(e.getCause().getMessage());
        }
    }

    /**
     * Получение данных о сохраненном файле по идентификатору.
     *
     * @param fileId идентификатор файла
     * @return данные о сохраненном файле
     */
    @Override
    public DataFile findDataFileById(long fileId) {
        DataFile file = findFile(fileId);
        log.info("Получение файла с id '{}'.", fileId);
        return file;
    }

    /**
     * Сохранение списка данных о файлах.
     *
     * @param dataFiles список данных о файлах
     * @return сохраненный список данных о файлах
     */
    @Override
    public List<DataFile> saveDataFiles(List<DataFile> dataFiles) {
        List<DataFile> savedFiles = dataFileRepository.saveAll(dataFiles);
        log.info("Сохранение группы файлов в количестве: '{}'.", savedFiles.size());
        return savedFiles;
    }

    /**
     * Удаление списка данных о файлах вместе с файлами, находящимися в хранилище.
     *
     * @param files список файлов
     */
    @Override
    public void deleteFiles(List<DataFile> files) {
        List<Long> oldFileIds = files.stream()
                .map(DataFile::getId)
                .toList();
        dataFileRepository.deleteAllById(oldFileIds);

        for (DataFile file : files) {
            deleteFileFromLocalStorage(file);
        }
    }

    private void deleteFileFromLocalStorage(DataFile file) {
        try {
            final Path currentPicture = Paths.get(file.getFilePath());
            Files.deleteIfExists(currentPicture);
        } catch (IOException e) {
            throw new DataFileStorageException(e.getCause().getMessage());
        }
    }

    private DataFile findFile(long fileId) {
        return dataFileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("Файл с id '" + fileId + "' не найден."));
    }

    private String createUserDirectoryIfNotExists(long userId) throws IOException {
        String userFileStorage = baseFileDirectory + "/" + userId + "/";
        final Path directory = Paths.get(userFileStorage);
        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
        }
        return userFileStorage;
    }

    private DataFile createDataFileAndMoveToUserDirectory(MultipartFile fileToUpload, long userId) {
        try {
            final String userFileStorage = createUserDirectoryIfNotExists(userId);


            final String fileExtension = FilenameUtils.getExtension(fileToUpload.getOriginalFilename());
            final String fileName = UUID.randomUUID() + "." + fileExtension;
            final String filePath = userFileStorage + fileName;
            final Path file = Paths.get(filePath);
            final DataFile dataFile = DataFile.builder()
                    .fileName(fileName)
                    .fileType(fileToUpload.getContentType())
                    .filePath(filePath)
                    .build();
            Files.copy(fileToUpload.getInputStream(), file);
            return dataFile;
        } catch (IOException e) {
            throw new DataFileStorageException(e.getLocalizedMessage());
        }
    }
}
