// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.CloudEndureReplicationConfiguration;
import aws.proserve.bcs.ce.dto.ImmutableConfigureCloudEndureProjectInput;
import aws.proserve.bcs.ce.service.ProjectService;
import aws.proserve.bcs.ce.service.RegionService;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigureProject implements RequestHandler<ConfigureProject.Request, CloudEndureReplicationConfiguration> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public CloudEndureReplicationConfiguration handleRequest(Request request, Context context) {
        final var spring = AppConfig.login();
        final var targetRegion = spring.getBean(RegionService.class).find(
                request.getCredentialId(), Regions.fromName(request.getTargetRegion()));
        log.info("Found target region {} with ID {}", targetRegion.getName(), targetRegion.getId());

        final var input = ImmutableConfigureCloudEndureProjectInput.builder()
                .region(targetRegion.getId())
                .subnetId(request.getStagingSubnetId())
                .usePrivateIp(!request.isPublicNetwork())
                .disablePublicIp(!request.isPublicNetwork())
                .build();
        return spring.getBean(ProjectService.class).configure(request.getProjectId(), input);
    }

    static class Request {
        private boolean publicNetwork;
        private String projectId;
        private String credentialId;
        private String targetRegion;
        private String stagingSubnetId;

        public boolean isPublicNetwork() {
            return publicNetwork;
        }

        public void setPublicNetwork(boolean publicNetwork) {
            this.publicNetwork = publicNetwork;
        }

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

        public String getTargetRegion() {
            return targetRegion;
        }

        public void setTargetRegion(String targetRegion) {
            this.targetRegion = targetRegion;
        }

        public String getStagingSubnetId() {
            return stagingSubnetId;
        }

        public void setStagingSubnetId(String stagingSubnetId) {
            this.stagingSubnetId = stagingSubnetId;
        }
    }
}
