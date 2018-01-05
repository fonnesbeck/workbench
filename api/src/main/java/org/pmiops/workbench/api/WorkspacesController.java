package org.pmiops.workbench.api;

import com.google.common.base.Strings;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.pmiops.workbench.annotations.AuthorityRequired;
import org.pmiops.workbench.db.dao.CdrVersionDao;
import org.pmiops.workbench.db.dao.UserDao;
import org.pmiops.workbench.db.dao.WorkspaceService;
import org.pmiops.workbench.db.model.CdrVersion;
import org.pmiops.workbench.db.model.User;
import org.pmiops.workbench.db.model.Workspace.FirecloudWorkspaceId;
import org.pmiops.workbench.db.model.WorkspaceUserRole;
import org.pmiops.workbench.exceptions.BadRequestException;
import org.pmiops.workbench.exceptions.ConflictException;
import org.pmiops.workbench.exceptions.ForbiddenException;
import org.pmiops.workbench.exceptions.NotFoundException;
import org.pmiops.workbench.exceptions.ServerErrorException;
import org.pmiops.workbench.firecloud.FireCloudService;
import org.pmiops.workbench.model.Authority;
import org.pmiops.workbench.model.CloneWorkspaceRequest;
import org.pmiops.workbench.model.CloneWorkspaceResponse;
import org.pmiops.workbench.model.DataAccessLevel;
import org.pmiops.workbench.model.EmptyResponse;
import org.pmiops.workbench.model.ResearchPurpose;
import org.pmiops.workbench.model.ResearchPurposeReviewRequest;
import org.pmiops.workbench.model.ShareWorkspaceRequest;
import org.pmiops.workbench.model.ShareWorkspaceResponse;
import org.pmiops.workbench.model.UserRole;
import org.pmiops.workbench.model.Workspace;
import org.pmiops.workbench.model.WorkspaceAccessLevel;
import org.pmiops.workbench.model.WorkspaceListResponse;
import org.pmiops.workbench.model.WorkspaceResponse;
import org.pmiops.workbench.model.WorkspaceResponseListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class WorkspacesController implements WorkspacesApiDelegate {

  private static final Logger log = Logger.getLogger(WorkspacesController.class.getName());

  private static final String RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyz";
  private static final int NUM_RANDOM_CHARS = 20;
  private static final int MAX_FC_CREATION_ATTEMPT_VALUES = 6;

  /**
   * Converter function from backend representation (used with Hibernate) to
   * client representation (generated by Swagger).
   */
  @Deprecated
  private static final Function<org.pmiops.workbench.db.model.Workspace, Workspace>
      TO_CLIENT_WORKSPACE =
      new Function<org.pmiops.workbench.db.model.Workspace, Workspace>() {
        @Override
        public Workspace apply(org.pmiops.workbench.db.model.Workspace workspace) {
          FirecloudWorkspaceId workspaceId = workspace.getFirecloudWorkspaceId();
          ResearchPurpose researchPurpose = new ResearchPurpose()
              .diseaseFocusedResearch(workspace.getDiseaseFocusedResearch())
              .diseaseOfFocus(workspace.getDiseaseOfFocus())
              .methodsDevelopment(workspace.getMethodsDevelopment())
              .controlSet(workspace.getControlSet())
              .aggregateAnalysis(workspace.getAggregateAnalysis())
              .ancestry(workspace.getAncestry())
              .commercialPurpose(workspace.getCommercialPurpose())
              .population(workspace.getPopulation())
              .populationOfFocus(workspace.getPopulationOfFocus())
              .additionalNotes(workspace.getAdditionalNotes())
              .reviewRequested(workspace.getReviewRequested())
              .approved(workspace.getApproved());

          if(workspace.getTimeRequested() != null){
            researchPurpose.timeRequested(workspace.getTimeRequested().getTime());
          }

          if(workspace.getTimeReviewed() != null){
            researchPurpose.timeReviewed(workspace.getTimeReviewed().getTime());
          }
          Workspace result = new Workspace()
              .etag(Etags.fromVersion(workspace.getVersion()))
              .lastModifiedTime(workspace.getLastModifiedTime().getTime())
              .creationTime(workspace.getCreationTime().getTime())
              .dataAccessLevel(workspace.getDataAccessLevel())
              .name(workspace.getName())
              .id(workspaceId.getWorkspaceName())
              .namespace(workspaceId.getWorkspaceNamespace())
              .description(workspace.getDescription())
              .researchPurpose(researchPurpose);
          if (workspace.getCreator() != null) {
            result.setCreator(workspace.getCreator().getEmail());
          }
          if (workspace.getCdrVersion() != null) {
            result.setCdrVersionId(String.valueOf(workspace.getCdrVersion().getCdrVersionId()));
          }


          result.setUserRoles(workspace.getWorkspaceUserRoles().stream().map(TO_CLIENT_USER_ROLE).collect(Collectors.toList()));

          return result;
        }
      };
      private static final BiFunction<org.pmiops.workbench.db.model.Workspace, org.pmiops.workbench.firecloud.model.Workspace, Workspace>
          TO_CLIENT_WORKSPACE_FROM_FC_AND_DB =
          new BiFunction<org.pmiops.workbench.db.model.Workspace, org.pmiops.workbench.firecloud.model.Workspace, Workspace>() {
            @Override
            public Workspace apply(org.pmiops.workbench.db.model.Workspace workspace,
                org.pmiops.workbench.firecloud.model.Workspace fcWorkspace) {
              ResearchPurpose researchPurpose = new ResearchPurpose()
                  .diseaseFocusedResearch(workspace.getDiseaseFocusedResearch())
                  .diseaseOfFocus(workspace.getDiseaseOfFocus())
                  .methodsDevelopment(workspace.getMethodsDevelopment())
                  .controlSet(workspace.getControlSet())
                  .aggregateAnalysis(workspace.getAggregateAnalysis())
                  .ancestry(workspace.getAncestry())
                  .commercialPurpose(workspace.getCommercialPurpose())
                  .population(workspace.getPopulation())
                  .populationOfFocus(workspace.getPopulationOfFocus())
                  .additionalNotes(workspace.getAdditionalNotes())
                  .reviewRequested(workspace.getReviewRequested())
                  .approved(workspace.getApproved());

              if(workspace.getTimeRequested() != null){
                researchPurpose.timeRequested(workspace.getTimeRequested().getTime());
              }

              if(workspace.getTimeReviewed() != null){
                researchPurpose.timeReviewed(workspace.getTimeReviewed().getTime());
              }
              Workspace result = new Workspace()
                  .etag(Etags.fromVersion(workspace.getVersion()))
                  .lastModifiedTime(workspace.getLastModifiedTime().getTime())
                  .creationTime(workspace.getCreationTime().getTime())
                  .dataAccessLevel(workspace.getDataAccessLevel())
                  .name(workspace.getName())
                  .id(fcWorkspace.getName())
                  .namespace(fcWorkspace.getNamespace())
                  .description(workspace.getDescription())
                  .researchPurpose(researchPurpose);
              if (fcWorkspace.getCreatedBy() != null) {
                result.setCreator(fcWorkspace.getCreatedBy());
              }
              if (workspace.getCdrVersion() != null) {
                result.setCdrVersionId(String.valueOf(workspace.getCdrVersion().getCdrVersionId()));
              }


              result.setUserRoles(workspace.getWorkspaceUserRoles().stream().map(TO_CLIENT_USER_ROLE).collect(Collectors.toList()));

              return result;
            }
          };

  private static final Function<Workspace, org.pmiops.workbench.db.model.Workspace>
      FROM_CLIENT_WORKSPACE =
      new Function<Workspace, org.pmiops.workbench.db.model.Workspace>() {
        @Override
        public org.pmiops.workbench.db.model.Workspace apply(Workspace workspace) {
          org.pmiops.workbench.db.model.Workspace result = new org.pmiops.workbench.db.model.Workspace();
          if (workspace.getDataAccessLevel() != null) {
            result.setDataAccessLevel(
                DataAccessLevel.fromValue(workspace.getDataAccessLevel().toString()));
          }
          result.setDescription(workspace.getDescription());
          result.setName(workspace.getName());
          if (workspace.getResearchPurpose() != null) {
            setResearchPurposeDetails(result, workspace.getResearchPurpose());
            if (workspace.getResearchPurpose().getTimeReviewed() != null) {
              result
                  .setTimeReviewed(new Timestamp(workspace.getResearchPurpose().getTimeReviewed()));
            }
            result.setReviewRequested(workspace.getResearchPurpose().getReviewRequested());
            if (workspace.getResearchPurpose().getTimeRequested() != null) {
              result.setTimeRequested(
                  new Timestamp(workspace.getResearchPurpose().getTimeRequested()));
            }
            result.setApproved(workspace.getResearchPurpose().getApproved());
          }
          return result;
        }
      };


      private static final Function<org.pmiops.workbench.db.model.WorkspaceUserRole, UserRole>
          TO_CLIENT_USER_ROLE =
          new Function<org.pmiops.workbench.db.model.WorkspaceUserRole, UserRole>() {
            @Override
            public UserRole apply(org.pmiops.workbench.db.model.WorkspaceUserRole workspaceUserRole) {
              UserRole result = new UserRole();
              result.setEmail(workspaceUserRole.getUser().getEmail());
              result.setRole(workspaceUserRole.getRole());

              return result;
            }
          };


  private final WorkspaceService workspaceService;
  private final CdrVersionDao cdrVersionDao;
  private final UserDao userDao;
  private final Provider<User> userProvider;
  private final FireCloudService fireCloudService;
  private final Clock clock;

  @Autowired
  WorkspacesController(
      WorkspaceService workspaceService,
      CdrVersionDao cdrVersionDao,
      UserDao userDao,
      Provider<User> userProvider,
      FireCloudService fireCloudService,
      Clock clock) {
    this.workspaceService = workspaceService;
    this.cdrVersionDao = cdrVersionDao;
    this.userDao = userDao;
    this.userProvider = userProvider;
    this.fireCloudService = fireCloudService;
    this.clock = clock;
  }

  private static String generateRandomChars(String candidateChars, int length) {
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < length; i++) {
      sb.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
    }
    return sb.toString();
  }

  private void setCdrVersionId(org.pmiops.workbench.db.model.Workspace dbWorkspace, String cdrVersionId) {
    if (cdrVersionId != null) {
      try {
        CdrVersion cdrVersion = cdrVersionDao.findOne(Long.parseLong(cdrVersionId));
        if (cdrVersion == null) {
          throw new BadRequestException(
              String.format("CDR version with ID %s not found", cdrVersionId));
        }
        dbWorkspace.setCdrVersion(cdrVersion);
      } catch (NumberFormatException e) {
        throw new BadRequestException(String.format(
            "Invalid cdr version ID: %s", cdrVersionId));
      }
    }
  }

  /**
   * Sets user-editable research purpose detail fields.
   */
  private static void setResearchPurposeDetails(org.pmiops.workbench.db.model.Workspace dbWorkspace,
      ResearchPurpose purpose) {
    dbWorkspace.setDiseaseFocusedResearch(purpose.getDiseaseFocusedResearch());
    dbWorkspace.setDiseaseOfFocus(purpose.getDiseaseOfFocus());
    dbWorkspace.setMethodsDevelopment(purpose.getMethodsDevelopment());
    dbWorkspace.setControlSet(purpose.getControlSet());
    dbWorkspace.setAggregateAnalysis(purpose.getAggregateAnalysis());
    dbWorkspace.setAncestry(purpose.getAncestry());
    dbWorkspace.setCommercialPurpose(purpose.getCommercialPurpose());
    dbWorkspace.setPopulation(purpose.getPopulation());
    dbWorkspace.setPopulationOfFocus(purpose.getPopulationOfFocus());
    dbWorkspace.setAdditionalNotes(purpose.getAdditionalNotes());
  }

  private FirecloudWorkspaceId generateFirecloudWorkspaceId(String namespace, String name) {
    // Find a unique workspace namespace based off of the provided name.
    String strippedName = name.toLowerCase().replaceAll("[^0-9a-z]", "");
    // If the stripped name has no chars, generate a random name.
    if (strippedName.isEmpty()) {
      strippedName = generateRandomChars(RANDOM_CHARS, NUM_RANDOM_CHARS);
    }
    return new FirecloudWorkspaceId(namespace, strippedName);
  }

  private void checkWorkspaceWriteAccess(String workspaceNamespace, String workspaceId) {
    WorkspaceAccessLevel userAccess = getWorkspaceAccessLevel(workspaceNamespace, workspaceId);
    if (!(WorkspaceAccessLevel.OWNER.equals(userAccess) ||
          WorkspaceAccessLevel.WRITER.equals(userAccess))) {
      throw new ForbiddenException(String.format("Insufficient permissions to edit workspace %s/%s",
          workspaceNamespace, workspaceId));
    }
  }

  private void checkWorkspaceReadAccess(String workspaceNamespace, String workspaceId) {
    WorkspaceAccessLevel userAccess = getWorkspaceAccessLevel(workspaceNamespace, workspaceId);
    if (!(WorkspaceAccessLevel.OWNER.equals(userAccess) ||
          WorkspaceAccessLevel.WRITER.equals(userAccess) ||
          WorkspaceAccessLevel.READER.equals(userAccess))) {
      throw new ForbiddenException(String.format("Insufficient permissions to read workspace %s/%s",
          workspaceNamespace, workspaceId));
    }
  }

  private WorkspaceAccessLevel getWorkspaceAccessLevel(String workspaceNamespace, String workspaceId) {
    String userAccess;
    try {
      userAccess = fireCloudService.getWorkspace(
          workspaceNamespace, workspaceId).getAccessLevel();
    } catch (org.pmiops.workbench.firecloud.ApiException e) {
      if (e.getCode() == 404) {
        throw new NotFoundException(String.format("Workspace %s/%s not found",
            workspaceNamespace, workspaceId));
      } else {
        throw new ServerErrorException(e.getResponseBody());
      }
    }
    return WorkspaceAccessLevel.fromValue(userAccess);
  }

  private void attemptFirecloudWorkspaceCreation(FirecloudWorkspaceId workspaceId) {
    try {
      fireCloudService.createWorkspace(workspaceId.getWorkspaceNamespace(),
          workspaceId.getWorkspaceName());
    } catch (org.pmiops.workbench.firecloud.ApiException e) {
      log.log(
          Level.SEVERE,
          String.format(
              "Error creating FC workspace %s/%s: %s",
              workspaceId.getWorkspaceNamespace(),
              workspaceId.getWorkspaceName(),
              e.getResponseBody()),
          e);
      if (e.getCode() == 403) {
        throw new ForbiddenException(e.getResponseBody());
      } else if (e.getCode() == 409) {
        throw new ConflictException(e.getResponseBody());
      } else {
        throw new ServerErrorException(e.getResponseBody());
      }
    }
  }

  @Override
  public ResponseEntity<Workspace> createWorkspace(Workspace workspace) {
    if (Strings.isNullOrEmpty(workspace.getNamespace())) {
      throw new BadRequestException("missing required field 'namespace'");
    } else if (Strings.isNullOrEmpty(workspace.getName())) {
      throw new BadRequestException("missing required field 'name'");
    } else if (workspace.getResearchPurpose() == null) {
      throw new BadRequestException("missing required field 'researchPurpose'");
    } else if (workspace.getDataAccessLevel() == null) {
      throw new BadRequestException("missing required field 'dataAccessLevel'");
    }
    User user = userProvider.get();
    org.pmiops.workbench.db.model.Workspace existingWorkspace = workspaceService.getByName(
        workspace.getNamespace(), workspace.getName());
    if (existingWorkspace != null) {
      throw new ConflictException(String.format(
          "Workspace %s/%s already exists",
          workspace.getNamespace(), workspace.getName()));
    }

    FirecloudWorkspaceId workspaceId = generateFirecloudWorkspaceId(workspace.getNamespace(),
        workspace.getName());
    FirecloudWorkspaceId fcWorkspaceId = workspaceId;
    for (int attemptValue = 0; attemptValue < this.MAX_FC_CREATION_ATTEMPT_VALUES; attemptValue++) {
      try {
        attemptFirecloudWorkspaceCreation(fcWorkspaceId);
        break;
      } catch (ConflictException e) {
        if (attemptValue >= 5) {
          throw e;
        } else {
          fcWorkspaceId =
              new FirecloudWorkspaceId(workspaceId.getWorkspaceNamespace(),
                  workspaceId.getWorkspaceName() + Integer.toString(attemptValue));
        }
      }
    }
    Timestamp now = new Timestamp(clock.instant().toEpochMilli());
    org.pmiops.workbench.db.model.Workspace dbWorkspace =
        new org.pmiops.workbench.db.model.Workspace();
    dbWorkspace.setFirecloudName(fcWorkspaceId.getWorkspaceName());
    dbWorkspace.setWorkspaceNamespace(fcWorkspaceId.getWorkspaceNamespace());
    dbWorkspace.setCreator(user);
    dbWorkspace.setCreationTime(now);
    dbWorkspace.setLastModifiedTime(now);
    dbWorkspace.setVersion(1);
    setCdrVersionId(dbWorkspace, workspace.getCdrVersionId());

    org.pmiops.workbench.db.model.Workspace reqWorkspace = FROM_CLIENT_WORKSPACE.apply(workspace);
    // TODO: enforce data access level authorization
    dbWorkspace.setDataAccessLevel(reqWorkspace.getDataAccessLevel());
    dbWorkspace.setName(reqWorkspace.getName());
    dbWorkspace.setDescription(reqWorkspace.getDescription());

    // Ignore incoming fields pertaining to review status; clients can only request a review.
    setResearchPurposeDetails(dbWorkspace, workspace.getResearchPurpose());
    if (reqWorkspace.getReviewRequested()) {
      // Use a consistent timestamp.
      dbWorkspace.setTimeRequested(now);
    }
    dbWorkspace.setReviewRequested(reqWorkspace.getReviewRequested());

    org.pmiops.workbench.db.model.WorkspaceUserRole permissions = new org.pmiops.workbench.db.model.WorkspaceUserRole();
    permissions.setRole(WorkspaceAccessLevel.OWNER);
    permissions.setWorkspace(dbWorkspace);
    permissions.setUser(user);

    dbWorkspace.addWorkspaceUserRole(permissions);

    dbWorkspace = workspaceService.getDao().save(dbWorkspace);
    return ResponseEntity.ok(TO_CLIENT_WORKSPACE.apply(dbWorkspace));
  }

  @Override
  public ResponseEntity<EmptyResponse> deleteWorkspace(String workspaceNamespace, String workspaceId) {
    org.pmiops.workbench.db.model.Workspace dbWorkspace = workspaceService.getRequired(
        workspaceNamespace, workspaceId);
    try {
      fireCloudService.deleteWorkspace(workspaceNamespace, workspaceId);
    } catch (org.pmiops.workbench.firecloud.ApiException e) {
      if (e.getCode() == 403) {
        throw new ForbiddenException(String.format("Insufficient permissions to delete workspace %s/%s",
            workspaceNamespace, workspaceId));
      } else if (e.getCode() == 404) {
        throw new NotFoundException(String.format("Workspace %s/%s not found",
            workspaceNamespace, workspaceId));
      } else {
        throw new ServerErrorException(e.getResponseBody());
      }
    }
    workspaceService.getDao().delete(dbWorkspace);
    return ResponseEntity.ok(new EmptyResponse());
  }

  @Override
  public ResponseEntity<WorkspaceResponse> getWorkspace(String workspaceNamespace, String workspaceId) {
    org.pmiops.workbench.db.model.Workspace dbWorkspace = workspaceService.getRequired(
        workspaceNamespace, workspaceId);
    org.pmiops.workbench.firecloud.model.WorkspaceResponse fcResponse;
    org.pmiops.workbench.firecloud.model.Workspace fcWorkspace;

    WorkspaceResponse response = new WorkspaceResponse();

    try {
      fcResponse = fireCloudService.getWorkspace(
          workspaceNamespace, workspaceId);
      fcWorkspace = fcResponse.getWorkspace();
    } catch (org.pmiops.workbench.firecloud.ApiException e) {
      if (e.getCode() == 404) {
        throw new NotFoundException(String.format("Workspace %s/%s not found",
            workspaceNamespace, workspaceId));
      } else {
        throw new ServerErrorException(e.getResponseBody());
      }
    }

    response.setAccessLevel(WorkspaceAccessLevel.fromValue(fcResponse.getAccessLevel()));
    response.setWorkspace(TO_CLIENT_WORKSPACE_FROM_FC_AND_DB.apply(dbWorkspace, fcWorkspace));
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<WorkspaceResponseListResponse> getWorkspaces() {
    // TODO: use FireCloud to determine what workspaces to return, instead of just returning
    // workspaces from our database.
    User user = userProvider.get();
    List<WorkspaceResponse> responseList = new ArrayList<WorkspaceResponse>();
    if (user != null) {
      for (WorkspaceUserRole userRole : user.getWorkspaceUserRoles()) {
        // TODO: Use FireCloud to determine access roles, not our DB
        WorkspaceResponse currentWorkspace = new WorkspaceResponse();
        currentWorkspace.setWorkspace(TO_CLIENT_WORKSPACE.apply(userRole.getWorkspace()));
        currentWorkspace.setAccessLevel(userRole.getRole());
        responseList.add(currentWorkspace);
      }
    }
    WorkspaceResponseListResponse response = new WorkspaceResponseListResponse();
    response.setItems(responseList);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Workspace> updateWorkspace(String workspaceNamespace, String workspaceId,
      Workspace workspace) {
    org.pmiops.workbench.db.model.Workspace dbWorkspace = workspaceService.getRequired(
        workspaceNamespace, workspaceId);
    checkWorkspaceWriteAccess(workspaceNamespace, workspaceId);

    if (Strings.isNullOrEmpty(workspace.getEtag())) {
      throw new BadRequestException("Missing required update field 'etag'");
    }
    int version = Etags.toVersion(workspace.getEtag());
    if (dbWorkspace.getVersion() != version) {
      throw new ConflictException("Attempted to modify outdated workspace version");
    }
    if(workspace.getDataAccessLevel() != null &&
        !dbWorkspace.getDataAccessLevel().equals(workspace.getDataAccessLevel())){
      throw new BadRequestException("Attempted to change data access level");
    }
    if (workspace.getDescription() != null) {
      dbWorkspace.setDescription(workspace.getDescription());
    }
    if (workspace.getName() != null) {
      dbWorkspace.setName(workspace.getName());
    }
    // TODO: handle research purpose
    setCdrVersionId(dbWorkspace, workspace.getCdrVersionId());
    // The version asserted on save is the same as the one we read via
    // getRequired() above, see RW-215 for details.
    dbWorkspace = workspaceService.saveWithLastModified(dbWorkspace);
    return ResponseEntity.ok(TO_CLIENT_WORKSPACE.apply(dbWorkspace));
  }

  @Override
  public ResponseEntity<CloneWorkspaceResponse> cloneWorkspace(String workspaceNamespace,
      String workspaceId, CloneWorkspaceRequest body) {
    Workspace workspace = body.getWorkspace();
    if (Strings.isNullOrEmpty(workspace.getNamespace())) {
      throw new BadRequestException("missing required field 'workspace.namespace'");
    } else if (Strings.isNullOrEmpty(workspace.getName())) {
      throw new BadRequestException("missing required field 'workspace.name'");
    } else if (workspace.getResearchPurpose() == null) {
      throw new BadRequestException("missing required field 'workspace.researchPurpose'");
    }
    User user = userProvider.get();
    if (workspaceService.getByName(workspace.getNamespace(), workspace.getName()) != null) {
      throw new ConflictException(String.format(
          "Workspace %s/%s already exists",
          workspace.getNamespace(), workspace.getName()));
    }

    checkWorkspaceReadAccess(workspaceNamespace, workspaceId);
    org.pmiops.workbench.db.model.Workspace fromWorkspace = workspaceService.getRequired(
        workspaceNamespace, workspaceId);
    if (fromWorkspace == null) {
      throw new NotFoundException(String.format(
          "Workspace %s/%s not found", workspaceNamespace, workspaceId));
    }

    FirecloudWorkspaceId fcWorkspaceId = generateFirecloudWorkspaceId(workspace.getNamespace(),
        workspace.getName());
    fireCloudService.cloneWorkspace(workspaceNamespace, workspaceId,
        fcWorkspaceId.getWorkspaceNamespace(), fcWorkspaceId.getWorkspaceName());

    // TODO(calbach): Determine whether we need to copy GCS notebooks here.

    org.pmiops.workbench.db.model.Workspace toWorkspace =
        FROM_CLIENT_WORKSPACE.apply(body.getWorkspace());
    org.pmiops.workbench.db.model.Workspace dbWorkspace =
        new org.pmiops.workbench.db.model.Workspace();

    Timestamp now = new Timestamp(clock.instant().toEpochMilli());
    dbWorkspace.setFirecloudName(fcWorkspaceId.getWorkspaceName());
    dbWorkspace.setWorkspaceNamespace(fcWorkspaceId.getWorkspaceNamespace());
    dbWorkspace.setCreator(user);
    dbWorkspace.setCreationTime(now);
    dbWorkspace.setLastModifiedTime(now);
    dbWorkspace.setVersion(1);

    dbWorkspace.setName(toWorkspace.getName());
    ResearchPurpose researchPurpose = body.getWorkspace().getResearchPurpose();
    setResearchPurposeDetails(dbWorkspace, researchPurpose);
    if (researchPurpose.getReviewRequested()) {
      // Use a consistent timestamp.
      dbWorkspace.setTimeRequested(now);
    }
    dbWorkspace.setReviewRequested(researchPurpose.getReviewRequested());

    // Clone the previous description, by default.
    if (Strings.isNullOrEmpty(toWorkspace.getDescription())) {
      dbWorkspace.setDescription(fromWorkspace.getDescription());
    } else {
      dbWorkspace.setDescription(toWorkspace.getDescription());
    }

    // TODO(calbach): Copy cohorts.
    dbWorkspace.setCdrVersion(fromWorkspace.getCdrVersion());
    dbWorkspace.setDataAccessLevel(fromWorkspace.getDataAccessLevel());

    org.pmiops.workbench.db.model.WorkspaceUserRole permissions = new org.pmiops.workbench.db.model.WorkspaceUserRole();
    permissions.setRole(WorkspaceAccessLevel.OWNER);
    permissions.setWorkspace(dbWorkspace);
    permissions.setUser(user);

    dbWorkspace.addWorkspaceUserRole(permissions);

    dbWorkspace = workspaceService.getDao().save(dbWorkspace);
    CloneWorkspaceResponse resp = new CloneWorkspaceResponse();
    resp.setWorkspace(TO_CLIENT_WORKSPACE.apply(dbWorkspace));
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<ShareWorkspaceResponse> shareWorkspace(String workspaceNamespace, String workspaceId,
      ShareWorkspaceRequest request) {
    if (Strings.isNullOrEmpty(request.getWorkspaceEtag())) {
      throw new BadRequestException("Missing required update field 'workspaceEtag'");
    }

    org.pmiops.workbench.db.model.Workspace dbWorkspace =
        workspaceService.getRequired(workspaceNamespace, workspaceId);
    int version = Etags.toVersion(request.getWorkspaceEtag());
    if (dbWorkspace.getVersion() != version) {
      throw new ConflictException("Attempted to modify user roles with outdated workspace etag");
    }
    Set<WorkspaceUserRole> dbUserRoles = new HashSet<WorkspaceUserRole>();
    for (UserRole user : request.getItems()) {
      WorkspaceUserRole newUserRole = new WorkspaceUserRole();
      User newUser = userDao.findUserByEmail(user.getEmail());
      if (newUser == null) {
        throw new BadRequestException(String.format(
            "User %s doesn't exist",
            user.getEmail()));
      }
      newUserRole.setUser(newUser);
      newUserRole.setRole(user.getRole());
      dbUserRoles.add(newUserRole);
    }
    dbWorkspace = workspaceService.updateUserRoles(dbWorkspace, dbUserRoles);
    ShareWorkspaceResponse resp = new ShareWorkspaceResponse();
    resp.setWorkspaceEtag(Etags.fromVersion(dbWorkspace.getVersion()));
    return ResponseEntity.ok(resp);
  }

  /** Record approval or rejection of research purpose. */
  @AuthorityRequired({Authority.REVIEW_RESEARCH_PURPOSE})
  public ResponseEntity<EmptyResponse> reviewWorkspace(
      String ns, String id, ResearchPurposeReviewRequest review) {
    workspaceService.setResearchPurposeApproved(ns, id, review.getApproved());
    return ResponseEntity.ok(new EmptyResponse());
  }


  // Note we do not paginate the workspaces list, since we expect few workspaces
  // to require review.
  //
  // We can add pagination in the DAO by returning Slice<Workspace> if we want the method to return
  // pagination information (e.g. are there more workspaces to get), and Page<Workspace> if we
  // want the method to return both pagination information and a total count.
  @AuthorityRequired({Authority.REVIEW_RESEARCH_PURPOSE})
  public ResponseEntity<WorkspaceListResponse> getWorkspacesForReview() {
    WorkspaceListResponse response = new WorkspaceListResponse();
    List<org.pmiops.workbench.db.model.Workspace> workspaces = workspaceService.findForReview();
    response.setItems(workspaces.stream().map(TO_CLIENT_WORKSPACE).collect(Collectors.toList()));
    return ResponseEntity.ok(response);
  }
}
