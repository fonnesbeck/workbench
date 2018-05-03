import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {ClrDatagridStateInterface} from '@clr/angular';
import {fromJS} from 'immutable';
import {Observable} from 'rxjs/Observable';
import {Subscription} from 'rxjs/Subscription';

import {CohortReviewService, PageFilterRequest, SortOrder} from 'generated';

@Component({
  selector: 'app-detail-tab-table',
  templateUrl: './detail-tab-table.component.html',
  styleUrls: ['./detail-tab-table.component.css']
})
export class DetailTabTableComponent {
  @Input() tabname;
  @Input() columns;
  @Input() data;
  @Input() loading = true;
}
