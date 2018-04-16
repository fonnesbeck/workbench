import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StandardSearchComponent } from './standard-search.component';

describe('StandardSearchComponent', () => {
  let component: StandardSearchComponent;
  let fixture: ComponentFixture<StandardSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StandardSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StandardSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
