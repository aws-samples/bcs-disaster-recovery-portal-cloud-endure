// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.service.ProjectService;
import aws.proserve.bcs.dr.lambda.VoidHandler;
import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteEndureProject implements VoidHandler<String> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void handleRequest(String projectId, Context context) {
        final var service = AppConfig.login().getBean(ProjectService.class);
        try {
            service.delete(projectId);
        } catch (Exception e) {
            log.warn("Unable to delete cloud-endure project " + projectId, e);
        }
    }
}
