import {NgRedux} from '@angular-redux/store';
import {MockNgRedux} from '@angular-redux/store/testing';
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ClarityModule} from '@clr/angular';
import {fromJS, List} from 'immutable';
import {NgxPopperModule} from 'ngx-popper';

import {
  CohortSearchActions,
  CohortSearchState,
  getItem,
  parameterList,
  REOPEN_WIZARD,
} from '../redux';
import {SearchGroupItemComponent} from './search-group-item.component';

import {CohortBuilderService} from 'generated';

const baseItem = fromJS({
  id: 'item001',
  type: 'icd9',
  searchParameters: [0, 1],
  modifiers: [],
  count: null,
  isRequesting: false,
});

const zeroCrit = fromJS({
  id: 0,
  type: 'icd9',
  code: 'CodeA',
});

const oneCrit = fromJS({
  id: 1,
  type: 'icd9',
  code: 'CodeB',
});


describe('SearchGroupItemComponent', () => {
  let fixture: ComponentFixture<SearchGroupItemComponent>;
  let comp: SearchGroupItemComponent;

  let mockReduxInst;
  let itemStub;
  let codeStub;

  beforeEach(async(() => {
    mockReduxInst = MockNgRedux.getInstance();
    const old = mockReduxInst.getState;
    const wrapped = () => fromJS(old());
    mockReduxInst.getState = wrapped;

    TestBed
      .configureTestingModule({
        declarations: [SearchGroupItemComponent],
        imports: [
          ClarityModule,
          NgxPopperModule,
        ],
        providers: [
          {provide: NgRedux, useValue: mockReduxInst},
          {provide: CohortBuilderService, useValue: {}},
          CohortSearchActions,
        ],
      })
      .compileComponents();
  }));

  beforeEach(() => {
    MockNgRedux.reset();

    fixture = TestBed.createComponent(SearchGroupItemComponent);
    comp = fixture.componentInstance;

    // Default Inputs for tests
    comp.role = 'includes';
    comp.groupId = 'include0';
    comp.itemIndex = 0;

    comp.itemId = 'item001';

    itemStub = MockNgRedux
      .getSelectorStub<CohortSearchState, any>(
        getItem(comp.itemId));

    codeStub = MockNgRedux
      .getSelectorStub<CohortSearchState, List<any>>(
        parameterList(comp.itemId));

    fixture.detectChanges();
  });

  it('Should display code type', () => {
    expect(fixture.debugElement.query(By.css('small.trigger'))).toBeTruthy();
    itemStub.next(baseItem);
    codeStub.next(List([zeroCrit, oneCrit]));
    fixture.detectChanges();

    const display = fixture.debugElement.query(By.css('small.trigger')).nativeElement;
    expect(display.childElementCount).toBe(2);

    const trimmedText = display.textContent.replace(/\s+/g, ' ').trim();
    expect(trimmedText).toEqual('Contains ICD9 Codes');
  });

  it('Should properly pluralize \'Code\'', () => {
    const critList = List([zeroCrit]);
    codeStub.next(critList);
    fixture.detectChanges();
    expect(comp.pluralizedCode).toBe('Code');

    codeStub.next(critList.push(oneCrit));
    fixture.detectChanges();
    expect(comp.pluralizedCode).toBe('Codes');
  });

  it('Should dispatch REOPEN_WIZARD on edit', () => {
    /*
     * More specifically, when the edit icon is clicked, it should dispatch an
     * action like {type: REOPEN_WIZARD, item, context}
     */
    const spy = spyOn(mockReduxInst, 'dispatch');
    itemStub.next(baseItem);
    codeStub.next(List([zeroCrit, oneCrit]));
    fixture.detectChanges();

    const expectedContext = {
      criteriaType: 'icd9',
      role: 'includes',
      groupId: 'include0',
      itemId: 'item001',
    };

    const editButton = fixture.debugElement.query(By.css('clr-icon[shape=pencil]')).parent;
    editButton.triggerEventHandler('click', null);

    expect(spy).toHaveBeenCalledWith({
      type: REOPEN_WIZARD,
      item: baseItem,
      context: expectedContext,
    });
  });

  it('Should render an \'OR\' if it isn\'t the first item', () => {
    comp.itemIndex = 1;
    itemStub.next(baseItem);
    codeStub.next(List([zeroCrit, oneCrit]));
    fixture.detectChanges();

    const display = fixture.debugElement.query(By.css('small.trigger')).nativeElement;
    expect(display.childElementCount).toBe(3);

    const trimmedText = display.textContent.replace(/\s+/g, ' ').trim();
    expect(trimmedText).toEqual('OR Contains ICD9 Codes');
  });

});
