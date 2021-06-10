// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.service.DocumentService;
import aws.proserve.bcs.dr.lambda.VoidHandler;
import aws.proserve.bcs.dr.secret.Credential;
import aws.proserve.bcs.dr.secret.SecretManager;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.CreateDocumentRequest;
import com.amazonaws.services.simplesystemsmanagement.model.DocumentFormat;
import com.amazonaws.services.simplesystemsmanagement.model.DocumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

public class DeployInstallAgentDocument implements VoidHandler<DeployInstallAgentDocument.Request> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String INSTALL_DOCUMENT = "InstallCloudEndureAgent";

    @Override
    public void handleRequest(Request request, Context context) {
        final var spring = AppConfig.login();
        final var credential = spring.getBean(SecretManager.class).getCredential(request.getSecretId());
        final var ssm = AWSSimpleSystemsManagementClientBuilder.standard()
                .withRegion(request.getRegion())
                .withCredentials(Credential.toProvider(credential))
                .build();
        final var document = spring.getBean(DocumentService.class).getDocument(ssm, INSTALL_DOCUMENT);
        if (document != null) {
            log.info("Found document {}", document);
            return;
        }

        log.info("Unable to find document {}, will create a new one.", INSTALL_DOCUMENT);
        final var stream = Objects.requireNonNull(getClass().getClassLoader()
                .getResourceAsStream("documents/InstallCloudEndureAgent.yaml"));
        final String documentContent = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining(System.lineSeparator()));

        ssm.createDocument(new CreateDocumentRequest()
                .withName("DRPortal-CloudEndure-Ssm-InstallCloudEndureAgentDocument")
                .withTargetType("/AWS::EC2::Instance")
                .withDocumentType(DocumentType.Command)
                .withDocumentFormat(DocumentFormat.YAML)
                .withContent(documentContent));
    }

    static class Request {
        private String region;
        private String secretId;

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getSecretId() {
            return secretId;
        }

        public void setSecretId(String secretId) {
            this.secretId = secretId;
        }
    }
}
