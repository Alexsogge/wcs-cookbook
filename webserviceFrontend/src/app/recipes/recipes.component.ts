import { Component, OnInit } from '@angular/core';
import {Recipe} from '../recipe';
import {ApiService} from '../api.service';

@Component({
  selector: 'app-recipes',
  templateUrl: './recipes.component.html',
  styleUrls: ['./recipes.component.css']
})
export class RecipesComponent implements OnInit {

  public recipes: Recipe[] = [];
  constructor(private apiService: ApiService) { }

  ngOnInit() {
    this.getRecipes();
  }

  getRecipes(): void {
    this.apiService.getRecipes().subscribe(recipes => this.recipes = recipes);
  }

}
