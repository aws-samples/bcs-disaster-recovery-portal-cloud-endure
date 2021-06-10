// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.CloudEndureCredential;
import aws.proserve.bcs.ce.dto.ImmutableCloudEndureCredentialInput;
import aws.proserve.bcs.ce.service.CredentialService;
import aws.proserve.bcs.dr.secret.SecretManager;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.Objects;

public class CreateCredential implements RequestHandler<String, CloudEndureCredential> {

    @Override
    public CloudEndureCredential handleRequest(String secretId, Context context) {
        final var spring = AppConfig.login();
        final var awsCredential = spring.getBean(SecretManager.class).getCredential(secretId);
        Objects.requireNonNull(awsCredential, "Unable to find secret at " + secretId);
        final var credential = ImmutableCloudEndureCredentialInput.builder()
                .publicKey(awsCredential.getAccess())
                .privateKey(awsCredential.getSecret())
                .cloudId(spring.getBean(CredentialService.class).getAwsCloudId())
                .build();
        return spring.getBean(CredentialService.class).save(credential);
    }
}
