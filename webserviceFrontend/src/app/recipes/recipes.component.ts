import { Component, OnInit } from '@angular/core';
import {Recipe} from '../recipe';
import {ApiService} from '../api.service';
import {Globals} from '../global';

@Component({
  selector: 'app-recipes',
  templateUrl: './recipes.component.html',
  styleUrls: ['./recipes.component.css']
})
export class RecipesComponent implements OnInit {

  public recipes: Recipe[] = [];
  public BACKEND_WEB_URL = Globals.BACKEND_WEB_URL;
  constructor(private apiService: ApiService) { }

  ngOnInit() {
    this.getRecipes();
  }

  getRecipes(): void {
    this.apiService.getRecipes().subscribe(recipes => this.recipes = recipes);
  }

}
