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
import {UserService} from './user.service';
import { LoginComponent } from './login/login.component';
import { ProfileComponent } from './profile/profile.component';
import { RegisterComponent } from './register/register.component';
import { ToastrModule } from 'ngx-toastr';
import { NewrecipeComponent } from './newrecipe/newrecipe.component';
import {CookieService} from 'ngx-cookie-service';




@NgModule({
  declarations: [
    AppComponent,
    RecipesComponent,
    NavigationComponent,
    HeaderComponent,
    HomeComponent,
    RecipeComponent,
    CookingComponent,
    LoginComponent,
    ProfileComponent,
    RegisterComponent,
    NewrecipeComponent,
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
    ToastrModule.forRoot(), // ToastrModule added
  ],
  providers: [UserService, CookieService],
  bootstrap: [AppComponent],
  exports: [
    MatButtonModule,
    MatIconModule
  ]
})
export class AppModule { }
