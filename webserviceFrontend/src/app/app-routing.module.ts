import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {RouterModule, Routes} from '@angular/router';

import {RecipesComponent} from './recipes/recipes.component';
import {HomeComponent} from './home/home.component';
import {RecipeComponent} from './recipe/recipe.component';
import {LoginComponent} from './login/login.component';
import {CookingComponent} from './cooking/cooking.component';
import {ProfileComponent} from './profile/profile.component';
import {RegisterComponent} from "./register/register.component";


const routes: Routes = [
  {path: 'recipes', component: RecipesComponent},
  {path: 'home', component: HomeComponent},
  { path: 'recipe/:id', component: RecipeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'cooking/:id', component: CookingComponent },
  { path: 'profile', component: ProfileComponent },
  { path: 'register', component: RegisterComponent },
];


@NgModule({
  declarations: [],
  imports: [
    RouterModule.forRoot(routes)
  ],
  exports: [ RouterModule ]
})
export class AppRoutingModule { }
