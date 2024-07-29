package ru.yandex.kardomoblieapp.participant;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import ru.yandex.kardomoblieapp.event.model.Event;
import ru.yandex.kardomoblieapp.user.model.User;

@Entity
    @Table(name = "participants")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Participant {
    @Id
    @Column(name = "participant_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id", referencedColumnName = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "is_approved")
    private boolean isApproved;

    @Formula("(SELECT us.name FROM user_status us WHERE us.user_status_id = user_status_id)")
    private String status;

}
