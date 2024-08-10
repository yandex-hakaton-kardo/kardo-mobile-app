package ru.yandex.kardomoblieapp.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.participation.model.ParticipantType;
import ru.yandex.kardomoblieapp.participation.model.Participation;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long>, JpaSpecificationExecutor<Participation> {

    @Query("SELECT p FROM Participation p LEFT JOIN FETCH p.event e LEFT JOIN FETCH p.user u  WHERE p.id = ?1")
    Optional<Participation> findFullParticipationById(long participationId);

    @Query("SELECT p FROM Participation p LEFT JOIN FETCH p.event e LEFT JOIN FETCH p.user u WHERE u.id = ?1")
    List<Participation> findUsersParticipations(long userId);

    @Query("SELECT p FROM Participation p LEFT JOIN FETCH p.event e LEFT JOIN FETCH p.user u WHERE u.id = ?1 AND " +
            "p.type = ?2 ORDER BY p.event.eventStart ASC")
    List<Participation> findParticipationsByUserIdAndType(long userId, ParticipantType type);

    @Query("SELECT p FROM Participation p LEFT JOIN p.event e LEFT JOIN p.user u WHERE e.id = ?1 AND u.id = ?2 AND p.type = ?3")
    Optional<Participation> findByEventIdUserIdAndParticipantType(long eventId, long userId, ParticipantType type);
}
