import {Component, Input, OnInit, ViewEncapsulation} from '@angular/core';
import {NgRedux} from '@angular-redux/store';
import {Observable} from 'rxjs/Observable';
import {Map} from 'immutable';
import {trigger, state, style, animate, transition} from '@angular/animations';

import {
  CohortSearchActions,
  CohortSearchState,
  activeParameterList,
  activeRole,
  activeGroupId,
  activeItem,
  isCriteriaLoading,
  criteriaLoadErrors,
  focusedCriterion,
} from '../../redux';


@Component({
  selector: 'app-criteria-wizard',
  templateUrl: './wizard.component.html',
  styleUrls: ['./wizard.component.css'],
  encapsulation: ViewEncapsulation.None,
  animations: [
    // TODO (jms) animate the thingy
  ],
})
export class WizardComponent implements OnInit {
  @Input() open: boolean;
  @Input() criteriaType: string;
  private readonly parentId = 0;  /* Root parent ID is always zero */
  private loading$: Observable<boolean>;
  private errors$: Observable<any>;
  private nodeInFocus$: Observable<Map<any, any>>;
  private settingAttributes$: Observable<boolean>;

  constructor(
    private ngRedux: NgRedux<CohortSearchState>,
    private actions: CohortSearchActions,
  ) {}

  ngOnInit() {
    const _type = <string>this.rootNode.get('type');
    const _id = <number>this.rootNode.get('id');

    this.loading$ = this.ngRedux.select(isCriteriaLoading(_type, _id));
    this.errors$ = this.ngRedux.select(criteriaLoadErrors).map(errSet =>
      errSet
        .filter((_, key) => key.first() === this.criteriaType)
        .map((val, key) => ({
          kind: key.first(),
          id: key.last(),
          error: val
        })).valueSeq().toJS()
    );
    this.nodeInFocus$ = this.ngRedux.select(focusedCriterion);
    this.settingAttributes$ = this.nodeInFocus$.map(node => !node.isEmpty());
  }

  get rootNode() {
    return Map({type: this.criteriaType, id: this.parentId});
  }

  get critPageTitle() {
    let _type = this.criteriaType;
    if (_type.match(/^DEMO.*/i)) {
      _type = 'Demographics';
    } else if (_type.match(/^(ICD|CPT).*/i)) {
      _type = _type.toUpperCase();
    }
    return `Choose ${_type} Codes`;
  }

  cancel() {
    this.actions.cancelWizard();
  }

  finish() {
    const state = this.ngRedux.getState();
    const role = activeRole(state);
    const groupId = activeGroupId(state);
    const itemId = activeItem(state).get('id');
    const selections = activeParameterList(state);
    this.actions.finishWizard();

    if (!selections.isEmpty()) {
      this.actions.requestItemCount(role, itemId);
      this.actions.requestGroupCount(role, groupId);
      this.actions.requestTotalCount(groupId);
    }
  }
}
