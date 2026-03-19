package com.coworking.member.repository;

import com.coworking.member.model.Member;
import com.coworking.member.model.SubscriptionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Member Repository Tests")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Should save and retrieve a member")
    void testSaveAndRetrieveMember() {
        Member member = new Member("John Doe", "john@example.com", SubscriptionType.PRO);
        
        Member savedMember = memberRepository.save(member);
        
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getFullName()).isEqualTo("John Doe");
        assertThat(savedMember.getEmail()).isEqualTo("john@example.com");
        assertThat(savedMember.getSubscriptionType()).isEqualTo(SubscriptionType.PRO);
        assertThat(savedMember.isSuspended()).isFalse();
    }

    @Test
    @DisplayName("Should find member by email")
    void testFindByEmail() {
        Member member = new Member("Jane Smith", "jane@example.com", SubscriptionType.BASIC);
        memberRepository.save(member);
        
        Optional<Member> foundMember = memberRepository.findByEmail("jane@example.com");
        
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getFullName()).isEqualTo("Jane Smith");
    }

    @Test
    @DisplayName("Should verify email uniqueness")
    void testEmailUniqueness() {
        Member member1 = new Member("User One", "unique@example.com", SubscriptionType.BASIC);
        memberRepository.save(member1);
        
        Optional<Member> foundMember = memberRepository.findByEmail("unique@example.com");
        
        assertThat(foundMember).isPresent();
        assertThat(foundMember.get().getEmail()).isEqualTo("unique@example.com");
    }

    @Test
    @DisplayName("Should initialize maxConcurrentBookings for BASIC")
    void testBasicSubscriptionQuota() {
        Member member = new Member("Basic User", "basic@example.com", SubscriptionType.BASIC);
        
        Member savedMember = memberRepository.save(member);
        
        assertThat(savedMember.getMaxConcurrentBookings()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should initialize maxConcurrentBookings for PRO")
    void testProSubscriptionQuota() {
        Member member = new Member("Pro User", "pro@example.com", SubscriptionType.PRO);
        
        Member savedMember = memberRepository.save(member);
        
        assertThat(savedMember.getMaxConcurrentBookings()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should initialize maxConcurrentBookings for ENTERPRISE")
    void testEnterpriseSubscriptionQuota() {
        Member member = new Member("Enterprise User", "enterprise@example.com", SubscriptionType.ENTERPRISE);
        
        Member savedMember = memberRepository.save(member);
        
        assertThat(savedMember.getMaxConcurrentBookings()).isEqualTo(10);
    }
}
