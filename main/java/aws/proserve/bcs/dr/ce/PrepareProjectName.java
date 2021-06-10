// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.dr.lambda.MapHandler;
import aws.proserve.bcs.dr.project.ProjectFinder;
import aws.proserve.bcs.dr.project.Side;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.Map;

public class PrepareProjectName implements MapHandler<String> {

    @Override
    public Map<String, Object> handleRequest(String projectId, Context context) {
        final var project = AppConfig.login().getBean(ProjectFinder.class).findOne(projectId);
        return Map.of(
                "project", project,
                "sourceCredentialId", project.generateSecretId(Side.source),
                "newName", project.getName() + "-cutback");
    }
}
