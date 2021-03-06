import {Component, Input, OnChanges} from '@angular/core';
import {Observable} from 'rxjs/Observable';

import {ReviewStateService} from '../review-state.service';

import {
  CohortAnnotationDefinition,
  ParticipantCohortAnnotation,
} from 'generated';


interface Annotation {
  definition: CohortAnnotationDefinition;
  value: ParticipantCohortAnnotation;
}


/*
 * Curried predicate function that matches CohortAnnotationDefinitions to
 * ParticipantCohortAnnotations - byDefinitionId(definition)(value) => true/false.
 */
const byDefinitionId =
  ({cohortAnnotationDefinitionId}: CohortAnnotationDefinition) =>
  ({cohortAnnotationDefinitionId: annotationDefinitionId}: ParticipantCohortAnnotation): boolean =>
  (cohortAnnotationDefinitionId === annotationDefinitionId);

/*
 * Curried ParticipantCohortAnnotation factory - generates a blank value, given
 * a participant and a cohort review id
 */
const valueFactory =
  ([participantId, cohortReviewId]) =>
  (): ParticipantCohortAnnotation =>
  (<ParticipantCohortAnnotation>{participantId, cohortReviewId});

/*
 * The identity function (useful for filtering objects by truthiness)
 */
const identity = obj => obj;

@Component({
  selector: 'app-annotation-list',
  templateUrl: './annotation-list.component.html',
  styleUrls: ['./annotation-list.component.css'],
})
export class AnnotationListComponent implements OnChanges {
  @Input() participant;

  annotations$: Observable<Annotation[]>;

  /* Determines if the children should show the datatype of the annotation */
  showDataType = false;

  constructor(private state: ReviewStateService) {}

  ngOnChanges(changes) {
    const defs$ = this.state.annotationDefinitions$.filter(identity);
    const factory$ = this.state.review$.filter(identity).pluck('cohortReviewId')
      .map(rid => valueFactory([this.participant.id, rid]));

    this.annotations$ = Observable
      .combineLatest(defs$, factory$)
      .map(([defs, factoryFunc]) =>
        defs.map(definition => {
          const vals = this.participant.annotations;
          const value = vals.find(byDefinitionId(definition)) || factoryFunc();
          return <Annotation>{definition, value};
        }))
      .do(console.dir);
  }

  openManager(): void {
    this.state.annotationManagerOpen.next(true);
  }
}
