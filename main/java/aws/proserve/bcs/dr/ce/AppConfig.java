// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package aws.proserve.bcs.dr.ce;

import aws.proserve.bcs.ce.config.CloudEndureCommonConfig;
import aws.proserve.bcs.ce.service.SessionService;
import aws.proserve.bcs.dr.lambda.util.Assure;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CloudEndureCommonConfig.class)
public class AppConfig {
    static ApplicationContext context;

    private static ApplicationContext context() {
        if (context == null) {
            context = new AnnotationConfigApplicationContext(AppConfig.class);
        }
        return context;
    }

    public static ApplicationContext login() {
        Assure.assure(context().getBean(SessionService.class)::login);
        return context();
    }

    @Bean
    DynamoDBMapper dbMapper() {
        return new DynamoDBMapper(
                AmazonDynamoDBClientBuilder.defaultClient(),
                DynamoDBMapperConfig.builder()
                        .withSaveBehavior(SaveBehavior.CLOBBER)
                        .build());
    }
}
