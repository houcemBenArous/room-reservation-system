import { Component, inject, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarkdownModule } from 'ngx-markdown';
import { AuthService } from '../../core/services/auth.service';
import { ChatService } from '../../core/services/chat.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownModule],
  templateUrl: './chat.component.html'
})
export class ChatComponent implements AfterViewChecked {
  authService = inject(AuthService);
  chatService = inject(ChatService);

  @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

  userMessage = '';

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  scrollToBottom(): void {
    try {
      this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
    } catch (err) { }
  }

  sendMessage() {
    if (!this.userMessage.trim()) return;

    const msg = this.userMessage;
    this.userMessage = '';

    // Use the streaming method
    this.chatService.sendMessageStream(msg);
  }

  // Check if any message is currently streaming
  get isStreaming() {
    return this.chatService.messages().some(msg => msg.isStreaming);
  }

  // Alias for template compatibility
  get isLoading() {
    return this.isStreaming;
  }

  clearChat() {
    this.chatService.clearMessages();
  }
}
