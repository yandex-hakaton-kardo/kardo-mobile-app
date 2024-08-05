package ru.yandex.kardomoblieapp.user.repository;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.yandex.kardomoblieapp.user.model.User;

@UtilityClass
public class UserSpecification {

    public static Specification<User> textInUsernameOrEmail(String text) {
        if (text == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")),
                                "%" + text.toLowerCase() + "%"),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                                "%" + text.toLowerCase() + "%")
                );
    }
}
