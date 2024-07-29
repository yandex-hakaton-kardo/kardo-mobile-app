package ru.yandex.kardomoblieapp.post.service;

import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.post.dto.CommentRequest;
import ru.yandex.kardomoblieapp.post.model.Comment;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.model.PostSort;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class PostServiceImplTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    private Post post;

    private User savedUser;

    private User savedUser2;

    private String content;

    long unknownId;

    private MockMultipartFile file;

    @BeforeEach
    @SneakyThrows
    void init() {
        User user = User.builder()
                .name("Имя")
                .username("username")
                .secondName("Отчество")
                .surname("Фамилия")
                .country("Россия")
                .city("Москва")
                .email("test@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        savedUser = userService.createUser(user);
        User user2 = User.builder()
                .name("Имя 2")
                .username("username 2")
                .secondName("Отчество")
                .surname("Фамилия")
                .country("Россия")
                .city("Москва")
                .email("test2@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        savedUser2 = userService.createUser(user2);
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
        assertThat(savedPost.getTitle(), is(content));
        assertThat(savedPost.getAuthor().getId(), is(savedUser.getId()));
        assertThat(savedPost.getViews(), is(0L));
        assertThat(savedPost.getLikes(), is(0L));
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
        assertThat(updatePost.getTitle(), is(updatedContent));
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
        assertThat(updatePost.getTitle(), is(savedPost.getTitle()));
        assertThat(updatePost.getFile(), notNullValue());
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
        assertThat(result.getTitle(), is(savedPost.getTitle()));
        assertThat(result.getAuthor().getId(), is(savedPost.getAuthor().getId()));
        assertThat(result.getFile().getId(), is(savedPost.getFile().getId()));
        assertThat(result.getCreatedOn(), is(savedPost.getCreatedOn()));
        assertThat(result.getViews(), is(1L));
        assertThat(result.getLikes(), is(0L));
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

    @Test
    @DisplayName("Добавление лайка посту")
    void addLikeToPost_whenPostHaveNoLikes_shouldHaveOneLike() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        long numberOfLikes = postService.addLikeToPost(savedUser.getId(), savedPost.getId());

        assertThat(numberOfLikes, is(1L));
    }

    @Test
    @DisplayName("Повторное добавление лайка посту")
    void addLikeToPost_whenUserAlreadyLikedPost_shouldRemoveLikeFromPost() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        long firstLike = postService.addLikeToPost(savedUser.getId(), savedPost.getId());
        long secondLike = postService.addLikeToPost(savedUser.getId(), savedPost.getId());

        assertThat(firstLike, is(1L));
        assertThat(secondLike, is(0L));
    }

    @Test
    @DisplayName("Добавление лайка посту двумя пользователями")
    void addLikeToPost_whenTwoUserLikedPost_postShouldHaveTwoLikes() {
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

        long firstLike = postService.addLikeToPost(savedUser.getId(), savedPost.getId());
        long secondLike = postService.addLikeToPost(secondUser.getId(), savedPost.getId());

        assertThat(firstLike, is(1L));
        assertThat(secondLike, is(2L));
    }

    @Test
    @DisplayName("Добавление лайка посту, пользователь не найден")
    void addLikeToPost_whenUserNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.addLikeToPost(unknownId, unknownId));

        assertThat(ex.getMessage(), is("Пользователь с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Добавление лайка посту, пост не найден")
    void addLikeToPost_whenPostNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.addLikeToPost(savedUser.getId(), unknownId));

        assertThat(ex.getMessage(), is("Пост с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Получение ленты постов без просмотров")
    @SneakyThrows
    void getPostsFeed_whenNoPostViews_shouldReturnFromLatestToEarliest() {
        Post savedPost1 = postService.createPost(savedUser.getId(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getId(), file, content);

        List<Post> feed = postService.getPostsFeed(0, 10);

        assertThat(feed, notNullValue());
        assertThat(feed.size(), is(2));
        assertThat(feed.get(0).getId(), is(savedPost2.getId()));
        assertThat(feed.get(1).getId(), is(savedPost1.getId()));
    }

    @Test
    @DisplayName("Получение ленты постов без просмотров, список длинной 1")
    @SneakyThrows
    void getPostsFeed_whenSizeIs1_shouldReturnOnePost() {
        Post savedPost1 = postService.createPost(savedUser.getId(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getId(), file, content);

        List<Post> feed = postService.getPostsFeed(0, 1);

        assertThat(feed, notNullValue());
        assertThat(feed.size(), is(1));
        assertThat(feed.get(0).getId(), is(savedPost2.getId()));
    }

    @Test
    @DisplayName("Получение ленты постов без просмотров")
    @SneakyThrows
    void getPostsFeed_whenPostHaveViews_shouldPostWithViewsFirst() {
        Post savedPost1 = postService.createPost(savedUser.getId(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getId(), file, content);

        postService.findPostById(savedPost1.getId());

        List<Post> feed = postService.getPostsFeed(0, 10);

        assertThat(feed, notNullValue());
        assertThat(feed.size(), is(2));
        assertThat(feed.get(0).getId(), is(savedPost1.getId()));
        assertThat(feed.get(1).getId(), is(savedPost2.getId()));
    }

    @Test
    @DisplayName("Получение ленты постов без просмотров")
    @SneakyThrows
    void getPostsFeed_whenAllPostsHaveViews_shouldPostWithMostViewsFirst() {
        Post savedPost1 = postService.createPost(savedUser.getId(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getId(), file, content);

        postService.findPostById(savedPost1.getId());
        postService.findPostById(savedPost2.getId());
        postService.findPostById(savedPost2.getId());

        List<Post> feed = postService.getPostsFeed(0, 10);

        assertThat(feed, notNullValue());
        assertThat(feed.size(), is(2));
        assertThat(feed.get(0).getId(), is(savedPost2.getId()));
        assertThat(feed.get(1).getId(), is(savedPost1.getId()));
    }

    @Test
    @DisplayName("Добавление комментария к посту")
    void addCommentToPost() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);
        Comment comment = Comment.builder()
                .text("comment")
                .build();

        Comment savedComment = postService.addCommentToPost(savedUser.getId(), savedPost.getId(), comment);

        assertThat(savedComment, notNullValue());
        assertThat(savedComment.getId(), greaterThan(0L));
        assertThat(savedComment.getText(), is(comment.getText()));
        assertThat(savedComment.getPost().getId(), is(savedPost.getId()));
        assertThat(savedComment.getAuthor().getId(), is(savedUser.getId()));
    }

    @Test
    @DisplayName("Обновление комментария")
    void updatedComment() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Comment savedComment = postService.addCommentToPost(savedUser.getId(), savedPost.getId(), comment);
        CommentRequest commentRequest = new CommentRequest("updated comment");

        Comment updatedComment = postService.updateComment(savedUser.getId(), savedComment.getId(), commentRequest);
        Post post = postService.findPostById(savedPost.getId());

        assertThat(updatedComment, notNullValue());
        assertThat(updatedComment.getId(), is(savedComment.getId()));
        assertThat(updatedComment.getText(), is(commentRequest.getText()));
        assertThat(updatedComment.getAuthor().getId(), is(savedUser.getId()));
        assertThat(post.getComments().size(), is(1));
        assertThat(post.getComments().get(0).getId(), is(savedComment.getId()));
    }

    @Test
    @DisplayName("Удаление комментария")
    void deleteComment() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Comment savedComment = postService.addCommentToPost(savedUser.getId(), savedPost.getId(), comment);
        CommentRequest commentRequest = new CommentRequest("updated comment");

        postService.deleteComment(savedUser.getId(), savedComment.getId());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.updateComment(savedUser.getId(), savedComment.getId(), commentRequest));

        assertThat(ex.getMessage(), is("Комментарий с id '" + savedComment.getId() + "' не найден."));
    }

    @Test
    @DisplayName("Получение рекомендаций, у пользователя нет друзей.")
    void getRecommendations_whenUserHaveNoFriends_shouldDisplayAllPosts() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);

        List<Post> recommendations = postService.getRecommendations(savedUser2.getId(), 0, 10, PostSort.LIKES);

        assertThat(recommendations, notNullValue());
        assertThat(recommendations.size(), is(1));
        assertThat(recommendations.get(0).getId(), is(savedPost.getId()));
    }

    @Test
    @DisplayName("Получение рекомендаций, сортировка по количеству лайков.")
    void getRecommendations_whenPostsHaveLikes_shouldBeOrderedByNumberOfLikes() {
        Post savedPost = postService.createPost(savedUser.getId(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getId(), file, content);

        postService.addLikeToPost(savedPost2.getId(), savedPost2.getId());

        List<Post> recommendations = postService.getRecommendations(savedUser2.getId(), 0, 10, PostSort.LIKES);

        assertThat(recommendations, notNullValue());
        assertThat(recommendations.size(), is(2));
        assertThat(recommendations.get(0).getId(), is(savedPost2.getId()));
        assertThat(recommendations.get(1).getId(), is(savedPost.getId()));
    }

    @Test
    @DisplayName("Получение рекомендаций, пользователь не должен получать свои посты в рекомендации.")
    void getRecommendations_whenUserHavePosts_shouldNotShowHisPostsInRecommendations() {
        postService.createPost(savedUser.getId(), file, content);

        List<Post> recommendations = postService.getRecommendations(savedUser.getId(), 0, 10, PostSort.LIKES);

        assertThat(recommendations, notNullValue());
        assertThat(recommendations, emptyIterable());
    }
}