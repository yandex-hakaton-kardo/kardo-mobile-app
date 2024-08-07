package ru.yandex.kardomoblieapp.post.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.yandex.kardomoblieapp.post.model.Post;

@UtilityClass
public class PostSpecification {

    public static Specification<Post> textInPostTitle(String text) {
        if (text == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + text.toLowerCase() + "%");
    }
}
