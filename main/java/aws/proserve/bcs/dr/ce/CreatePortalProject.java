// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.dr.lambda.StringHandler;
import aws.proserve.bcs.dr.project.Component;
import aws.proserve.bcs.dr.project.Project;
import aws.proserve.bcs.dr.project.ProjectFinder;
import aws.proserve.bcs.dr.project.Region;
import aws.proserve.bcs.dr.secret.SecretManager;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CreatePortalProject implements StringHandler<Map<String, Object>> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String handleRequest(Map<String, Object> request, Context context) {
        final var spring = AppConfig.login();
        final var mapper = spring.getBean(ObjectMapper.class);
        final var projectId = (String) request.get("projectId");
        final var project = projectId == null ? new Project() : spring.getBean(ProjectFinder.class).findOne(projectId);
        final var cloudEndureItem = mapper.convertValue(request.get("cloudEndureItem"), CloudEndureItem.class);

        if (projectId == null) {
            log.info("Create a new project");
            final var secretId = (String) request.get("sourceCredentialId");
            final var secretManager = spring.getBean(SecretManager.class);
            final var cloudEndureProject = new CloudEndureProject();
            cloudEndureProject.setItems(List.of(cloudEndureItem));
            cloudEndureProject.setPublicNetwork((boolean) request.get("publicNetwork"));
            cloudEndureProject.setSourceVpcId((String) request.get("sourceVpcId"));
            cloudEndureProject.setTargetVpcId((String) request.get("targetVpcId"));
            cloudEndureProject.setSourceInstanceType((String) request.get("sourceInstanceType"));
            cloudEndureProject.setTargetInstanceType((String) request.get("targetInstanceType"));

            project.setName((String) request.get("name"));
            project.setType(Component.CloudEndure);
            project.setSourceRegion(new Region(Regions.fromName((String) request.get("sourceRegion"))));
            project.setTargetRegion(new Region(Regions.fromName((String) request.get("targetRegion"))));
            project.setCloudEndureProject(cloudEndureProject);
            spring.getBean(ProjectFinder.class).save(project, secretManager.getCredential(secretId));
            secretManager.deleteSecret(secretId);
        } else {
            log.info("Update an existing project to prepare cutback [{}]", projectId);
            final var items = project.getCloudEndureProject().getItems();
            if (items.size() > 1) {
                items.set(1, cloudEndureItem);
            } else {
                items.add(cloudEndureItem);
            }

            spring.getBean(ProjectFinder.class).save(project);
        }

        return project.getId();
    }
}
