import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {forkJoin} from 'rxjs/observable/forkJoin';

import {
    CohortReviewService, PageFilterRequest,
    PageFilterType,
    ParticipantConditionsColumns,
    ParticipantDevicesColumns,
    ParticipantDrugsColumns,
    ParticipantMasterColumns,
    ParticipantMeasurementsColumns,
    ParticipantObservationsColumns,
    ParticipantProceduresColumns,
    ParticipantVisitsColumns,
    SortOrder,
} from 'generated';
import {Subscription} from 'rxjs/Subscription';

/* The most common column types */
const itemDate = {
  name: 'itemDate',
  classNames: ['date-col'],
  displayName: 'Start Date',
};
const endDate = {
    name: 'endDate',
    classNames: ['date-col'],
    displayName: 'End Date',
};
const domain = {
  name: 'domain',
  displayName: 'Domain',
};
const standardVocabulary = {
  name: 'standardVocabulary',
  classNames: ['vocab-col'],
  displayName: 'Standard Vocabulary',
};
const standardName = {
  name: 'standardName',
  displayName: 'Standard Name',
};
const sourceVocabulary = {
  name: 'sourceVocabulary',
  classNames: ['vocab-col'],
  displayName: 'Source Vocabulary',
};
const sourceName = {
  name: 'sourceName',
  displayName: 'Source Name',
};
const signature = {
    name: 'signature',
    displayName: 'Signature',
};
const sourceValue = {
  name: 'sourceValue',
  displayName: 'Source Value',
};
const ageAtEvent = {
  name: 'age',
  notField: true,
  displayName: 'Age At Event',
};


@Component({
  selector: 'app-detail-tabs',
  templateUrl: './detail-tabs.component.html',
  styleUrls: ['./detail-tabs.component.css']
})
export class DetailTabsComponent implements OnInit, OnDestroy {

  readonly stubs = [
    'physical-measurements',
    'ppi',
  ];

