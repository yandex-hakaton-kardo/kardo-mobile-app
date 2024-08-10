package ru.yandex.kardomoblieapp.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.kardomoblieapp.participation.model.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {

    @Query(value = "SELECT (AVG(s.score_type1 + s.score_type2 + s.score_type3) / 3) FROM scores s " +
            "WHERE s.participation_id = ?1 GROUP BY s.participation_id", nativeQuery = true)
    double findAvgRatingOfParticipation(long participationId);
}
