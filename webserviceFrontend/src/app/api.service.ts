import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import {CookSession, Ingredient, Recipe, Workstep} from './recipe';

import {UserService} from './user.service';
import {Globals} from './global';


const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json'}),
  withCredentials: false
};


@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private urlPrefix = Globals.BACKEND_WEB_URL;

  private recipesUrl = this.urlPrefix + '/api/recipes/';
  private recipeUrl = this.urlPrefix + '/api/recipe';
  private ingredientUrl = this.urlPrefix + '/api/ingredient';
  private workstepsUrl = this.urlPrefix + '/api/worksteps/';
  private workstepUrl = this.urlPrefix + '/api/workstep/';
  private startSessionUrl = this.urlPrefix + '/api/startsession/';
  private getSessionsUrl = this.urlPrefix + '/api/getsessions/';

  private loginUrl = this.urlPrefix + '/api/rest-auth/login/';

  constructor(private http: HttpClient, private userService: UserService) { }


  getRecipes(): Observable<Recipe[]> {
    return this.http.get<Recipe[]>(this.recipesUrl, httpOptions).pipe(catchError(this.handleError<Recipe[]>('getRecipes', [])));
  }

  getRecipe(id: number): Observable<Recipe> {
    const url = `${this.recipeUrl}/${id}`;
    return this.http.get<Recipe>(url, httpOptions).pipe(catchError(this.handleError<Recipe>('getRecipe', null)));
  }

  getIngedient(id: number): Observable<Ingredient> {
    const url = `${this.ingredientUrl}/${id}`;
    return this.http.get<Ingredient>(url, httpOptions).pipe(catchError(this.handleError<Ingredient>('getIngedients', null)));
  }

  getWorkstepsOfRecipe(id: number): Observable<Workstep[]> {
    const url = `${this.workstepsUrl}?recipe_id=${id}`;
    return this.http.get<Workstep[]>(url, httpOptions).pipe(catchError(this.handleError<Workstep[]>('getIngedients', null)));
  }

  getWorkstep(id: number): Observable<Workstep> {
    const url = `${this.workstepUrl}${id}/`;
    return this.http.get<Workstep>(url, httpOptions).pipe(catchError(this.handleError<Workstep>('getIngedients', null)));
  }

  askForNewCookSession(recipeId: number): Observable<CookSession> {
    const url = `${this.startSessionUrl}?recipe_id=${recipeId}`;
    let params = new HttpParams();
    let options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'JWT ' + this.userService.token
      })
    };
    return this.http.get<CookSession>(url, options).pipe(catchError(this.handleError<CookSession>('startNewSession', null)));

  }

  getActiveSessions(): Observable<CookSession[]> {
    const url = `${this.getSessionsUrl}`;
    let params = new HttpParams();
    let options = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'JWT ' + this.userService.token
      })
    };
    return this.http.get<CookSession[]>(url, options).pipe(catchError(this.handleError<CookSession[]>('startNewSession', null)));
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error); // log to console instead
      return of(result as T);
    };
  }
}
