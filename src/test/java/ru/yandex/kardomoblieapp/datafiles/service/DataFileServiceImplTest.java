package ru.yandex.kardomoblieapp.datafiles.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.yandex.kardomoblieapp.TestUtils.POSTGRES_VERSION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class DataFileServiceImplTest {

    @Autowired
    private DataFileService dataFileService;

    private DataFile dataFile;
    private DataFile dataFile1;

    private long userId;

    private long unknownId;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_VERSION);

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void init() {
        dataFile = DataFile.builder()
                .fileName("fileName")
                .fileType(MediaType.IMAGE_JPEG_VALUE)
                .filePath("filePath")
                .build();
        dataFile1 = DataFile.builder()
                .fileName("fileName1")
                .fileType(MediaType.IMAGE_JPEG_VALUE)
                .filePath("filePath1")
                .build();

        userId = 1L;
        unknownId = 99999L;
    }

    @Test
    @DisplayName("Загрузка файла, путь сохранения должен содержать id пользователя")
    @SneakyThrows
    void uploadFile_whenSuccessful_filePathShouldContainUserId() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "fileName", MediaType.IMAGE_JPEG_VALUE, inputStream);
        DataFile savedFile = dataFileService.uploadFile(file, userId);

        assertThat(savedFile, notNullValue());
        assertThat(savedFile.getId(), greaterThan(0L));
        assertThat(savedFile.getFileName(), is(file.getOriginalFilename()));
        assertThat(savedFile.getFileType(), is(file.getContentType()));
        assertThat(savedFile.getFilePath(), endsWith("/" + userId + "/" + dataFile.getFileName()));
    }

    @Test
    @SneakyThrows
    @DisplayName("Загрузка нескольких файлов")
    void uploadMultipleFiles_whenSuccessful_ShouldReturnListOfDataFiles() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file1 = new MockMultipartFile("file", "fileName1", MediaType.IMAGE_JPEG_VALUE, inputStream);
        MockMultipartFile file2 = new MockMultipartFile("file", "fileName2", MediaType.MULTIPART_FORM_DATA_VALUE, inputStream);
        MockMultipartFile file3 = new MockMultipartFile("file", "fileName3", MediaType.IMAGE_JPEG_VALUE, inputStream);
        MockMultipartFile file4 = new MockMultipartFile("file", "fileName4", MediaType.MULTIPART_FORM_DATA_VALUE, inputStream);
        List<MultipartFile> files = List.of(file1, file2, file3, file4);

        List<DataFile> result = dataFileService.uploadMultipleFiles(files, userId);

        assertThat(result, notNullValue());
        assertThat(result.size(), is(files.size()));
    }

    @Test
    @SneakyThrows
    @DisplayName("Удаление файла")
    void deleteFile_whenFileExists_shouldDeleteItFromDb() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "fileName", MediaType.IMAGE_JPEG_VALUE, inputStream);
        DataFile savedFile = dataFileService.uploadFile(file, userId);

        assertThat(dataFileService.findDataFileById(savedFile.getId()).getId(), is(savedFile.getId()));

        dataFileService.deleteFile(savedFile.getId());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> dataFileService.findDataFileById(savedFile.getId()));
        assertThat(ex.getMessage(), is("Файл с id '" + savedFile.getId() + "' не найден."));
    }

    @Test
    @DisplayName("Удаление файла, которого нет в бд")
    void deleteFile_whenFileNotExists_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> dataFileService.deleteFile(unknownId));
        assertThat(ex.getMessage(), is("Файл с id '" + unknownId + "' не найден."));
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение файла в виде массива байтов")
    void downloadFileBytesById_whenFileExists_shouldReturnByteArray() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "fileName", MediaType.IMAGE_JPEG_VALUE, inputStream);
        DataFile savedFile = dataFileService.uploadFile(file, userId);

        byte[] result = dataFileService.downloadFileBytesById(savedFile.getId());

        assertThat(result, notNullValue());
    }

    @Test
    @DisplayName("Получение файла в виде массива байтов, файл не существует")
    void downloadFileBytesById_whenFileNotExists_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> dataFileService.downloadFileBytesById(unknownId));
        assertThat(ex.getMessage(), is("Файл с id '" + unknownId + "' не найден."));
    }

    @Test
    @SneakyThrows
    @DisplayName("Получение файла по идентификатору")
    void findDataFileById_shouldReturnDataFile() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MockMultipartFile file = new MockMultipartFile("file", "fileName", MediaType.IMAGE_JPEG_VALUE, inputStream);
        DataFile savedFile = dataFileService.uploadFile(file, userId);

        assertThat(dataFileService.findDataFileById(savedFile.getId()).getId(), is(savedFile.getId()));
    }

    @Test
    @DisplayName("Получение файла по идентификатору, файл не существует")
    void findDataFileById_whenNoFileExists_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> dataFileService.findDataFileById(unknownId));
        assertThat(ex.getMessage(), is("Файл с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Сохранение нескольких файлов")
    @SneakyThrows
    void saveDataFiles_shouldReturnDataFilesWithNotNullId() {
        List<DataFile> savedFiles = dataFileService.saveDataFiles(List.of(dataFile, dataFile1));

        assertThat(savedFiles, notNullValue());
        assertThat(savedFiles.size(), is(2));
        assertThat(savedFiles.get(0).getId(), greaterThan(0L));
        assertThat(savedFiles.get(1).getId(), greaterThan(1L));
    }

    @Test
    @DisplayName("Удаление нескольких файлов")
    void deleteFiles_shouldDeleteFilesFromDbAndFileStorage() {
        List<DataFile> savedFiles = dataFileService.saveDataFiles(List.of(dataFile, dataFile1));

        DataFile savedFile1 = savedFiles.get(0);
        DataFile savedFile2 = savedFiles.get(1);

        dataFileService.deleteFiles(savedFiles);

        assertThrows(NotFoundException.class, () -> dataFileService.findDataFileById(savedFile1.getId()));
        assertThrows(NotFoundException.class, () -> dataFileService.findDataFileById(savedFile2.getId()));

        assertFalse(Files.exists(Paths.get(savedFile1.getFilePath())));
        assertFalse(Files.exists(Paths.get(savedFile2.getFilePath())));
    }
}