// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.service.ProjectService;
import aws.proserve.bcs.dr.lambda.BoolHandler;
import com.amazonaws.services.lambda.runtime.Context;

import java.util.stream.Stream;

public class CheckName implements BoolHandler<String> {

    @Override
    public boolean handleRequest(String name, Context context) {
        return Stream.of(AppConfig.login().getBean(ProjectService.class).findAll())
                .map(CloudEndureItem::getName)
                .anyMatch(name::equals);
    }
}
