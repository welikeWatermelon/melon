package com.melonme.member.domain;

import com.melonme.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "therapy_area")
    private String therapyArea;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public Member(Provider provider, String providerId, String nickname, String therapyArea, Role role) {
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
        this.therapyArea = therapyArea;
        this.role = role != null ? role : Role.PENDING;
    }

    public void updateProfile(String nickname, String therapyArea) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (therapyArea != null) {
            this.therapyArea = therapyArea;
        }
    }

    public void updateRole(Role role) {
        this.role = role;
    }
}
