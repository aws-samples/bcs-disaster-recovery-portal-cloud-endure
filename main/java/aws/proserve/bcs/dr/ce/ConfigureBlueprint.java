// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.CloudEndureBlueprint;
import aws.proserve.bcs.ce.CloudEndureBlueprintDisk;
import aws.proserve.bcs.ce.CloudEndureTag;
import aws.proserve.bcs.ce.ImmutableCloudEndureBlueprintDisk;
import aws.proserve.bcs.ce.ImmutableCloudEndureTag;
import aws.proserve.bcs.ce.dto.ImmutableConfigureCloudEndureBlueprintInput;
import aws.proserve.bcs.ce.service.BlueprintService;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigureBlueprint implements RequestHandler<ConfigureBlueprint.Request, CloudEndureBlueprint> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public CloudEndureBlueprint handleRequest(Request request, Context context) {
        final var spring = AppConfig.login();
        final var blueprintService = spring.getBean(BlueprintService.class);
        final var blueprintMap = Stream.of(blueprintService.findAll(request.getProjectId()))
                .collect(Collectors.toMap(CloudEndureBlueprint::getMachineId, CloudEndureBlueprint::getId));
        final var input = ImmutableConfigureCloudEndureBlueprintInput.builder()
                .machineId(request.getMachineId())
                .subnetIDs(request.getSubnetId())
                .securityGroupIDs(request.getSecurityGroupIds())
                .privateIPs(request.getPrivateIp())
                .iamRole(request.getIamRole())
                .instanceType(request.getInstanceType())
                .disks(Stream.of(request.getDisks())
                        .map(d -> ImmutableCloudEndureBlueprintDisk.builder()
                                .name(d)
                                .type(request.getDiskType()).build())
                        .toArray(CloudEndureBlueprintDisk[]::new))
                .tags(Stream.of(request.getTags())
                        .map(t -> ImmutableCloudEndureTag.builder().key(t.getKey()).value(t.getValue()).build())
                        .toArray(CloudEndureTag[]::new))
                .build();
        final var blueprintId = blueprintMap.get(request.getMachineId());
        log.info("Configure blueprint [{}] input [{}]", blueprintId, input);
        return blueprintService.configure(request.getProjectId(), blueprintId, input);
    }

    static class Request {
        private String projectId;
        private String machineId;
        private String subnetId;
        private String[] securityGroupIds;
        private String privateIp;
        private String iamRole;
        private String instanceType;
        private String[] disks;
        private String diskType;
        private int diskIops;
        private Tag[] tags;

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getMachineId() {
            return machineId;
        }

        public void setMachineId(String machineId) {
            this.machineId = machineId;
        }

        public String getSubnetId() {
            return subnetId;
        }

        public void setSubnetId(String subnetId) {
            this.subnetId = subnetId;
        }

        public String[] getSecurityGroupIds() {
            return securityGroupIds;
        }

        public void setSecurityGroupIds(String[] securityGroupIds) {
            this.securityGroupIds = securityGroupIds;
        }

        public String getPrivateIp() {
            return privateIp;
        }

        public void setPrivateIp(String privateIp) {
            this.privateIp = privateIp;
        }

        public String getIamRole() {
            return iamRole;
        }

        public void setIamRole(String iamRole) {
            this.iamRole = iamRole;
        }

        public String getInstanceType() {
            return instanceType;
        }

        public void setInstanceType(String instanceType) {
            this.instanceType = instanceType;
        }

        public String[] getDisks() {
            return disks;
        }

        public void setDisks(String[] disks) {
            this.disks = disks;
        }

        public String getDiskType() {
            return diskType;
        }

        public void setDiskType(String diskType) {
            this.diskType = diskType;
        }

        public int getDiskIops() {
            return diskIops;
        }

        public void setDiskIops(int diskIops) {
            this.diskIops = diskIops;
        }

        public Tag[] getTags() {
            return tags;
        }

        public void setTags(Tag[] tags) {
            this.tags = tags;
        }
    }
}
