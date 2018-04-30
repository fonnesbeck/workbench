import {Component, Input, OnInit} from '@angular/core';

import {CohortBuilderService} from 'generated';
import {CohortSearchActions} from '../redux';

@Component({
  selector: 'crit-standard-node',
  templateUrl: './standard-node.component.html',
  styleUrls: ['./standard-node.component.css']
})
export class StandardNodeComponent {
  @Input() domain;
  @Input() value;
  @Input() cdrid;
  @Input() node;

  children = [];
  loading = false;

  constructor(
    private api: CohortBuilderService,
    private actions: CohortSearchActions,
  ) {}

  loadChildren(event) {
    if (!event) { return ; }
    this.loading = true;
    const call = this.api.getCriteriaTreeQuickSearch(
      this.cdrid,
      this.domain,
      this.value,
      this.node.conceptId
    );
    call.subscribe(results => {
      this.children = results.items;
      this.loading = false;
    });
  }
}
