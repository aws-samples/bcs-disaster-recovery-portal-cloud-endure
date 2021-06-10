// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.dto.ImmutableCreateCloudEndureProjectInput;
import aws.proserve.bcs.ce.service.CredentialService;
import aws.proserve.bcs.ce.service.LicenseService;
import aws.proserve.bcs.ce.service.ProjectService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class CreateEndureProject implements RequestHandler<CreateEndureProject.Request, CloudEndureItem> {

    @Override
    public CloudEndureItem handleRequest(Request request, Context context) {
        final var spring = AppConfig.login();
        final var project = ImmutableCreateCloudEndureProjectInput.builder()
                .name(request.getName())
                .type(CloudEndureItem.Type.MIGRATION.name())
                .targetCloudId(spring.getBean(CredentialService.class).getAwsCloudId())
                .licensesIDs(spring.getBean(LicenseService.class).findMigrationLicense().getId())
                .cloudCredentialsIDs(request.getCredentialId())
                .build();
        return spring.getBean(ProjectService.class).save(project);
    }

    static class Request {
        private String name;
        private String credentialId;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCredentialId() {
            return credentialId;
        }

        public void setCredentialId(String credentialId) {
            this.credentialId = credentialId;
        }
    }
}
