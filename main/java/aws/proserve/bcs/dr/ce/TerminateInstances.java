// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.CloudEndureMachine;
import aws.proserve.bcs.ce.CloudEndureSourceProperties;
import aws.proserve.bcs.ce.service.MachineService;
import aws.proserve.bcs.dr.lambda.VoidHandler;
import aws.proserve.bcs.dr.project.ProjectFinder;
import aws.proserve.bcs.dr.project.Side;
import aws.proserve.bcs.dr.secret.Credential;
import aws.proserve.bcs.dr.secret.SecretManager;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.waiters.WaiterParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class TerminateInstances implements VoidHandler<TerminateInstances.Request> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void handleRequest(Request request, Context context) {
        final var spring = AppConfig.login();
        final var finder = spring.getBean(ProjectFinder.class);
        final var project = finder.findOne(request.getProjectId());
        final var side = request.getSide();
        final var item = Objects.requireNonNull(project.getCloudEndureProject().getItem(side), side.name());
        final var instanceIds = Stream.of(spring.getBean(MachineService.class).findAll(item.getId()))
                .map(CloudEndureMachine::getSourceProperties)
                .map(CloudEndureSourceProperties::getMachineCloudId)
                .toArray(String[]::new);
        final var ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(Credential.toProvider(spring.getBean(SecretManager.class).getCredential(project)))
                .withRegion(project.getRegion(side).toAwsRegion())
                .build();

        log.info("Terminate instances {}", Arrays.toString(instanceIds));
        ec2.terminateInstances(new TerminateInstancesRequest()
                .withInstanceIds(List.of(instanceIds)));
        ec2.waiters().instanceTerminated()
                .run(new WaiterParameters<>(new DescribeInstancesRequest()
                        .withInstanceIds(instanceIds)));
    }

    static class Request {
        private Side side;
        private String projectId;

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
    }
}
