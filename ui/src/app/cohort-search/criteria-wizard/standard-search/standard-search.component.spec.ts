import {NgRedux} from '@angular-redux/store';
import {MockNgRedux} from '@angular-redux/store/testing';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {ActivatedRoute} from '@angular/router';
import {ClarityModule} from '@clr/angular';
import {fromJS} from 'immutable';

import {CohortBuilderService} from 'generated';
import {CohortSearchActions} from '../../redux';
import {StandardSearchComponent} from '../standard-search/standard-search.component';


describe('StandardSearchComponent', () => {
  let fixture: ComponentFixture<StandardSearchComponent>;
  let component: StandardSearchComponent;
  let mockReduxInst;

  beforeEach(async(() => {
    mockReduxInst = MockNgRedux.getInstance();
    const old = mockReduxInst.getState;
    const wrapped = () => fromJS(old());
    mockReduxInst.getState = wrapped;

    TestBed
      .configureTestingModule({
        declarations: [
          StandardSearchComponent,
        ],
        imports: [
          ClarityModule,
          NoopAnimationsModule,
          ReactiveFormsModule,
        ],
        providers: [
          {provide: ActivatedRoute, useValue: {}},
          {provide: NgRedux, useValue: mockReduxInst},
          {provide: CohortBuilderService, useValue: {}},
          CohortSearchActions,
        ],
      })
      .compileComponents();
  }));

  beforeEach(() => {
    MockNgRedux.reset();

    fixture = TestBed.createComponent(StandardSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
