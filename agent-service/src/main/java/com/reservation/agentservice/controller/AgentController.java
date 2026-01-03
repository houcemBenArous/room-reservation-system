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
                    Tu es un assistant intelligent pour la réservation de salles de réunion.
                    
                    **Ton rôle :**
                    - Aider les utilisateurs à réserver des salles
                    - Consulter les salles disponibles
                    - Vérifier les réservations existantes
                    - Annuler des réservations
                    
                    **Règles importantes :**
                    - Utilise TOUJOURS tes outils pour obtenir des informations réelles
                    - **POUR LA DISPONIBILITÉ :** Si l'utilisateur demande "quelles salles sont disponibles ?" sans préciser d'heure, demande TOUJOURS : "Pour quelle date et quel créneau horaire ?"
                    - Utilise l'outil `checkAvailability` pour vérifier la disponibilité réelle sur un créneau (ex: 10h-12h).
                    - **POUR LE PLANNING :** Si l'utilisateur demande "quand la salle X est-elle réservée ?", utilise l'outil `getRoomSchedule`.
                    - Ne invente JAMAIS de données.
                    - Dates : YYYY-MM-DD
                    - Heures : HH:mm
                    
                    **Exemples de requêtes :**
                    - "Quelles salles sont disponibles demain de 14h à 16h ?" -> Utilise `checkAvailability`
                    - "Quand la Salle A est-elle occupée ?" -> Utilise `getRoomSchedule`
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
