import { Injectable } from '@angular/core';
import { Socket } from 'ngx-socket-io';
import {Message} from "./recipe";

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  currentMessage = this.socket.fromEvent<Message>('message');
  messages = this.socket.fromEvent<string[]>('messages');

  constructor(private socket: Socket) { }

  getDocument(id: string) {
    this.socket.emit('getmsg', id);
  }

  newDocument() {
    this.socket.emit('addmsg', { id: this.msgId(), doc: '' });
  }

  editDocument(message: Document) {
    this.socket.emit('editmsg', message);
  }

  private msgId() {
    let text = '';
    const possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

    for (let i = 0; i < 5; i++) {
      text += possible.charAt(Math.floor(Math.random() * possible.length));
    }

    return text;
  }
}
