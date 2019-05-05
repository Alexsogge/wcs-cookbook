import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from '@angular/router';

import {RecipesComponent} from './recipes/recipes.component';
import {HomeComponent} from './home/home.component';
import {RecipeComponent} from './recipe/recipe.component';


const routes: Routes = [
  {path: 'recipes', component: RecipesComponent},
  {path: 'home', component: HomeComponent},
  { path: 'recipe/:id', component: RecipeComponent },
];


@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [ RouterModule ]
})
export class AppRoutingModule { }
