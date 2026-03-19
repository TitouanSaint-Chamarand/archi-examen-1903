package com.coworking.member.service;

import com.coworking.member.event.MemberDeletedEvent;
import com.coworking.member.event.MemberEventPublisher;
import com.coworking.member.exception.ResourceNotFoundException;
import com.coworking.member.model.Member;
import com.coworking.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MemberService {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private MemberEventPublisher memberEventPublisher;
    
    public Member create(Member member) {
        return memberRepository.save(member);
    }
    
    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }
    
    public List<Member> findAll() {
        return memberRepository.findAll();
    }
    
    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }
    
    public Member update(Long id, Member memberDetails) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        
        member.setFullName(memberDetails.getFullName());
        member.setEmail(memberDetails.getEmail());
        member.setSubscriptionType(memberDetails.getSubscriptionType());
        
        return memberRepository.save(member);
    }
    
    public Member updateSuspensionStatus(Long id, boolean suspended) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        
        member.setSuspended(suspended);
        return memberRepository.save(member);
    }
    
    public boolean isSuspended(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return member.isSuspended();
    }
    
    public void delete(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        memberRepository.delete(member);
        memberEventPublisher.publishMemberDeleted(new MemberDeletedEvent(member.getId(), member.getEmail()));
    }
}
