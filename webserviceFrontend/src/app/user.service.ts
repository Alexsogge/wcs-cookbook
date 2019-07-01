import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Globals} from './global';
import {ToastrService} from 'ngx-toastr';
import {CookieService} from 'ngx-cookie-service';

@Injectable()
export class UserService {

  private authUrl = Globals.BACKEND_WEB_URL + '/api/api-token-auth/';
  private registerUrl = Globals.BACKEND_WEB_URL + '/api/register/';

  // http options used for making API calls
  private httpOptions: any;

  // the actual JWT token
  public token: string;

  // the token expiration date
  public token_expires: Date;

  // the username of the logged in user
  public username: string;

  // error messages received from the login attempt
  public errors: any = [];

  constructor(private http: HttpClient, private toastr: ToastrService, private cookieService: CookieService) {
    this.httpOptions = {
      headers: new HttpHeaders({'Content-Type': 'application/json'})
    };

    if (this.cookieService.check('token')) {
      console.log("Token:" + this.token);
      this.token_expires = new Date(this.cookieService.get('token_expires'));
      const now = new Date();
      console.log(now + ' < ' + this.token_expires);
      if (now < this.token_expires) {
        console.log("Still valid token");
        this.token = this.cookieService.get('token');
        this.username = this.cookieService.get('username');
      } else {
        this.token_expires = null;
        console.log("unvalid token");
      }
    }
  }

  // Uses http.post() to get an auth token from djangorestframework-jwt endpoint
  public login(user) {
    this.http.post(this.authUrl, JSON.stringify(user), this.httpOptions).subscribe(
      data => {
        this.updateData(data['token']);
      },
      err => {
        this.errors = err['error'];
      }
    );
  }

  public register(user) {
    this.http.post(this.registerUrl, JSON.stringify(user), this.httpOptions).subscribe(
      data => {
        if ('error' in data) {
          this.errors = data['error'];
          this.toastr.error(this.errors, 'Can\'t create user');
        } else {
          this.toastr.success('Registered user');
        }
      }
    );
  }


  // Refreshes the JWT token, to extend the time the user is logged in
  public refreshToken() {
    this.http.post('/api-token-refresh/', JSON.stringify({token: this.token}), this.httpOptions).subscribe(
      data => {
        this.updateData(data['token']);
      },
      err => {
        this.errors = err['error'];
      }
    );
  }

  public logout() {
    this.token = null;
    this.token_expires = null;
    this.username = null;
  }

  private updateData(token) {
    this.token = token;
    this.errors = [];

    // decode the token to read the username and expiration timestamp
    const token_parts = this.token.split(/\./);
    const token_decoded = JSON.parse(window.atob(token_parts[1]));
    this.token_expires = new Date(token_decoded.exp * 1000);
    this.username = token_decoded.username;

    this.cookieService.set( 'token', this.token);
    this.cookieService.set( 'token_expires', this.token_expires.toString());
    this.cookieService.set( 'username', this.username);

    console.log("Loged in to:" + this.username);
  }

}
