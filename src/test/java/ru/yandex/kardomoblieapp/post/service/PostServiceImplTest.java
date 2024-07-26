package ru.yandex.kardomoblieapp.post.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PostServiceImplTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    private Post post;

    private User savedUser;

    private String content;

    long unknownId;

    private MockMultipartFile file;

    @BeforeEach
    @SneakyThrows
    void init() {
        User user = User.builder().name("Имя")
                .secondName("Отчество")
                .surname("Фамилия")
                .country("Россия")
                .city("Москва")
                .email("test@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        savedUser = userService.createUser(user);
        content = "post content";
        unknownId = 9999L;
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        file = new MockMultipartFile("file", "fileName", MediaType.IMAGE_JPEG_VALUE, inputStream);
    }

    @Test
    @DisplayName("Создание поста с прикрепленными файлами")
    void createPost_whenFilesNotNull_shouldCreatePostWithFiles() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        assertThat(savedPost, notNullValue());
        assertThat(savedPost.getId(), greaterThan(0L));
        assertThat(savedPost.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(savedPost.getFile(), notNullValue());
        assertThat(savedPost.getFile().getId(), greaterThan(0L));
        assertThat(savedPost.getFile().getFileName(), is(file.getOriginalFilename()));
        assertThat(savedPost.getContent(), is(content));
        assertThat(savedPost.getAuthor().getId(), is(savedUser.getId()));
    }

    @Test
    @DisplayName("Создание поста, пользователь не найден")
    void createPost_whenUserNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.createPost(unknownId, file, content));

        assertThat(ex.getMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Обновление текста поста")
    void updatePost_withoutNewFile_shouldUpdateOnlyContent() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        String updatedContent = "updated content";
        Post updatePost = postService.updatePost(savedUser.getId(), savedPost.getId(), null, updatedContent);

        assertThat(updatePost, notNullValue());
        assertThat(updatePost.getId(), is(savedPost.getId()));
        assertThat(updatePost.getContent(), is(updatedContent));
        assertThat(updatePost.getFile(), notNullValue());
        assertThat(updatePost.getFile().getId(), greaterThan(0L));
        assertThat(updatePost.getFile().getFileName(), is(file.getOriginalFilename()));
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление файла поста")
    void updatePost_whenFileNotNull_shouldReplaceOldFile() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MultipartFile newFile = new MockMultipartFile("file", "new fileName", MediaType.IMAGE_JPEG_VALUE, inputStream);
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        Post updatePost = postService.updatePost(savedUser.getId(), savedPost.getId(), newFile, null);

        assertThat(updatePost, notNullValue());
        assertThat(updatePost.getId(), is(savedPost.getId()));
        assertThat(updatePost.getContent(), is(savedPost.getContent()));
        assertThat(updatePost.getFile(), notNullValue());
        assertThat(updatePost.getFile().getId(), not(savedPost.getFile().getId()));
        assertThat(updatePost.getFile().getFileName(), is(newFile.getOriginalFilename()));
    }

    @Test
    @DisplayName("Обновление поста, пользователь не имеет прав на редактирование")
    void updatePost_whenUserNotAuthorized_shouldThrowNotAuthorisedException() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);
        User user = User.builder().name("Имя")
                .secondName("Отчество")
                .surname("Фамилия")
                .country("Россия")
                .city("Москва")
                .email("test@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        User secondUser = userService.createUser(user);

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> postService.updatePost(secondUser.getId(), savedPost.getId(), null, null));
        assertThat(ex.getMessage(), is("Пользователь с id '" + secondUser.getId()
                + "' не имеет прав на редактирование поста с id '" + savedPost.getId() + "'."));
    }

    @Test
    @DisplayName("Обновление поста, пользователь не найден")
    void updatePost_whenUserNotFound_shouldThrowNotFoundException() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.updatePost(unknownId, savedPost.getId(), file, content));

        assertThat(ex.getMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Обновление поста, пост не найден")
    void updatePost_whenPostNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.updatePost(savedUser.getId(), unknownId, file, content));

        assertThat(ex.getMessage(), is("Пост с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Удаление поста")
    void deletePost() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        postService.deletePost(savedUser.getId(), savedPost.getId());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.findPostById(savedPost.getId()));
        assertThat(ex.getMessage(), is("Пост с id '" + savedPost.getId() + "' не найден."));
    }

    @Test
    @DisplayName("Удаление поста, пост не найден")
    void deletePost_whenPostNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.deletePost(savedUser.getId(), unknownId));

        assertThat(ex.getMessage(), is("Пост с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Удаление поста, пользователь не найден")
    void deletePost_whenUserNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.deletePost(unknownId, unknownId));

        assertThat(ex.getMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Удаление поста, пользователь не найден")
    void deletePost_whenUserNotAuthorized_shouldThrowNotAuthorizedException() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);
        User user = User.builder().name("Имя")
                .secondName("Отчество")
                .surname("Фамилия")
                .country("Россия")
                .city("Москва")
                .email("test@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        User secondUser = userService.createUser(user);

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> postService.deletePost(secondUser.getId(), savedPost.getId()));

        assertThat(ex.getMessage(), is("Пользователь с id '" + secondUser.getId()
                + "' не имеет прав на редактирование поста с id '" + savedPost.getId() + "'."));
    }

    @Test
    @DisplayName("Получение поста по id")
    void findPostById() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        Post result = postService.findPostById(savedPost.getId());

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(savedPost.getId()));
        assertThat(result.getContent(), is(savedPost.getContent()));
        assertThat(result.getAuthor().getId(), is(savedPost.getAuthor().getId()));
        assertThat(result.getFile().getId(), is(savedPost.getFile().getId()));
        assertThat(result.getCreatedOn(), is(savedPost.getCreatedOn()));
    }

    @Test
    @DisplayName("Получение поста по id, пост не найден")
    void findPostById_whenPostNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.findPostById(unknownId));

        assertThat(ex.getMessage(), is("Пост с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Получение списка постов пользователя, у пользователя один пост")
    void findPostsFromUser_whenUserHaveOnePost_shouldReturnOnlyOnePost() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        List<Post> postsFromUser = postService.findPostsFromUser(savedUser.getId());

        assertThat(postsFromUser, notNullValue());
        assertThat(postsFromUser.size(), is(1));
        assertThat(postsFromUser.get(0).getId(), is(savedPost.getId()));
    }

    @Test
    @DisplayName("Получение списка постов пользователя, у пользователя нет постов")
    void findPostsFromUser_whenUserHaveNoPost_shouldReturnEmptyList() {
        List<Post> postsFromUser = postService.findPostsFromUser(savedUser.getId());

        assertThat(postsFromUser, notNullValue());
        assertThat(postsFromUser, emptyIterable());
    }

    @Test
    @DisplayName("Получение списка постов пользователя, у пользователя несколько постов")
    void findPostsFromUser_whenUserHaveMultiplePost_shouldReturnList() {
        postService.createPost(savedUser.getId(), file, content);
        postService.createPost(savedUser.getId(), file, content);
        postService.createPost(savedUser.getId(), file, content);
        postService.createPost(savedUser.getId(), file, content);

        List<Post> postsFromUser = postService.findPostsFromUser(savedUser.getId());

        assertThat(postsFromUser, notNullValue());
        assertThat(postsFromUser.size(), is(4));
    }
}