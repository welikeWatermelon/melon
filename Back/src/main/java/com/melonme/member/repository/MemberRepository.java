package com.melonme.member.repository;

import com.melonme.member.domain.Member;
import com.melonme.member.domain.Provider;
import com.melonme.member.domain.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderId(Provider provider, String providerId);

    boolean existsByNickname(String nickname);

    Page<Member> findByRole(Role role, Pageable pageable);
}