  readonly tabs = [{
      name: 'All Events',
      filterType: PageFilterType.ParticipantMasters,
      columns: [
          itemDate, domain, standardVocabulary, standardName, sourceVocabulary, sourceValue,
      ],
      reverseEnum: {
          itemDate: ParticipantMasterColumns.ItemDate,
          domain: ParticipantMasterColumns.Domain,
          standardVocabulary: ParticipantMasterColumns.StandardVocabulary,
          standardName: ParticipantMasterColumns.StandardName,
          sourceValue: ParticipantMasterColumns.SourceValue,
          sourceVocabulary: ParticipantMasterColumns.SourceVocabulary,
      }
  }, {
    name: 'Conditions',
    filterType: PageFilterType.ParticipantConditions,
    columns: [
      itemDate, standardVocabulary, standardName, sourceVocabulary, sourceValue, ageAtEvent,
    ],
    reverseEnum: {
      itemDate: ParticipantConditionsColumns.ItemDate,
      standardVocabulary: ParticipantConditionsColumns.StandardVocabulary,
      standardName: ParticipantConditionsColumns.StandardName,
      sourceValue: ParticipantConditionsColumns.SourceValue,
      sourceVocabulary: ParticipantConditionsColumns.SourceVocabulary,
      age: ParticipantConditionsColumns.Age,
    }
  }, {
    name: 'Procedures',
    filterType: PageFilterType.ParticipantProcedures,
    columns: [
      itemDate, standardVocabulary, standardName, sourceVocabulary, sourceValue, ageAtEvent,
    ],
    reverseEnum: {
      itemDate: ParticipantProceduresColumns.ItemDate,
      standardVocabulary: ParticipantProceduresColumns.StandardVocabulary,
      standardName: ParticipantProceduresColumns.StandardName,
      sourceValue: ParticipantProceduresColumns.SourceValue,
      sourceVocabulary: ParticipantProceduresColumns.SourceVocabulary,
      age: ParticipantProceduresColumns.Age,
    }
  }, {
    name: 'Drugs',
    filterType: PageFilterType.ParticipantDrugs,
    columns: [
      itemDate, standardVocabulary, standardName, sourceVocabulary, sourceValue,
        ageAtEvent, signature,
    ],
    reverseEnum: {
      itemDate: ParticipantDrugsColumns.ItemDate,
      standardVocabulary: ParticipantDrugsColumns.StandardVocabulary,
      standardName: ParticipantDrugsColumns.StandardName,
      sourceValue: ParticipantDrugsColumns.SourceValue,
      sourceVocabulary: ParticipantDrugsColumns.SourceVocabulary,
      age: ParticipantDrugsColumns.Age,
      signature: ParticipantDrugsColumns.Signature,
    }
  }, {
    name: 'Observations',
    filterType: PageFilterType.ParticipantObservations,
    columns: [
      itemDate, standardVocabulary, standardName, sourceVocabulary, sourceValue, ageAtEvent,
    ],
    reverseEnum: {
      itemDate: ParticipantObservationsColumns.ItemDate,
      standardVocabulary: ParticipantObservationsColumns.StandardVocabulary,
      standardName: ParticipantObservationsColumns.StandardName,
      sourceValue: ParticipantObservationsColumns.SourceValue,
      sourceVocabulary: ParticipantObservationsColumns.SourceVocabulary,
      sourceName: ParticipantObservationsColumns.SourceName,
      age: ParticipantObservationsColumns.Age,
    }
  }, {
      name: 'Visits',
      filterType: PageFilterType.ParticipantVisits,
      columns: [
          itemDate, endDate, standardVocabulary, standardName, sourceVocabulary,
          sourceValue, ageAtEvent,
      ],
      reverseEnum: {
          itemDate: ParticipantVisitsColumns.ItemDate,
          endDate: ParticipantVisitsColumns.EndDate,
          standardVocabulary: ParticipantVisitsColumns.StandardVocabulary,
          standardName: ParticipantVisitsColumns.StandardName,
          sourceValue: ParticipantVisitsColumns.SourceValue,
          sourceVocabulary: ParticipantVisitsColumns.SourceVocabulary,
          sourceName: ParticipantVisitsColumns.SourceName,
          age: ParticipantVisitsColumns.Age,
      }
  }, {
      name: 'Devices',
      filterType: PageFilterType.ParticipantDevices,
      columns: [
          itemDate, standardVocabulary, standardName, sourceVocabulary,
          sourceValue, ageAtEvent,
      ],
      reverseEnum: {
          itemDate: ParticipantDevicesColumns.ItemDate,
          standardVocabulary: ParticipantDevicesColumns.StandardVocabulary,
          standardName: ParticipantDevicesColumns.StandardName,
          sourceValue: ParticipantDevicesColumns.SourceValue,
          sourceVocabulary: ParticipantDevicesColumns.SourceVocabulary,
          sourceName: ParticipantDevicesColumns.SourceName,
          age: ParticipantDevicesColumns.Age,
      }
  }, {
      name: 'Measurements',
      filterType: PageFilterType.ParticipantMeasurements,
      columns: [
          itemDate, standardVocabulary, standardName, sourceVocabulary,
          sourceValue, ageAtEvent,
      ],
      reverseEnum: {
          itemDate: ParticipantMeasurementsColumns.ItemDate,
          standardVocabulary: ParticipantMeasurementsColumns.StandardVocabulary,
          standardName: ParticipantMeasurementsColumns.StandardName,
          sourceValue: ParticipantMeasurementsColumns.SourceValue,
          sourceVocabulary: ParticipantMeasurementsColumns.SourceVocabulary,
          sourceName: ParticipantMeasurementsColumns.SourceName,
          age: ParticipantMeasurementsColumns.Age,
      }
  }];


  tabbedData = [];
  loading = false;
  detailsLoading = false;
  subscription: Subscription;
  details;

  constructor(
    private route: ActivatedRoute,
    private reviewApi: CohortReviewService,
  ) {}

  ngOnInit() {
    this.subscription = this.route.data
      .map(({participant}) => participant)
      .withLatestFrom(
        this.route.parent.data.map(({cohort}) => cohort),
        this.route.parent.data.map(({workspace}) => workspace),
      )
      .distinctUntilChanged()
      .subscribe(([participant, cohort, workspace]) => {
        this.tabbedData = [];
        const calls = this.tabs.map(tab =>
          this.reviewApi.getParticipantData(
            workspace.namespace,
            workspace.id,
            cohort.id,
            workspace.cdrVersionId,
            participant.participantId,
              <PageFilterRequest>{
              page: 0,
              pageSize: 25,
              sortOrder: SortOrder.Asc,
              sortColumn: tab.reverseEnum[tab.columns[0].name],
              pageFilterType: tab.filterType,
            }
          )
        );

        this.loading = true;
        forkJoin(...calls).subscribe(results => {
          setTimeout(() => {
            this.tabbedData = results;
            this.loading = false;
          });
        });
      });
  }

  ngOnDestroy() {
     this.subscription.unsubscribe();
  }

  detailView(datum) {
    this.detailsLoading = true;
    const {participant} = this.route.snapshot.data;
    const {cohort, workspace} = this.route.parent.snapshot.data;
    this.reviewApi.getDetailParticipantData(
      workspace.namespace,
      workspace.id,
      cohort.id,
      workspace.cdrVersionId,
      datum.dataId,
      datum.domain
    ).subscribe(
        details => {
          this.details = details;
          this.detailsLoading = false;
        }
    );
  }
}
