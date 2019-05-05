import {Component, EventEmitter, OnInit, Output} from '@angular/core';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent implements OnInit {

  @Output() public sidenavToggle = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  public onToggleSidenav = () => {
    this.sidenavToggle.emit();
  }

}
