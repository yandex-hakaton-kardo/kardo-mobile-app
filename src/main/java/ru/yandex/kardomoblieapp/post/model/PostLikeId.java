package ru.yandex.kardomoblieapp.post.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.yandex.kardomoblieapp.user.model.User;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class PostLikeId implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
