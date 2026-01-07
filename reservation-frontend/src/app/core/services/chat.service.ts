import { Injectable, signal } from '@angular/core';
import { AuthService } from './auth.service';

export interface ChatMessage {
    text: string;
    sender: 'user' | 'agent';
    timestamp: Date;
    isStreaming?: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class ChatService {
    private apiUrl = 'http://localhost:8888/api/agent';

    // Signal to hold the conversation history
    messages = signal<ChatMessage[]>([]);

    constructor(private authService: AuthService) { }

    sendMessageStream(message: string): void {
        const username = this.authService.currentUser()?.username || 'guest';
        const token = this.authService.getToken();

        // Add user message
        this.addMessage(message, 'user');

        // Add placeholder for agent response
        const agentMessageIndex = this.messages().length;
        this.messages.update(msgs => [...msgs, {
            text: '',
            sender: 'agent',
            timestamp: new Date(),
            isStreaming: true
        }]);

        // Use fetch with streaming
        fetch(`${this.apiUrl}/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ message, username })
        }).then(async response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body?.getReader();
            const decoder = new TextDecoder();
            let fullText = '';

            if (reader) {
                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    const chunk = decoder.decode(value, { stream: true });
                    fullText += chunk;

                    // Update the message in real-time
                    this.messages.update(msgs => {
                        const updated = [...msgs];
                        updated[agentMessageIndex] = {
                            ...updated[agentMessageIndex],
                            text: fullText
                        };
                        return updated;
                    });
                }

                // Mark streaming as complete
                this.messages.update(msgs => {
                    const updated = [...msgs];
                    updated[agentMessageIndex] = {
                        ...updated[agentMessageIndex],
                        isStreaming: false
                    };
                    return updated;
                });
            }
        }).catch(error => {
            console.error('Streaming error:', error);
            this.messages.update(msgs => {
                const updated = [...msgs];
                updated[agentMessageIndex] = {
                    text: 'Désolé, une erreur est survenue. Veuillez réessayer.',
                    sender: 'agent',
                    timestamp: new Date(),
                    isStreaming: false
                };
                return updated;
            });
        });
    }

    addMessage(text: string, sender: 'user' | 'agent') {
        this.messages.update(msgs => [...msgs, {
            text,
            sender,
            timestamp: new Date(),
            isStreaming: false
        }]);
    }

    clearMessages() {
        this.messages.set([]);
    }
}
