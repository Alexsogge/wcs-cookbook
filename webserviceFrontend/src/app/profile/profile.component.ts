import { Component, OnInit } from '@angular/core';
import {UserService} from '../user.service';
import {CookSession} from '../recipe';
import {ApiService} from '../api.service';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  private activeSessions: CookSession[] = [];

  constructor(private userService: UserService, private apiService: ApiService) { }

  ngOnInit() {
    this.apiService.getActiveSessions().subscribe(sessions => this.loadSessions(sessions));
  }

  loadSessions(sessions: CookSession[]) {
    this.activeSessions = sessions;
  }
}
