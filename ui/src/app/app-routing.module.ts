import {NgModule} from '@angular/core';
import {NavigationEnd, Router, RouterModule, Routes} from '@angular/router';

import {HomeComponent} from './data-browser/home/home.component';
import {SearchComponent} from './data-browser/search/search.component';

import {SignInGuard} from './guards/sign-in-guard.service';

import {AdminReviewIdVerificationComponent} from './views/admin-review-id-verification/component';
import {AdminReviewWorkspaceComponent} from './views/admin-review-workspace/component';
import {CohortEditComponent} from './views/cohort-edit/component';
import {LoginComponent} from './views/login/component';
import {ProfilePageComponent} from './views/profile-page/component';
import {SettingsComponent} from './views/settings/component';
import {SignedInComponent} from './views/signed-in/component';
import {WorkspaceEditComponent, WorkspaceEditMode} from './views/workspace-edit/component';
import {WorkspaceListComponent} from './views/workspace-list/component';
import {WorkspaceShareComponent} from './views/workspace-share/component';
import {WorkspaceComponent} from './views/workspace/component';

import {CohortResolver} from './resolvers/cohort';
import {WorkspaceResolver} from './resolvers/workspace';

declare let gtag: Function;
declare let ga_tracking_id: string;

const routes: Routes = [
  {
    path: 'data-browser/home',
    component: HomeComponent,
    data: {title: 'Data Browser'}
  }, {
    path: 'data-browser/browse',
    component: SearchComponent,
    data: {title: 'Browse'}
  }, {
    path: 'login',
    component: LoginComponent,
    data: {title: 'Sign In'}
  }, {
    path: '',
    component: SignedInComponent,
    canActivate: [SignInGuard],
    canActivateChild: [SignInGuard],
    runGuardsAndResolvers: 'always',
    children: [
      {
        path: '',
        data: { breadcrumb: 'Workspaces' },
        children: [
          {
            path: '',
            component: WorkspaceListComponent,
            data: {title: 'View Workspaces'}
          },
          {
            /* TODO The children under ./views need refactoring to use the data
             * provided by the route rather than double-requesting it.
             */
            path: 'workspace/:ns/:wsid',
            data: {
              title: 'View Workspace Details',
              breadcrumb: ':wsid'
            },
            runGuardsAndResolvers: 'always',
            resolve: {
              workspace: WorkspaceResolver,
            },
            children: [{
              path: '',
              component: WorkspaceComponent,
              data: {
                title: 'View Workspace Details',
                breadcrumb: 'View Workspace Details'
              }
            }, {
              path: 'edit',
              component: WorkspaceEditComponent,
              data: {
                title: 'Edit Workspace',
                mode: WorkspaceEditMode.Edit,
                breadcrumb: 'Edit Workspace'
              }
            }, {
              path: 'clone',
              component: WorkspaceEditComponent,
              data: {
                title: 'Clone Workspace',
                mode: WorkspaceEditMode.Clone,
                breadcrumb: 'Clone Workspace'
              }
            }, {
              path: 'cohorts/build',
              loadChildren: './cohort-search/cohort-search.module#CohortSearchModule',
              data: {
                breadcrumb: 'Cohort Builder'
              }
            }, {
              path: 'cohorts/:cid/review',
              loadChildren: './cohort-review/cohort-review.module#CohortReviewModule',
              data: {
                breadcrumb: 'Cohort Review'
              }
            }, {
              path: 'cohorts/:cid/edit',
              component: CohortEditComponent,
              data: {
                title: 'Edit Cohort',
                breadcrumb: 'Edit Cohort'
              },
              resolve: {
                cohort: CohortResolver,
              },
            }],
          }
        ]
      },
      {
        path: 'admin/review-workspace',
        component: AdminReviewWorkspaceComponent,
        data: {title: 'Review Workspaces'}
      }, {
        path: 'admin/review-id-verification',
        component: AdminReviewIdVerificationComponent,
        data: {title: 'Review ID Verifications'}
      }, {
        path: 'profile',
        component: ProfilePageComponent,
        data: {title: 'Profile'}
      }, {
        path: 'settings',
        component: SettingsComponent,
        data: {title: 'Settings'}
      }, {
        path: 'workspace/build',
        component: WorkspaceEditComponent,
        data: {title: 'Create Workspace', mode: WorkspaceEditMode.Create}
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {onSameUrlNavigation: 'reload'})],
  exports: [RouterModule],
  providers: [
    CohortResolver,
    SignInGuard,
    WorkspaceResolver,
  ]
})
export class AppRoutingModule {

 constructor(public router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        gtag('config', ga_tracking_id, { 'page_path': event.urlAfterRedirects });
      }
    });
  }
}
