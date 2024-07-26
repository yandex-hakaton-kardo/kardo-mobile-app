package ru.yandex.kardomoblieapp.post.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.datafiles.service.DataFileService;
import ru.yandex.kardomoblieapp.post.model.Post;
import ru.yandex.kardomoblieapp.post.repository.PostRepository;
import ru.yandex.kardomoblieapp.shared.exception.NotAuthorizedException;
import ru.yandex.kardomoblieapp.shared.exception.NotFoundException;
import ru.yandex.kardomoblieapp.user.model.User;
import ru.yandex.kardomoblieapp.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    private final UserService userService;

    private final DataFileService dataFileService;

    @Override
    @Transactional
    public Post createPost(long requesterId, MultipartFile file, String content) {
        final User author = userService.findUserById(requesterId);
        final Post newPost = Post.builder()
                .author(author)
                .content(content)
                .build();

        DataFile uploadedFile = dataFileService.uploadFile(file, requesterId);
        newPost.setFile(uploadedFile);
        uploadedFile.setPost(newPost);
        Post savedPost = postRepository.save(newPost);
        log.info("Пользователь с id '{}' создал пост с id '{}'.", requesterId, savedPost.getId());
        return savedPost;
    }

    @Override
    @Transactional
    public Post updatePost(long requesterId, long postId, MultipartFile file, String content) {
        userService.findUserById(requesterId);
        final Post postToUpdate = findPost(postId);
        checkIfUserIsAuthor(requesterId, postToUpdate);

        DataFile currentFile = postToUpdate.getFile();
        if (file != null && !currentFile.getFileName().equals(file.getOriginalFilename())) {
            dataFileService.deleteFile(currentFile.getId());
            DataFile newFile = dataFileService.uploadFile(file, requesterId);
            postToUpdate.setFile(newFile);
        }
        if (content != null) {
            postToUpdate.setContent(content);
        }

        postRepository.save(postToUpdate);
        log.info("Пост с id '{}' был обновлен пользователем с id '{}'.", postId, requesterId);
        return postToUpdate;
    }

    @Override
    @Transactional
    public void deletePost(long requesterId, long postId) {
        userService.findUserById(requesterId);
        final Post postToDelete = findPost(postId);
        checkIfUserIsAuthor(requesterId, postToDelete);
        DataFile file = postToDelete.getFile();
        dataFileService.deleteFile(file.getId());
        postRepository.deleteById(postId);
    }

    @Override
    public Post findPostById(long postId) {
        final Post post = findPost(postId);
        log.info("Получение поста с id '{}'.", postId);
        return post;
    }

    @Override
    public List<Post> findPostsFromUser(long userId) {
        List<Post> posts = postRepository.findPostsByAuthorId(userId);
        log.info("Получение постов пользователя с id '{}'. Количество постов: '{}'.", userId, posts.size());
        return posts;
    }

    private Post findPost(long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new NotFoundException("Пост с id '" + postId + "' не найден."));
    }

    private void checkIfUserIsAuthor(long userId, Post post) {
        if (userId != post.getAuthor().getId()) {
            throw new NotAuthorizedException("Пользователь с id '" + userId
                    + "' не имеет прав на редактирование поста с id '" + post.getId() + "'.");
        }
    }
}
