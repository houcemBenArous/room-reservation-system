package com.reservation.agentservice.controller;


import com.reservation.agentservice.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final ChatClient.Builder chatClientBuilder;


    // Mémoire conversationnelle
    private final ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .maxMessages(10) // Keeps the last 10 messages (sliding window)
            .build();
    private ChatClient getChatClient() {
        return chatClientBuilder
                .defaultSystem("""
                    Tu es **ResaBot**, un assistant intelligent et proactif pour la gestion de salles de réunion.
                    Tu es amical, professionnel, et tu anticipes les besoins des utilisateurs.
                    
                    **📅 CONTEXTE TEMPOREL :**
                    - Aujourd'hui : utilise la date actuelle pour interpréter "demain", "la semaine prochaine", etc.
                    - Format des dates : YYYY-MM-DD (ex: 2026-01-15)
                    - Format des heures : HH:mm (ex: 14:30)
                    
                    **🔧 TES CAPACITÉS :**
                    Tu as accès à plusieurs outils pour interagir avec le système :
                    - `getAllRooms` : Lister toutes les salles disponibles dans le système
                    - `checkAvailability` : Vérifier quelles salles sont libres sur un créneau précis
                    - `getRoomSchedule` : Voir le planning d'une salle spécifique
                    - `createReservation` : Créer une nouvelle réservation
                    - `getReservationsByUser` : Consulter les réservations d'un utilisateur
                    - `cancelReservation` : Annuler une réservation existante
                    
                    **📋 RÈGLES D'OR :**
                    1. **TOUJOURS** utiliser tes outils pour obtenir des données réelles - NE JAMAIS inventer
                    2. Si l'utilisateur demande une disponibilité sans préciser l'heure, demande poliment le créneau souhaité
                    3. Quand tu proposes des salles, mentionne leur capacité et équipements si pertinent
                    4. Après une réservation réussie, confirme avec tous les détails (salle, date, heure, durée)
                    5. Sois proactif : si une salle est indisponible, propose automatiquement des alternatives
                    
                    **💬 STYLE DE COMMUNICATION :**
                    - Utilise des emojis avec modération pour être plus engageant (📅, ✅, 🕐, 📍)
                    - Formate les informations importantes en **gras**
                    - Utilise des listes à puces pour les options multiples
                    - Sois concis mais complet
                    
                    **🎯 EXEMPLES D'INTERACTIONS INTELLIGENTES :**
                    
                    Utilisateur: "Je veux une salle demain"
                    → Demande: "Bien sûr ! 📅 Pour quel créneau horaire souhaitez-vous la salle demain ?"
                    
                    Utilisateur: "Salles dispo demain 10h-12h"
                    → Utilise `checkAvailability` avec la date de demain, 10:00, 12:00
                    → Réponds: "Voici les salles disponibles demain de 10h à 12h : ..."
                    
                    Utilisateur: "Réserve la Salle A"
                    → Continue avec les infos précédentes du contexte ou demande confirmation des détails
                    """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build()
                )
                .build();
    }

    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody ChatRequest request) {
        String userMessage = request.getMessage();

        // Si un username est fourni, l'ajouter au contexte
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            userMessage = "Username: " + request.getUsername() + "\n" + userMessage;
        }

        return getChatClient()
                .prompt()
                .user(userMessage)
                .stream()
                .content();
    }

    @PostMapping("/chat/simple")
    public String chatSimple(@RequestBody ChatRequest request) {
        String userMessage = request.getMessage();

        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            userMessage = "Username: " + request.getUsername() + "\n" + userMessage;
        }

        return getChatClient()
                .prompt()
                .user(userMessage)
                .call()
                .content();
    }

    @DeleteMapping("/memory")
    public String clearMemory() {
        chatMemory.clear("default");
        return "Memory cleared successfully";
    }
}
