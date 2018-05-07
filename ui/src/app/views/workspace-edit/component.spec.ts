import {ComponentFixture, fakeAsync, inject, TestBed, tick} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {ActivatedRoute, Router} from '@angular/router';
import {RouterTestingModule} from '@angular/router/testing';
import {ClarityModule} from '@clr/angular';

import {ProfileStorageService} from 'app/services/profile-storage.service';
import {ServerConfigService} from 'app/services/server-config.service';
import {WorkspaceStorageService} from 'app/services/workspace-storage.service';
import {WorkspaceEditComponent, WorkspaceEditMode} from 'app/views/workspace-edit/component';
import {WorkspaceNavBarComponent} from 'app/views/workspace-nav-bar/component';
import {WorkspaceShareComponent} from 'app/views/workspace-share/component';

import {ProfileStubVariables} from 'testing/stubs/profile-service-stub';
import {ProfileStorageServiceStub} from 'testing/stubs/profile-storage-service-stub';
import {ServerConfigServiceStub} from 'testing/stubs/server-config-service-stub';
import {WorkspacesServiceStub, WorkspaceStubVariables} from 'testing/stubs/workspace-service-stub';

import {updateAndTick} from 'testing/test-helpers';

import {WorkspaceAccessLevel, WorkspacesService} from 'generated';


describe('WorkspaceEditComponent', () => {
  let activatedRouteStub;
  let testComponent: WorkspaceEditComponent;
  let fixture: ComponentFixture<WorkspaceEditComponent>;
  let workspacesService: WorkspacesServiceStub;

  function setupComponent(mode: WorkspaceEditMode) {
    activatedRouteStub.routeConfig.data.mode = mode;
    fixture = TestBed.createComponent(WorkspaceEditComponent);
    testComponent = fixture.componentInstance;
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  beforeEach(fakeAsync(() => {
    activatedRouteStub = {
      snapshot: {
        url: [
          {path: 'workspace'},
          {path: WorkspaceStubVariables.DEFAULT_WORKSPACE_NS},
          {path: WorkspaceStubVariables.DEFAULT_WORKSPACE_ID},
          {path: 'clone'}
        ],
        params: {
          'ns': WorkspaceStubVariables.DEFAULT_WORKSPACE_NS,
          'wsid': WorkspaceStubVariables.DEFAULT_WORKSPACE_ID
        },
        data: {
          workspace: {
            ...WorkspacesServiceStub.stubWorkspace(),
            accessLevel: WorkspaceAccessLevel.OWNER,
          }
        }
      },
      routeConfig: {data: {}}
    };
    workspacesService = new WorkspacesServiceStub();
    TestBed.configureTestingModule({
      declarations: [
        WorkspaceEditComponent,
        WorkspaceNavBarComponent,
        WorkspaceShareComponent
      ],
      imports: [
        RouterTestingModule,
        FormsModule,
        ClarityModule.forRoot()
      ],
      providers: [
        { provide: WorkspacesService, useValue: workspacesService },
        { provide: WorkspaceStorageService, useClass: WorkspaceStorageService },
        // Wrap in a factory function so we can later mutate the value if needed
        // for testing.
        { provide: ActivatedRoute, useFactory: () => activatedRouteStub },
        { provide: ProfileStorageService, useValue: new ProfileStorageServiceStub() },
        {
          provide: ServerConfigService,
          useValue: new ServerConfigServiceStub({
            gsuiteDomain: 'fake-research-aou.org'
          })
        }
      ]}).compileComponents();
  }));


  it('should support updating a workspace', fakeAsync(() => {
    setupComponent(WorkspaceEditMode.Edit);
    testComponent.workspace.name = 'edited';
    fixture.detectChanges();
    expect(workspacesService.workspaces[0].name).not.toBe('edited');

    fixture.debugElement.query(By.css('.add-button'))
      .triggerEventHandler('click', null);
    fixture.detectChanges();
    tick();
    expect(workspacesService.workspaces.length).toBe(1);
    expect(workspacesService.workspaces[0].name).toBe('edited');
  }));

  it('should support creating a workspace', fakeAsync(() => {
    workspacesService.workspaces = [];
    setupComponent(WorkspaceEditMode.Create);

    testComponent.workspace.namespace = 'foo';
    testComponent.workspace.name = 'created';
    testComponent.workspace.id = 'created';
    fixture.detectChanges();

    fixture.debugElement.query(By.css('.add-button'))
      .triggerEventHandler('click', null);
    fixture.detectChanges();
    tick();
    expect(workspacesService.workspaces.length).toBe(1);
    expect(workspacesService.workspaces[0].name).toBe('created');
  }));

  it('should support cloning a workspace', inject(
    [Router], fakeAsync((router: Router) => {
      workspacesService.workspaceAccess.set(
        WorkspaceStubVariables.DEFAULT_WORKSPACE_ID, WorkspaceAccessLevel.READER);
      setupComponent(WorkspaceEditMode.Clone);
      fixture.componentRef.instance.profileStorageService.reload();
      tick();
      expect(testComponent.workspace.name).toBe(
        `Clone of ${WorkspaceStubVariables.DEFAULT_WORKSPACE_NAME}`);
      expect(testComponent.hasPermission).toBeTruthy(
        'cloner should be able to edit cloned workspace');

      const spy = spyOn(router, 'navigate');
      fixture.debugElement.query(By.css('.add-button'))
        .triggerEventHandler('click', null);
      fixture.detectChanges();
      tick();
      expect(workspacesService.workspaces.length).toBe(2);
      const got = workspacesService.workspaces.find(w => w.name === testComponent.workspace.name);
      expect(got).not.toBeNull();
      expect(got.namespace).toBe(
        ProfileStubVariables.PROFILE_STUB.freeTierBillingProjectName);
      expect(spy).toHaveBeenCalled();
    })));
});
