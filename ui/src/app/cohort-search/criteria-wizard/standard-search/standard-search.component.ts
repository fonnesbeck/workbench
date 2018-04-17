import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {Observable} from 'rxjs/Observable';

import {CohortBuilderService} from 'generated';
import {CohortSearchActions} from '../../redux';


@Component({
  selector: 'crit-standard-search',
  templateUrl: './standard-search.component.html',
  styleUrls: ['./standard-search.component.css']
})
export class StandardSearchComponent implements OnInit {
  readonly domains = [
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

  get domain() { return this.search.get('domain'); }
  get value() { return this.search.get('value'); }

  // Memoize the last searched for domain and value, these are what is passed
  // down the resulting tree - NOT the current values of the form
  lastDomain = 'Condition'; // default
  lastValue = '';

  private cdrid: number;
  loading = false;
  roots = [];

  constructor(
    private route: ActivatedRoute,
    private api: CohortBuilderService,
    private actions: CohortSearchActions,
  ) {}

  ngOnInit() {
    this.cdrid = this.route.snapshot.data.workspace.cdrVersionId;
    this.domain.setValue('Condition');
    this.search.valueChanges.subscribe(console.log);
  }

  onClick() {
    this.loading = true;
    this.roots = [];
    this.lastDomain = this.domain.value;
    this.lastValue = this.value.value;

    const call = this.api.getCriteriaTreeQuickSearch(
      this.cdrid,
      this.domain.value,
      this.value.value
    );

    call.catch(err => {
      // Suppress any errors for demo purposes
      console.log('Error while getting stuff');
      console.dir(err);
      return Observable.of({items: []});
    }).subscribe(results => {
      this.roots = results.items;
      this.loading = false;
    });
  }
}
