package ru.yandex.kardomoblieapp.post.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_likes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PostLike {

    @EmbeddedId
    private PostLikeId id;
}
