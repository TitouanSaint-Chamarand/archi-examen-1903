package com.coworking.member.service;

import com.coworking.member.exception.ResourceNotFoundException;
import com.coworking.member.model.Member;
import com.coworking.member.model.SubscriptionType;
import com.coworking.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Member Service Tests")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = new Member("John Doe", "john@example.com", SubscriptionType.PRO);
        testMember.setId(1L);
    }

    @Test
    @DisplayName("Should create a BASIC member with quota 2")
    void testCreateBasicMember() {
        Member basicMember = new Member("Basic User", "basic@example.com", SubscriptionType.BASIC);
        basicMember.setId(1L);
        
        when(memberRepository.save(any(Member.class))).thenReturn(basicMember);

        Member createdMember = memberService.create(basicMember);

        assertThat(createdMember).isNotNull();
        assertThat(createdMember.getMaxConcurrentBookings()).isEqualTo(2);
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should create a PRO member with quota 5")
    void testCreateProMember() {
        Member proMember = new Member("Pro User", "pro@example.com", SubscriptionType.PRO);
        proMember.setId(1L);
        
        when(memberRepository.save(any(Member.class))).thenReturn(proMember);

        Member createdMember = memberService.create(proMember);

        assertThat(createdMember).isNotNull();
        assertThat(createdMember.getMaxConcurrentBookings()).isEqualTo(5);
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should create an ENTERPRISE member with quota 10")
    void testCreateEnterpriseMember() {
        Member enterpriseMember = new Member("Enterprise User", "enterprise@example.com", SubscriptionType.ENTERPRISE);
        enterpriseMember.setId(1L);
        
        when(memberRepository.save(any(Member.class))).thenReturn(enterpriseMember);

        Member createdMember = memberService.create(enterpriseMember);

        assertThat(createdMember).isNotNull();
        assertThat(createdMember.getMaxConcurrentBookings()).isEqualTo(10);
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should update a member")
    void testUpdateMember() {
        Member updatedDetails = new Member("John Updated", "john.updated@example.com", SubscriptionType.ENTERPRISE);
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member updatedMember = memberService.update(1L, updatedDetails);

        assertThat(updatedMember).isNotNull();
        verify(memberRepository, times(1)).findById(1L);
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should delete a member")
    void testDeleteMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        doNothing().when(memberRepository).delete(any(Member.class));

        memberService.delete(1L);

        verify(memberRepository, times(1)).findById(1L);
        verify(memberRepository, times(1)).delete(testMember);
    }

    @Test
    @DisplayName("Should check if member is suspended")
    void testIsSuspended() {
        testMember.setSuspended(true);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        boolean isSuspended = memberService.isSuspended(1L);

        assertThat(isSuspended).isTrue();
        verify(memberRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should update suspension status")
    void testUpdateSuspensionStatus() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenReturn(testMember);

        Member updatedMember = memberService.updateSuspensionStatus(1L, true);

        assertThat(updatedMember).isNotNull();
        verify(memberRepository, times(1)).findById(1L);
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("Should throw exception when member not found")
    void testMemberNotFound() {
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.isSuspended(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Member not found with id: 999");
    }
}
