import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';

import {CohortBuilderService} from 'generated';
import {CohortSearchActions} from '../../redux';


@Component({
  selector: 'crit-standard-search',
  templateUrl: './standard-search.component.html',
  styleUrls: ['./standard-search.component.css']
})
export class StandardSearchComponent implements OnInit {
  domains = [
    'Condition',
    'Observation',
    'Measurement',
    'Procedure',
    'Drug',
  ];

  search = new FormGroup({
    domain: new FormControl(),
    value: new FormControl(),
  });

  constructor(
    private route: ActivatedRoute,
    private api: CohortBuilderService,
    private actions: CohortSearchActions,
  ) {}

  ngOnInit() {
    this.search.valueChanges.subscribe(console.log);
  }

  /* Copied from demo-form */
  onCancel() {
    this.actions.cancelWizard();
  }

  onSubmit() {
    this.actions.finishWizard();
  }

  openChange(value: boolean) {
    if (!value) {
      this.onCancel();
    }
  }

}
