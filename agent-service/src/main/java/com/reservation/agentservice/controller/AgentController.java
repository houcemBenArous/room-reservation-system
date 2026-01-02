package com.reservation.agentservice.controller;


import com.reservation.agentservice.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final ChatClient.Builder chatClientBuilder;
    private final SyncMcpToolCallbackProvider mcpTools;

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
                    - Ne invente JAMAIS de données (salles, réservations, disponibilités)
                    - Si une opération échoue, explique clairement pourquoi
                    - Sois courtois et professionnel
                    - Pour les dates, utilise le format YYYY-MM-DD
                    - Pour les heures, utilise le format HH:mm (ex: 14:30)
                    
                    **Exemples de requêtes :**
                    - "Quelles salles sont disponibles ?"
                    - "Réserve la salle A demain de 10h à 11h pour user"
                    - "Montre mes réservations"
                    - "Annule ma réservation numéro 1"
                    """)
                .defaultToolCallbacks(mcpTools)
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
