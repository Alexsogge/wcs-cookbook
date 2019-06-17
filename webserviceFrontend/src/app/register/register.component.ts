import { Component, OnInit } from '@angular/core';
import {UserService} from "../user.service";
import { ToastrService } from 'ngx-toastr';
import {Router} from "@angular/router";


@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  public user: any;

  constructor(private userService: UserService, private toastr: ToastrService,  private router: Router) { }

  ngOnInit() {
    this.user = {
      username: '',
      password: ''
    };
  }

  private register() {
    this.userService.register({username: this.user.username, password: this.user.password});
    this.router.navigateByUrl('/login');
  }

}
