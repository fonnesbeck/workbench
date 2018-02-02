import {select} from '@angular-redux/store';
import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {fromJS, List} from 'immutable';
import {Subscription} from 'rxjs/Subscription';

import {activeParameterList} from '../../redux';
import {AttributeFormComponent} from './attributes.interface';

import {environment} from 'environments/environment';
import {Attribute} from 'generated';


const OPERATORS = {
  'between': 'In Range',
  '=': 'Equal To',
  '>': 'Greater Than',
  '<': 'Less Than',
  '>=': 'Greater Than or Equal To',
  '<=': 'Less Than or Equal To',
};

const MAX_AGE = 120;
const MIN_AGE = 0;
const operatorIsRange = (op: string): boolean => (op === 'between');
const operatorIsValid = (op: string): boolean => (Object.keys(OPERATORS).includes(op));

const asAttribute = (ageForm: FormGroup): Attribute => {
  const operator = ageForm.get('operator').value;
  const operands = operatorIsRange(operator)
    ? [ageForm.get('rangeOpen').value, ageForm.get('rangeClose').value]
    : [ageForm.get('age').value];
  return <Attribute>{operator, operands};
};

const validate = {
  interval: (group: FormGroup): null|object => {
    const start = group.get('rangeOpen').value;
    const close = group.get('rangeClose').value;
    return start && close && start >= close
      ? {invalidRange: 'The Range Open value must precede the Close value'}
      : null;
  },
  uniqueness: (existent: List<string>) =>
    (ageForm: FormGroup): null|object => {
      const wrapped = fromJS(asAttribute(ageForm));
      const hash = wrapped.hashCode();
      const paramId = `param${hash}`;
      return existent.includes(paramId)
        ? {duplicateAttribute: 'This Criterion has already been selected'}
        : null;
  },
  age: Validators.compose([
    Validators.min(MIN_AGE),
    Validators.max(MAX_AGE),
    Validators.required,
  ]),
};

@Component({
  selector: 'crit-age-form',
  templateUrl: './age-form.component.html',
})
export class AgeFormComponent implements AttributeFormComponent, OnInit, OnDestroy {
  ageForm = new FormGroup({
    operator: new FormControl('', [
      Validators.required,
    ]),
    age: new FormControl(),
    rangeOpen: new FormControl(),
    rangeClose: new FormControl(),
  });
  @select(activeParameterList) parameterSelection$;
  private selected: List<string>;
  private subscription: Subscription;

  private readonly operators = OPERATORS;
  private readonly opCodes = Object.keys(OPERATORS);
  private readonly maxAge = MAX_AGE;
  private readonly minAge = MIN_AGE;

  @Output() attribute = new EventEmitter<Attribute | null>();

  ngOnInit() {
    this.subscription = this.parameterSelection$
      .map(params => params.map(n => n.get('parameterId')))
      .subscribe(ids => this.selected = ids);

    const [isRange, isNotRange] = this.operator.valueChanges
      .filter(operatorIsValid)
      .partition(operatorIsRange);

    this.subscription.add(isRange.subscribe(op => {
      this.age.clearValidators();
      this.age.updateValueAndValidity();

      this.rangeOpen.setValidators(validate.age);
      this.rangeOpen.updateValueAndValidity();

      this.rangeClose.setValidators(validate.age);
      this.rangeClose.updateValueAndValidity();

      this.ageForm.setValidators([
        validate.interval,
        validate.uniqueness(this.selected),
      ]);
      this.ageForm.updateValueAndValidity();
    }));

    this.subscription.add(isNotRange.subscribe(op => {
      this.rangeOpen.clearValidators();
      this.rangeOpen.updateValueAndValidity();

      this.rangeClose.clearValidators();
      this.rangeClose.updateValueAndValidity();

      this.age.setValidators(validate.age);
      this.age.updateValueAndValidity();

      this.ageForm.setValidators([
        validate.uniqueness(this.selected),
      ]);
      this.ageForm.updateValueAndValidity();
    }));
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  get operator() { return this.ageForm.get('operator'); }
  get age() { return this.ageForm.get('age'); }
  get rangeOpen() { return this.ageForm.get('rangeOpen'); }
  get rangeClose() { return this.ageForm.get('rangeClose'); }

  get operatorIsRange() { return operatorIsRange(this.operator.value); }
  get operatorIsValid() { return operatorIsValid(this.operator.value); }

  get errorList() {
    const errors = this.ageForm.errors;
    if (errors) {
      return Object.keys(errors).map(key => errors[key]);
    }
  }

  submit(): void {
    this.attribute.emit(asAttribute(this.ageForm));
  }

  cancel(): void {
    this.attribute.emit(null);
  }

  get debug() { return !!environment.debug; }
}
