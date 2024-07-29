package ru.yandex.kardomoblieapp.user.model;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.kardomoblieapp.datafiles.model.DataFile;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String username;

    private String name;

    @Column(name = "second_name")
    private String secondName;

    private String surname;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String email;

    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    //TODO Собрать страны и города в отдельную таблицу
    private String country;

    private String city;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_picture_id", referencedColumnName = "data_file_id")
    private DataFile profilePicture;

    private String overview;

    @Column(name = "website")
    private String website;

    private boolean isAdmin;
}
