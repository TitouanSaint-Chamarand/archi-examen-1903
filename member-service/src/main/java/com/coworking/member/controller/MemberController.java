package com.coworking.member.controller;

import com.coworking.member.model.Member;
import com.coworking.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Member Management", description = "API de gestion des membres et abonnements")
public class MemberController {
    
    @Autowired
    private MemberService memberService;
    
    @PostMapping
    @Operation(summary = "Créer un nouveau membre", description = "Crée un membre avec son type d'abonnement (BASIC, PRO, ENTERPRISE)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Membre créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    public ResponseEntity<Member> createMember(@RequestBody Member member) {
        Member createdMember = memberService.create(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMember);
    }
    
    @GetMapping
    @Operation(summary = "Lister tous les membres", description = "Récupère la liste de tous les membres inscrits")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste des membres récupérée avec succès")
    })
    public ResponseEntity<List<Member>> getAllMembers() {
        List<Member> members = memberService.findAll();
        return ResponseEntity.ok(members);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un membre par ID", description = "Récupère les détails d'un membre spécifique")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Membre trouvé"),
        @ApiResponse(responseCode = "404", description = "Membre non trouvé")
    })
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{id}/suspended")
    @Operation(summary = "Vérifier si un membre est suspendu", description = "Vérifie si un membre a atteint son quota de réservations et est suspendu")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statut de suspension récupéré"),
        @ApiResponse(responseCode = "404", description = "Membre non trouvé")
    })
    public ResponseEntity<Map<String, Boolean>> isSuspended(@PathVariable Long id) {
        try {
            boolean suspended = memberService.isSuspended(id);
            return ResponseEntity.ok(Map.of("suspended", suspended));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un membre", description = "Met à jour les informations d'un membre existant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Membre mis à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Membre non trouvé")
    })
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member member) {
        try {
            Member updatedMember = memberService.update(id, member);
            return ResponseEntity.ok(updatedMember);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PatchMapping("/{id}/suspension")
    @Operation(summary = "Modifier le statut de suspension", description = "Change le statut de suspension d'un membre (utilisé par Kafka pour gérer les quotas)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Statut de suspension mis à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Membre non trouvé")
    })
    public ResponseEntity<Member> updateSuspension(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> suspensionUpdate) {
        try {
            boolean suspended = suspensionUpdate.get("suspended");
            Member updatedMember = memberService.updateSuspensionStatus(id, suspended);
            return ResponseEntity.ok(updatedMember);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un membre", description = "Supprime un membre et publie un événement Kafka pour supprimer ses réservations")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Membre supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Membre non trouvé")
    })
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        try {
            memberService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
