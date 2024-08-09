package ru.yandex.kardomoblieapp.post.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;
import ru.yandex.kardomoblieapp.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostWithLike {

    private long id;

    private String title;

    private User author;

    private DataFile file;

    private long likes;

    private long views;

    private List<Comment> comments;

    private boolean likedByUser;

    public PostWithLike(Post post, boolean isPostLikedByUser) {
        this.id = post.getId();
        this.author = post.getAuthor();
        this.comments = new ArrayList<>(post.getComments());
        this.file = post.getFile();
        this.title = post.getTitle();
        this.likes = post.getLikes();
        this.views = post.getViews();
        this.likedByUser = isPostLikedByUser;
    }
}
