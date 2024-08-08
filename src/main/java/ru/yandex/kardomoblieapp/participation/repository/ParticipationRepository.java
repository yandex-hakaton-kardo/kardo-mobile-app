package ru.yandex.kardomoblieapp.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.Participation;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    @Query("SELECT p FROM Participation p LEFT JOIN FETCH Event e LEFT JOIN FETCH User u LEFT JOIN FETCH Activity a " +
            "WHERE p.id = ?1")
    Optional<Participation> findFullParticipationById(long participationId);

    @Query("SELECT p FROM Participation p LEFT JOIN FETCH Event e LEFT JOIN FETCH User u LEFT JOIN FETCH Activity a " +
            "WHERE u.id = ?1")
    List<Participation> findUsersParticipations(long userId);

    @Query("SELECT p FROM Participation p LEFT JOIN FETCH Event e LEFT JOIN FETCH User u LEFT JOIN FETCH Activity a " +
            "WHERE u.id = ?1 AND p.type = ?2")
    List<Participation> findParticipationsByUserIdAndType(long userId, ParticipantType type);
}
