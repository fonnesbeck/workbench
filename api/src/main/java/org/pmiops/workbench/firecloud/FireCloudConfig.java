package org.pmiops.workbench.firecloud;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import org.pmiops.workbench.auth.UserAuthentication;
import org.pmiops.workbench.config.WorkbenchConfig;
import org.pmiops.workbench.config.WorkbenchEnvironment;
import org.pmiops.workbench.exceptions.ServerErrorException;
import org.pmiops.workbench.firecloud.api.BillingApi;
import org.pmiops.workbench.firecloud.api.GroupsApi;
import org.pmiops.workbench.firecloud.api.ProfileApi;
import org.pmiops.workbench.firecloud.api.WorkspacesApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.annotation.RequestScope;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

@org.springframework.context.annotation.Configuration
public class FireCloudConfig {

  private static final Logger log = Logger.getLogger(FireCloudConfig.class.getName());

  private static final String END_USER_API_CLIENT = "endUserApiClient";
  private static final String ALL_OF_US_API_CLIENT = "allOfUsApiClient";

  private static final String[] BILLING_SCOPES = new String[] {
      "https://www.googleapis.com/auth/userinfo.profile",
      "https://www.googleapis.com/auth/userinfo.email",
      "https://www.googleapis.com/auth/cloud-billing"
  };

  @Bean(name=END_USER_API_CLIENT)
  @RequestScope(proxyMode = ScopedProxyMode.DEFAULT)
  public ApiClient fireCloudApiClient(UserAuthentication userAuthentication,
      WorkbenchConfig workbenchConfig) {
    ApiClient apiClient = new ApiClient();
    apiClient.setAccessToken(userAuthentication.getCredentials());
    apiClient.setDebugging(workbenchConfig.firecloud.debugEndpoints);
    return apiClient;
  }

  private String getWorkbenchServiceAccountAccessToken(WorkbenchEnvironment workbenchEnvironment) throws IOException {
    // When running locally, we get application default credentials in a different way than
    // when running in Cloud.
    if (workbenchEnvironment.isDevelopment()) {
      GoogleCredential credential = GoogleCredential.getApplicationDefault()
          .createScoped(Arrays.asList(BILLING_SCOPES));
      credential.refreshToken();
      return credential.getAccessToken();
    } else {
      AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
      final AppIdentityService.GetAccessTokenResult accessTokenResult =
          appIdentity.getAccessToken(Arrays.asList(BILLING_SCOPES));
      return accessTokenResult.getAccessToken();
    }

  }

  @Bean(name=ALL_OF_US_API_CLIENT)
  @RequestScope(proxyMode = ScopedProxyMode.DEFAULT)
  public ApiClient allOfUsApiClient(WorkbenchEnvironment workbenchEnvironment,
      WorkbenchConfig workbenchConfig) {
    ApiClient apiClient = new ApiClient();
    try {
      apiClient.setAccessToken(getWorkbenchServiceAccountAccessToken(workbenchEnvironment));
      apiClient.setDebugging(workbenchConfig.firecloud.debugEndpoints);
    } catch (IOException e) {
      throw new ServerErrorException(e);
    }
    return apiClient;
  }

  @Bean
  @RequestScope(proxyMode = ScopedProxyMode.DEFAULT)
  public ProfileApi profileApi(@Qualifier(END_USER_API_CLIENT) ApiClient apiClient) {
    ProfileApi api = new ProfileApi();
    api.setApiClient(apiClient);
    return api;
  }

  @Bean
  @RequestScope(proxyMode = ScopedProxyMode.DEFAULT)
  public WorkspacesApi workspacesApi(@Qualifier(END_USER_API_CLIENT) ApiClient apiClient) {
    WorkspacesApi api = new WorkspacesApi();
    api.setApiClient(apiClient);
    return api;
  }

  @Bean
  @RequestScope(proxyMode = ScopedProxyMode.DEFAULT)
  public BillingApi billingApi(@Qualifier(ALL_OF_US_API_CLIENT) ApiClient apiClient) {
    // Billing calls are made by the AllOfUs service account, rather than using the end user's
    // credentials.
    BillingApi api = new BillingApi();
    api.setApiClient(apiClient);
    return api;
  }

  @Bean
  @RequestScope(proxyMode = ScopedProxyMode.DEFAULT)
  public GroupsApi groupsApi(@Qualifier(ALL_OF_US_API_CLIENT) ApiClient apiClient) {
    // Group/Auth Domain creation and addition are made by the AllOfUs service account
    GroupsApi api = new GroupsApi();
    api.setApiClient(apiClient);
    return api;
  }
}
