package ru.yandex.kardomoblieapp.participation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.kardomoblieapp.user.model.User;

@Entity
@Table(name = "scores")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id")
    @ToString.Exclude
    private Participation participation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "judge_id")
    @ToString.Exclude
    private User judge;

    @Column(name = "score_type1")
    private Integer scoreType1;

    @Column(name = "score_type2")
    private Integer scoreType2;

    @Column(name = "score_type3")
    private Integer scoreType3;
}
