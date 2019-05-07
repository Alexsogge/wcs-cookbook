import { Component, OnInit } from '@angular/core';
import {UserService} from '../user.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  public user: any;

  constructor(private userService: UserService) { }

  ngOnInit() {
    this.user = {
      username: 'alex',
      password: 'stein123'
    };
  }

  login() {
    this.userService.login({username: this.user.username, password: this.user.password});
  }

  refreshToken() {
    this.userService.refreshToken();
  }

  logout() {
    this.userService.logout();
  }

}
