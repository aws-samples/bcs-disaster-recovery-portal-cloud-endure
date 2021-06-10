// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.service.MachineService;
import aws.proserve.bcs.dr.lambda.BoolHandler;
import aws.proserve.bcs.dr.project.ProjectFinder;
import aws.proserve.bcs.dr.project.Side;
import aws.proserve.bcs.dr.secret.SecretManager;
import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class InstallAgent implements BoolHandler<InstallAgent.Request> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean handleRequest(Request request, Context context) {
        final var side = request.getSide();
        final var spring = AppConfig.login();

        // It may receive output from step functions directly, which is a JSON string.
        final var projectId = request.getProjectId().replaceAll("\"", "");
        final var project = spring.getBean(ProjectFinder.class).findOne(projectId);
        final var item = Objects.requireNonNull(project.getCloudEndureProject().getItem(side), side.name());

        try {
            spring.getBean(MachineService.class).installAgent(
                    spring.getBean(SecretManager.class).getCredential(project),
                    project.getRegion(side).getName(),
                    item.getAgentInstallationToken(),
                    request.getInstanceIds());
            return true;
        } catch (Exception e) {
            log.warn("Unable to install agent", e);
            return false;
        }
    }

    static class Request {
        private Side side;
        private String projectId;
        private String[] instanceIds;

        public Side getSide() {
            return side;
        }

        public void setSide(Side side) {
            this.side = side;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        /**
         * @return EC2 instance ID. (Do NOT use machineId)
         */
        public String[] getInstanceIds() {
            return instanceIds;
        }

        public void setInstanceIds(String[] instanceIds) {
            this.instanceIds = instanceIds;
        }
    }
}
