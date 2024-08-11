package ru.yandex.kardomoblieapp.post.service;

import jakarta.transaction.Transactional;
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
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.yandex.kardomoblieapp.post.dto.CommentRequest;
import ru.yandex.kardomoblieapp.post.dto.PostSearchFilter;
import ru.yandex.kardomoblieapp.post.model.Comment;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.model.PostSort;
import ru.yandex.kardomoblieapp.post.model.PostWithLike;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.kardomoblieapp.TestUtils.POSTGRES_VERSION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class PostServiceImplTest {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_VERSION);

    private Post post;

    private User savedUser;

    private User savedUser2;

    private String content;

    String unknownUsername;

    long unknownId;

    private MockMultipartFile file;

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
    @SneakyThrows
    void init() {
        User user = User.builder()
                .name("Имя")
                .username("username")
                .secondName("Отчество")
                .surname("Фамилия")
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
                .email("test2@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        savedUser2 = userService.createUser(user2);
        content = "post content";
        unknownUsername = "unknownUsername";
        unknownId = 9999L;
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        file = new MockMultipartFile("file", "fileName", MediaType.IMAGE_JPEG_VALUE, inputStream);
    }

    @Test
    @DisplayName("Создание поста с прикрепленными файлами")
    void createPost_whenFilesNotNull_shouldCreatePostWithFiles() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

        assertThat(savedPost, notNullValue());
        assertThat(savedPost.getId(), greaterThan(0L));
        assertThat(savedPost.getCreatedOn(), lessThanOrEqualTo(LocalDateTime.now()));
        assertThat(savedPost.getFile(), notNullValue());
        assertThat(savedPost.getFile().getId(), greaterThan(0L));
        assertThat(savedPost.getTitle(), is(content));
        assertThat(savedPost.getAuthor().getId(), is(savedUser.getId()));
        assertThat(savedPost.getViews(), is(0L));
        assertThat(savedPost.getLikes(), is(0L));
    }

    @Test
    @DisplayName("Создание поста, пользователь не найден")
    void createPost_whenUserNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.createPost(unknownUsername, file, content));

        assertThat(ex.getMessage(), is("Пользователь с именем '" + unknownUsername + "' не найден."));
    }

    @Test
    @DisplayName("Обновление текста поста")
    void updatePost_withoutNewFile_shouldUpdateOnlyContent() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

        String updatedContent = "updated content";
        Post updatePost = postService.updatePost(savedUser.getUsername(), savedPost.getId(), null, updatedContent);

        assertThat(updatePost, notNullValue());
        assertThat(updatePost.getId(), is(savedPost.getId()));
        assertThat(updatePost.getTitle(), is(updatedContent));
        assertThat(updatePost.getFile(), notNullValue());
        assertThat(updatePost.getFile().getId(), greaterThan(0L));
    }

    @Test
    @SneakyThrows
    @DisplayName("Обновление файла поста")
    void updatePost_whenFileNotNull_shouldReplaceOldFile() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.jpg");
        MultipartFile newFile = new MockMultipartFile("file", "new fileName", MediaType.IMAGE_JPEG_VALUE, inputStream);
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

        Post updatePost = postService.updatePost(savedUser.getUsername(), savedPost.getId(), newFile, null);

        assertThat(updatePost, notNullValue());
        assertThat(updatePost.getId(), is(savedPost.getId()));
        assertThat(updatePost.getTitle(), is(savedPost.getTitle()));
        assertThat(updatePost.getFile(), notNullValue());
    }

    @Test
    @DisplayName("Обновление поста, пользователь не найден")
    void updatePost_whenUserNotFound_shouldThrowNotFoundException() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.updatePost(unknownUsername, savedPost.getId(), file, content));

        assertThat(ex.getMessage(), is("Пользователь с именем '" + unknownUsername + "' не найден."));
    }

    @Test
    @DisplayName("Обновление поста, пост не найден")
    void updatePost_whenPostNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.updatePost(savedUser.getUsername(), unknownId, file, content));

        assertThat(ex.getMessage(), is("Пост с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Удаление поста")
    void deletePost() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

        postService.deletePost(savedUser.getUsername(), savedPost.getId());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.findPostById(savedPost.getId(), savedUser.getUsername()));
        assertThat(ex.getMessage(), is("Пост с id '" + savedPost.getId() + "' не найден."));
    }

    @Test
    @DisplayName("Удаление поста, пост не найден")
    void deletePost_whenPostNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.deletePost(savedUser.getUsername(), unknownId));

        assertThat(ex.getMessage(), is("Пост с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Удаление поста, пользователь не найден")
    void deletePost_whenUserNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.deletePost(unknownUsername, unknownId));

        assertThat(ex.getMessage(), is("Пользователь с именем '" + unknownUsername + "' не найден."));
    }

    @Test
    @DisplayName("Получение поста по id")
    void findPostById() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

        PostWithLike result = postService.findPostById(savedPost.getId(), savedUser.getUsername());

        assertThat(result, notNullValue());
        assertThat(result.getId(), is(savedPost.getId()));
        assertThat(result.getTitle(), is(savedPost.getTitle()));
        assertThat(result.getAuthor().getId(), is(savedPost.getAuthor().getId()));
        assertThat(result.getFile().getId(), is(savedPost.getFile().getId()));
        assertThat(result.getViews(), is(1L));
        assertThat(result.getLikes(), is(0L));
        assertFalse(result.isLikedByUser());
    }

    @Test
    @DisplayName("Получение поста по id, пост не найден")
    void findPostById_whenPostNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.findPostById(unknownId, savedUser.getUsername()));

        assertThat(ex.getMessage(), is("Пост с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Получение списка постов пользователя, у пользователя один пост")
    void findPostsFromUser_whenUserHaveOnePost_shouldReturnOnlyOnePost() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

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
        postService.createPost(savedUser.getUsername(), file, content);
        postService.createPost(savedUser.getUsername(), file, content);
        postService.createPost(savedUser.getUsername(), file, content);
        postService.createPost(savedUser.getUsername(), file, content);

        List<Post> postsFromUser = postService.findPostsFromUser(savedUser.getId());

        assertThat(postsFromUser, notNullValue());
        assertThat(postsFromUser.size(), is(4));
    }

    @Test
    @DisplayName("Добавление лайка посту")
    void addLikeToPost_whenPostHaveNoLikes_shouldHaveOneLike() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

        long numberOfLikes = postService.addLikeToPost(savedUser.getUsername(), savedPost.getId());

        assertThat(numberOfLikes, is(1L));
    }

    @Test
    @DisplayName("Повторное добавление лайка посту")
    void addLikeToPost_whenUserAlreadyLikedPost_shouldRemoveLikeFromPost() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

        long firstLike = postService.addLikeToPost(savedUser.getUsername(), savedPost.getId());
        long secondLike = postService.addLikeToPost(savedUser.getUsername(), savedPost.getId());

        assertThat(firstLike, is(1L));
        assertThat(secondLike, is(0L));
    }

    @Test
    @DisplayName("Добавление лайка посту двумя пользователями")
    void addLikeToPost_whenTwoUserLikedPost_postShouldHaveTwoLikes() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        User user2 = User.builder().name("Имя")
                .username("username2")
                .secondName("Отчество")
                .surname("Фамилия")
                .email("newuser@mail.ru")
                .password("password")
                .dateOfBirth(LocalDate.of(1990, 12, 12))
                .build();
        User secondUser = userService.createUser(user2);

        long firstLike = postService.addLikeToPost(savedUser.getUsername(), savedPost.getId());
        long secondLike = postService.addLikeToPost(secondUser.getUsername(), savedPost.getId());

        assertThat(firstLike, is(1L));
        assertThat(secondLike, is(2L));

        PostWithLike post = postService.findPostById(savedPost.getId(), savedUser.getUsername());

        assertTrue(post.isLikedByUser());

    }

    @Test
    @DisplayName("Добавление лайка посту, пользователь не найден")
    void addLikeToPost_whenUserNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.addLikeToPost(unknownUsername, unknownId));

        assertThat(ex.getMessage(), is("Пользователь с именем '" + unknownUsername + "' не найден."));
    }

    @Test
    @DisplayName("Добавление лайка посту, пост не найден")
    void addLikeToPost_whenPostNotFound_shouldThrowNotFoundException() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.addLikeToPost(savedUser.getUsername(), unknownId));

        assertThat(ex.getMessage(), is("Пост с id '" + unknownId + "' не найден."));
    }

    @Test
    @DisplayName("Получение ленты постов без просмотров")
    @SneakyThrows
    void getPostsFeed_whenNoPostViews_shouldReturnFromLatestToEarliest() {
        Post savedPost1 = postService.createPost(savedUser.getUsername(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getUsername(), file, content);

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
        Post savedPost1 = postService.createPost(savedUser.getUsername(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getUsername(), file, content);

        List<Post> feed = postService.getPostsFeed(0, 1);

        assertThat(feed, notNullValue());
        assertThat(feed.size(), is(1));
        assertThat(feed.get(0).getId(), is(savedPost2.getId()));
    }

    @Test
    @DisplayName("Получение ленты постов без просмотров")
    @SneakyThrows
    void getPostsFeed_whenPostHaveViews_shouldPostWithViewsFirst() {
        Post savedPost1 = postService.createPost(savedUser.getUsername(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getUsername(), file, content);

        postService.findPostById(savedPost1.getId(), savedUser.getUsername());

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
        Post savedPost1 = postService.createPost(savedUser.getUsername(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getUsername(), file, content);

        postService.findPostById(savedPost1.getId(), savedUser.getUsername());
        postService.findPostById(savedPost2.getId(), savedUser.getUsername());
        postService.findPostById(savedPost2.getId(), savedUser.getUsername());

        List<Post> feed = postService.getPostsFeed(0, 10);

        assertThat(feed, notNullValue());
        assertThat(feed.size(), is(2));
        assertThat(feed.get(0).getId(), is(savedPost2.getId()));
        assertThat(feed.get(1).getId(), is(savedPost1.getId()));
    }

    @Test
    @DisplayName("Добавление комментария к посту")
    void addCommentToPost() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        Comment comment = Comment.builder()
                .text("comment")
                .build();

        Comment savedComment = postService.addCommentToPost(savedUser.getUsername(), savedPost.getId(), comment);

        assertThat(savedComment, notNullValue());
        assertThat(savedComment.getId(), greaterThan(0L));
        assertThat(savedComment.getText(), is(comment.getText()));
        assertThat(savedComment.getPost().getId(), is(savedPost.getId()));
        assertThat(savedComment.getAuthor().getId(), is(savedUser.getId()));
    }

    @Test
    @DisplayName("Обновление комментария")
    void updatedComment() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Comment savedComment = postService.addCommentToPost(savedUser.getUsername(), savedPost.getId(), comment);
        CommentRequest commentRequest = new CommentRequest("updated comment");

        Comment updatedComment = postService.updateComment(savedUser.getUsername(), savedComment.getId(), commentRequest);
        PostWithLike post = postService.findPostById(savedPost.getId(), savedUser.getUsername());

        assertThat(updatedComment, notNullValue());
        assertThat(updatedComment.getId(), is(savedComment.getId()));
        assertThat(updatedComment.getText(), is(commentRequest.getText()));
        assertThat(updatedComment.getAuthor().getId(), is(savedUser.getId()));
        assertThat(post.getComments().size(), is(1));
        assertThat(post.getComments().get(0).getId(), is(savedComment.getId()));
    }

    @Test
    @DisplayName("Попытка обновить комментарий не автором")
    void updatedComment_whenNotAuthorTriesToUpdate_shouldThrowNotAuthorizedException() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Comment savedComment = postService.addCommentToPost(savedUser.getUsername(), savedPost.getId(), comment);
        CommentRequest commentRequest = new CommentRequest("updated comment");

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> postService.updateComment(savedUser2.getUsername(), savedComment.getId(), commentRequest));

        assertThat(ex.getMessage(), is("Пользователь с именем '" + savedUser2.getUsername()
                + "' не имеет прав на редактирование комментария с id '" + comment.getId() + "'."));
    }

    @Test
    @DisplayName("Удаление комментария")
    void deleteComment() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Comment savedComment = postService.addCommentToPost(savedUser.getUsername(), savedPost.getId(), comment);
        CommentRequest commentRequest = new CommentRequest("updated comment");

        postService.deleteComment(savedUser.getUsername(), savedComment.getId());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> postService.updateComment(savedUser.getUsername(), savedComment.getId(), commentRequest));

        assertThat(ex.getMessage(), is("Комментарий с id '" + savedComment.getId() + "' не найден."));
    }

    @Test
    @DisplayName("Попытка удалить комментарий не автором")
    void deleteComment_whenNotAuthorTriesToDelete_shouldThrowNotAuthorizedException() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        Comment comment = Comment.builder()
                .text("comment")
                .build();
        Comment savedComment = postService.addCommentToPost(savedUser.getUsername(), savedPost.getId(), comment);

        NotAuthorizedException ex = assertThrows(NotAuthorizedException.class,
                () -> postService.deleteComment(savedUser2.getUsername(), savedComment.getId()));

        assertThat(ex.getMessage(), is("Пользователь с именем '" + savedUser2.getUsername()
                + "' не имеет прав на редактирование комментария с id '" + comment.getId() + "'."));
    }

    @Test
    @DisplayName("Получение рекомендаций, у пользователя нет друзей.")
    void getRecommendations_whenUserHaveNoFriends_shouldDisplayAllPosts() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);

        List<Post> recommendations = postService.getRecommendations(savedUser2.getUsername(), 0, 10, PostSort.LIKES);

        assertThat(recommendations, notNullValue());
        assertThat(recommendations.size(), is(1));
        assertThat(recommendations.get(0).getId(), is(savedPost.getId()));
    }

    @Test
    @DisplayName("Получение рекомендаций, сортировка по количеству лайков.")
    void getRecommendations_whenPostsHaveLikes_shouldBeOrderedByNumberOfLikes() {
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        Post savedPost2 = postService.createPost(savedUser.getUsername(), file, content);

        postService.addLikeToPost(savedUser2.getUsername(), savedPost2.getId());

        List<Post> recommendations = postService.getRecommendations(savedUser2.getUsername(), 0, 10, PostSort.LIKES);

        assertThat(recommendations, notNullValue());
        assertThat(recommendations.size(), is(2));
        assertThat(recommendations.get(0).getId(), is(savedPost2.getId()));
        assertThat(recommendations.get(1).getId(), is(savedPost.getId()));
    }

    @Test
    @DisplayName("Получение рекомендаций, пользователь не должен получать свои посты в рекомендации.")
    void getRecommendations_whenUserHavePosts_shouldNotShowHisPostsInRecommendations() {
        postService.createPost(savedUser.getUsername(), file, content);

        List<Post> recommendations = postService.getRecommendations(savedUser.getUsername(), 0, 10, PostSort.LIKES);

        assertThat(recommendations, notNullValue());
        assertThat(recommendations, emptyIterable());
    }

    @Test
    @DisplayName("Поиск постов, title null")
    void searchPosts_whenTitleIsNull_shouldReturnAllPosts() {
        PostSearchFilter searchFilter = new PostSearchFilter(null);
        postService.createPost(savedUser.getUsername(), file, content);
        postService.createPost(savedUser.getUsername(), file, content);

        List<Post> posts = postService.searchPosts(searchFilter, 0, 10);

        assertThat(posts, notNullValue());
        assertThat(posts.size(), is(2));
    }

    @Test
    @DisplayName("Поиск постов")
    void searchPosts_whenTitleIsNotNull_shouldReturnPost() {
        PostSearchFilter searchFilter = new PostSearchFilter("PosT");
        Post savedPost = postService.createPost(savedUser.getUsername(), file, content);
        postService.createPost(savedUser.getUsername(), file, "new content");

        List<Post> posts = postService.searchPosts(searchFilter, 0, 10);

        assertThat(posts, notNullValue());
        assertThat(posts.size(), is(1));
        assertThat(posts.get(0).getId(), is(savedPost.getId()));
    }
}