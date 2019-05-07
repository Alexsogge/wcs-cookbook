import { Component, OnInit } from '@angular/core';
import {CookSession, Workstep} from "../recipe";
import {ApiService} from "../api.service";
import {ActivatedRoute} from "@angular/router";
import {WebsocketService} from "../websocket.service";
import {Observable, Subscription} from "rxjs";


@Component({
  selector: 'app-cooking',
  templateUrl: './cooking.component.html',
  styleUrls: ['./cooking.component.css']
})
export class CookingComponent implements OnInit {

  messages: Observable<string[]>;
  currentMessage: string;
  private _msgSub: Subscription;

  private activeSession: CookSession;
  private sessionId;
  private currentStep: Workstep;

  ioConnection: any;

  constructor(private apiService: ApiService, private route: ActivatedRoute, private websocket: WebsocketService) { }

  ngOnInit() {
    this.sessionId = +this.route.snapshot.paramMap.get('id');
    this.apiService.getActiveSessions().subscribe(sessions => this.searchForSession(sessions));
  }

  searchForSession(sessions: CookSession[]) {
    for (const session of sessions) {
      if (session.id === this.sessionId) {
        this.activeSession = session;
        this.apiService.getWorkstep(session.currentStep).subscribe(step => this.currentStep = step);
        this.initSocket();
        break;
      }
    }
  }

  ngOnDestroy() {
    this._msgSub.unsubscribe();
  }

  initSocket() {
    this.messages = this.websocket.messages;
    this._msgSub = this.websocket.currentMessage.subscribe(msg => this.currentMessage = msg.id);
  }


}
