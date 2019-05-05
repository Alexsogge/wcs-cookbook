import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';

import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule, MatCheckboxModule, MatListModule, MatTableModule} from '@angular/material';
import { RecipesComponent } from './recipes/recipes.component';
import { AppRoutingModule } from './app-routing.module';
import {MatGridListModule, MatIconModule, MatCardModule} from '@angular/material';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { FlexLayoutModule } from '@angular/flex-layout';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatInputModule} from '@angular/material/input';


import { NavigationComponent } from './navigation/navigation.component';
import { HeaderComponent } from './header/header.component';
import { HomeComponent } from './home/home.component';
import { RecipeComponent } from './recipe/recipe.component';
import { CookingComponent } from './cooking/cooking.component';

@NgModule({
  declarations: [
    AppComponent,
    RecipesComponent,
    NavigationComponent,
    HeaderComponent,
    HomeComponent,
    RecipeComponent,
    CookingComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MatButtonModule,
    MatCheckboxModule,
    AppRoutingModule,
    HttpClientModule,
    MatGridListModule,
    FormsModule,
    ReactiveFormsModule,
    FlexLayoutModule,
    MatToolbarModule,
    MatIconModule,
    MatCardModule,
    MatInputModule,
    MatListModule,
    MatTableModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
  exports: [
    MatButtonModule,
    MatIconModule
  ]
})
export class AppModule { }
