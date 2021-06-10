// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.dto.ImmutableUpdateCloudEndureProjectInput;
import aws.proserve.bcs.ce.service.ProjectService;
import aws.proserve.bcs.ce.service.RegionService;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkConfig implements RequestHandler<LinkConfig.Request, CloudEndureItem> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public CloudEndureItem handleRequest(Request request, Context context) {
        final var spring = AppConfig.login();
        final var sourceRegion = spring.getBean(RegionService.class).find(
                request.getCredentialId(), Regions.fromName(request.getSourceRegion()));
        log.info("Found source region {} with ID {}", sourceRegion.getName(), sourceRegion.getId());

        final var input = ImmutableUpdateCloudEndureProjectInput.builder()
                .sourceRegion(sourceRegion.getId())
                .replicationConfiguration(request.getConfigurationId())
                .build();
        final var result = spring.getBean(ProjectService.class).update(request.getProjectId(), input);
        result.setState(CloudEndureItem.State.NEW.name());
        return result;
    }

    static class Request {
        private String projectId;
        private String credentialId;
        private String configurationId;
        private String sourceRegion;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getCredentialId() {
            return credentialId;
        }

        public void setCredentialId(String credentialId) {
            this.credentialId = credentialId;
        }

        public String getConfigurationId() {
            return configurationId;
        }

        public void setConfigurationId(String configurationId) {
            this.configurationId = configurationId;
        }

        public String getSourceRegion() {
            return sourceRegion;
        }

        public void setSourceRegion(String sourceRegion) {
            this.sourceRegion = sourceRegion;
        }
    }
}
