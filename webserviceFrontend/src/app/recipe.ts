export class Tag {
  id: number;
  name: string;
}

export class Unit {
  id: number;
  name: string;
  short: string;
}

export class Ingredient {
  id: number;
  name: string;
  amount: string;
  unit: Unit;
}

export class Workstep {
  id: number;
  description: string;
}

export class Recipe {
  id: number;
  name: string;
  description: string;
  ingredients: number[];
  workSteps: number[];
  tags: number[];
  imageurl: string;
}

export class CookSession {
  id: number;
  recipe: number;
  recipeName: string;
  currentStep: number;
}

export class Message {
  id: string;
  text: string;
}
