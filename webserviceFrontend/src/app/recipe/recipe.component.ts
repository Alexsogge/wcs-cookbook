import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';

import {CookSession, Ingredient, Recipe, Workstep} from '../recipe';
import {ApiService} from '../api.service';
import {Globals} from '../global';

@Component({
  selector: 'app-recipe',
  templateUrl: './recipe.component.html',
  styleUrls: ['./recipe.component.css']
})
export class RecipeComponent implements OnInit {

  columnsToDisplay = ['amount', 'name'];

  public BACKEND_WEB_URL = Globals.BACKEND_WEB_URL;

  private recipeId: number;
  private recipe: Recipe = new Recipe();
  private ingredients: Ingredient[] = [];
  private worksteps: Workstep[] = [];

  constructor(private route: ActivatedRoute, private apiService: ApiService, private router: Router) { }

  ngOnInit() {
    this.recipeId = +this.route.snapshot.paramMap.get('id');
    this.apiService.getRecipe(this.recipeId).subscribe(recipe => this.initRecipe(recipe));
  }

  initRecipe(recipe: Recipe) {
    this.recipe = recipe;
    for (const id of recipe.ingredients) {
      this.apiService.getIngedient(id).subscribe(ingredient => this.ingredients.push(ingredient));
    }
    this.apiService.getWorkstepsOfRecipe(recipe.id).subscribe(worksteps => this.worksteps = worksteps);
    console.log(this.ingredients);
  }

  public startCooking() {
    console.log("New session");
    //this.apiService.authenticate();
    this.apiService.askForNewCookSession(this.recipeId).subscribe(session => this.beginNewSession(session[0]));
  }

  beginNewSession(session: CookSession) {
    console.log(session);
    this.router.navigateByUrl('/' + 'cooking' + '/' + session.id);
  }



}
