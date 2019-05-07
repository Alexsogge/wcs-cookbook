import { Injectable } from '@angular/core';
import {Observable, Observer, Subject} from 'rxjs';
import 'rxjs/add/operator/map';
import {UserService} from './user.service';


@Injectable({
  providedIn: 'root'
})
export class WebsocketService {

  private socket: WebSocket;
  public messages: Subject<string>;

  constructor(private userService: UserService) { }

  public connect(url): Subject<MessageEvent> {
    const socket = new WebSocket('ws://' + '192.168.1.102' + ':8000/ws/api/' + url + '?Authorization=' + this.userService.token);

    const observable = Observable.create(
      (obs: Observer<MessageEvent>) => {
        socket.onmessage = obs.next.bind(obs);
        socket.onerror = obs.error.bind(obs);
        socket.onclose = obs.complete.bind(obs);
        return socket.close.bind(socket);
      });
    const observer = {
      next: (data: string) => {
        if (socket.readyState === WebSocket.OPEN) {
          socket.send(data);
          if (data === 'stop') {
            socket.close(1000, 'bye');
          }
        }
      }
    };
    return Subject.create(observer, observable);
  }

  sendMSG(messgae: string) {
    this.socket.send(messgae);
  }
}
