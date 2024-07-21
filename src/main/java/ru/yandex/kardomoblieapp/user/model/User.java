package ru.yandex.kardomoblieapp.user.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

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
    private Long id;

    @Column(name = "username")
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

    private String avatar;

    private String overview;

    @Column(name = "website")
    private String website;

    private boolean isAdmin;
}
