import { Component, OnInit } from '@angular/core';
import {Ingredient, Workstep} from "../recipe";
import {FormControl, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-newrecipe',
  templateUrl: './newrecipe.component.html',
  styleUrls: ['./newrecipe.component.css']
})
export class NewrecipeComponent implements OnInit {
  public recipeForm = new FormGroup({
    recipeName: new FormControl(''),
    description: new FormControl(''),
    ingredients: new FormGroup(FormControl[0])
  });
  public recipeName = 'New Recipe';
  public ingedients: Ingredient[];

  constructor() { }

  ngOnInit() {
  }

}
