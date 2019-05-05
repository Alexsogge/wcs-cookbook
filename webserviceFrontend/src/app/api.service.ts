import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import {CookSession, Ingredient, Recipe, Workstep} from './recipe';



const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json'}),
  withCredentials: false
};


@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private urlPrefix = 'http://localhost:8000';

  private recipesUrl = this.urlPrefix + '/api/recipes/';
  private recipeUrl = this.urlPrefix + '/api/recipe';
  private ingredientUrl = this.urlPrefix + '/api/ingredient';
  private workstepsUrl = this.urlPrefix + '/api/worksteps/';
  private startSessionUrl = this.urlPrefix + '/api/startsession/';

  private loginUrl = this.urlPrefix + '/api/rest-auth/login/';

  constructor(private http: HttpClient) { }


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

  askForNewCookSession(recipeId: number): Observable<CookSession> {
    const url = `${this.startSessionUrl}${recipeId}`;
    let params = new HttpParams();
    const options = {
      params: params,
      reportProgress: true,
      withCredentials: true,
    };
    return this.http.get<CookSession>(url, options).pipe(catchError(this.handleError<CookSession>('startNewSession', null)));

  }

  authenticate() {
    this.http.post(this.loginUrl,
      {
        "username": "alex",
        "password": "stein123"
      })
      .subscribe(
        (val) => {
          console.log("POST call successful value returned in body",
            val);
          sessionStorage.setItem("key", val["key"]);
        },
        response => {
          console.log("POST call in error", response);
        },
        () => {
          console.log("The POST observable is now completed.");
        });
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead
      return of(result as T);
    };
  }
}
