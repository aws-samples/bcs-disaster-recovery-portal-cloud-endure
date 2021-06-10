// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.CloudEndureLaunchResult;
import aws.proserve.bcs.ce.dto.ImmutableLaunchMachinesInput;
import aws.proserve.bcs.ce.dto.ImmutableMachineAndPointInTime;
import aws.proserve.bcs.ce.dto.LaunchMachinesInput.MachineAndPointInTime;
import aws.proserve.bcs.ce.service.MachineService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class LaunchMachines implements RequestHandler<LaunchMachines.Request, CloudEndureLaunchResult> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public CloudEndureLaunchResult handleRequest(Request request, Context context) {
        final var spring = AppConfig.login();
        final var input = ImmutableLaunchMachinesInput.builder()
                .launchType(request.getLaunchType())
                .items(Stream.of(request.getMachineIds())
                        .map(i -> ImmutableMachineAndPointInTime.builder().machineId(i).build())
                        .toArray(MachineAndPointInTime[]::new))
                .build();
        log.info("Launch machines input [{}]", input);
        return spring.getBean(MachineService.class).launch(request.getProjectId(), input);
    }

    static class Request {
        private String projectId;
        private String launchType;
        private String[] machineIds;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getLaunchType() {
            return launchType;
        }

        public void setLaunchType(String launchType) {
            this.launchType = launchType;
        }

        public String[] getMachineIds() {
            return machineIds;
        }

        public void setMachineIds(String[] machineIds) {
            this.machineIds = machineIds;
        }
    }
}
