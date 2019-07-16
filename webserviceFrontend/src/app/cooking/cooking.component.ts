import { Component, OnInit } from '@angular/core';
import {CookSession, Recipe, Workstep} from '../recipe';
import {ApiService} from '../api.service';
import {ActivatedRoute} from '@angular/router';
import {WebsocketService} from '../websocket.service';
import {Observable, Subject, Subscription} from 'rxjs';
import {ToastrService} from "ngx-toastr";


@Component({
  selector: 'app-cooking',
  templateUrl: './cooking.component.html',
  styleUrls: ['./cooking.component.css']
})
export class CookingComponent implements OnInit {

  private activeSession: CookSession = new CookSession();
  private sessionId;
  private currentStep: Workstep;
  private currentRecipe: Recipe;

  public messages: Subject<string>;

  ioConnection: any;

  constructor(private apiService: ApiService, private route: ActivatedRoute, private websocket: WebsocketService, private toastr: ToastrService) { }

  ngOnInit() {
    this.sessionId = +this.route.snapshot.paramMap.get('id');
    this.currentStep = new Workstep();
    this.apiService.getActiveSessions().subscribe(sessions => this.searchForSession(sessions));
  }

  searchForSession(sessions: CookSession[]) {
    for (const session of sessions) {
      if (session.id === this.sessionId) {
        this.activeSession = session;
        this.apiService.getRecipe(session.recipe).subscribe(recipe => { this.currentRecipe = recipe; this.initWorkstep(); });
        this.initSocket();
        break;
      }
    }
  }

  initWorkstep() {
    this.apiService.getWorkstep(this.currentRecipe.workSteps[this.activeSession.currentStep]).subscribe(
      workstep => this.currentStep = workstep
    );
  }

  nextStep() {
    this.messages.next(JSON.stringify({message: 'next_step'}));
  }

  previousStep() {
    this.messages.next(JSON.stringify({message: 'previous_step'}));
  }

  initSocket() {
    // this.websocket.connect();
    // this.websocket.sendMSG("Test");

    this.messages = this.websocket.connect(`${this.sessionId}/`)
      .map((response: MessageEvent): string => {
        return  response.data;
      }) as Subject<string>;
    this.messages.subscribe(msg => {
        console.log('from server:' + msg);
        this.reciveMessage(msg);
      }
    );
  }

  reciveMessage(msg) {
    const msgBody = JSON.parse(msg).message;
    if (msgBody.event === 'step_update') {
      this.activeSession.currentStep = msgBody.new_step;
      this.currentStep.description = msgBody.step_desc;
    }
    if (msgBody.event === 'debug') {
      console.log('Debug: ' + msgBody.message);
      this.toastr.info(msgBody.message, 'Debug');
    }
  }


}
