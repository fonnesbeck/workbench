package org.pmiops.workbench;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.mockito.Mock;
import org.pmiops.workbench.config.WorkbenchConfig;
import org.pmiops.workbench.notebooks.NotebooksRetryHandler;
import org.pmiops.workbench.notebooks.NotebooksServiceImpl;
import org.pmiops.workbench.notebooks.api.ClusterApi;
import org.pmiops.workbench.notebooks.api.NotebooksApi;
import org.pmiops.workbench.test.Providers;
import org.springframework.retry.backoff.NoBackOffPolicy;

/**
 * Created by brubenst on 5/8/18.
 */
public class NotebooksIntegrationTest {
   /*
    * Mocked service providers are (currently) not available for functional integration tests.
    * Integration tests with real projects/clusters against production Notebooks is not
    * recommended at this time.
    */

  @Mock
  private ClusterApi clusterApi;
  @Mock
  private NotebooksApi notebooksApi;

  private final NotebooksServiceImpl notebooksService = new NotebooksServiceImpl(
      Providers.of(clusterApi),
      Providers.of(notebooksApi),
      Providers.of(createConfig()),
      new NotebooksRetryHandler(new NoBackOffPolicy()));

  @Test
  public void testStatus() {
    assertThat(notebooksService.getNotebooksStatus()).isTrue();
  }

  private static WorkbenchConfig createConfig() {
    WorkbenchConfig config = new WorkbenchConfig();
    config.firecloud = new WorkbenchConfig.FireCloudConfig();
    config.firecloud.debugEndpoints = true;
    return config;
  }
}
