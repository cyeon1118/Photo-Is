package org.ssafy.d103.members.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ssafy.d103.members.entity.Members;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Members, Long> {

    Optional<Members> findMembersByEmailAndDeletedAtIsNull(String email);

    Optional<Members> findMembersByEmailAndPasswordAndDeletedAtIsNull(String email, String password);

    Optional<Members> findMembersByNicknameAndDeletedAtIsNull(String nickname);

    @Query(value =
            "SELECT m.member_id as memberId, m.profile_url as profileUrl, m.nickname as nickname, m.photo_cnt as photoCnt, m.following_cnt as followingCnt, m.follower_cnt as followerCnt, " +
                    "CASE " +
                        "WHEN EXISTS (SELECT 1 FROM follows f WHERE f.follower_id = :memberId AND f.following_id = m.member_id) THEN TRUE " +
                        "ELSE FALSE " +
                    "END AS is_following " +
            "FROM members m " +
            "WHERE m.nickname = :nickname ", nativeQuery = true)
    Optional<Page<Members>> findAllByMemberIdAndNickname(@Param("memberId") Long memberId, @Param("nickname") String nickname, Pageable pageable);

}
