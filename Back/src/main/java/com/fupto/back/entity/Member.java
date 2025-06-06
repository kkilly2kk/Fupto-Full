package com.fupto.back.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "password", nullable = false, length = 200)
    private String password;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "tel", length = 11)
    private String tel;

    @Column(name = "email", nullable = false, length = 256)
    private String email;

    @ColumnDefault("current_timestamp()")
    @Column(name = "create_date", insertable = false, updatable = false)
    private Instant createDate;

    @ColumnDefault("current_timestamp()")
    @LastModifiedDate
    @Column(name = "update_date", insertable = false)
    private Instant updateDate;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "role", nullable = false, length = 10)
    private String role;

    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    @Builder.Default
    @Column(name = "state")
    private Boolean state = true;

    @Column(name = "login_date")
    private Instant loginDate;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "profile_img")
    private String profileImg;

    @ToString.Exclude
    @OneToMany(mappedBy = "regMember", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Board> boards;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Favorite> favorites;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Alert> alert;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private RefreshToken refreshTokens;

    @Column(name = "provider", length = 10)
    private String provider;


    public Member update(String name) {
        this.nickname = name;
        return this;
    }
}