package com.myorg;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.RemovalPolicy;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;

public class Service01Stack extends Stack {

    public Service01Stack(final Construct scope, final String id, Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, Cluster cluster) {
        super(scope, id, props);
        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder.create(this, "ALB01")
            .serviceName("service-01")
            .cluster(cluster)
            .cpu(512)
            .desiredCount(2)
            .listenerPort(8080)
            .memoryLimitMiB(1024)
            .taskImageOptions(
                ApplicationLoadBalancedTaskImageOptions.builder()
                    .containerName("aws_project")
                    .image(ContainerImage.fromRegistry("viniciusferrarini/aws_project:1.0.0"))
                    .containerPort(8080)
                    .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, "Service01LogGroup")
                            .logGroupName("Service01")
                            .removalPolicy(RemovalPolicy.DESTROY)
                            .build())
                        .streamPrefix("Service01")
                        .build()))
                    .build())
            .publicLoadBalancer(true)
            .build();

        //Configuração para o monitoramento da saúde da aplicação
        service01.getTargetGroup().configureHealthCheck(HealthCheck.builder()
            .path("/actuator/health")
            .port("8080")
            .healthyHttpCodes("200")
            .build());

        //Configuração do auto-scaling
        ScalableTaskCount scalableTaskCount = service01.getService()
            .autoScaleTaskCount(EnableScalingProps.builder()
                .minCapacity(2)
                .maxCapacity(4)
                .build());

        //Controle do uso de CPU
        scalableTaskCount.scaleOnCpuUtilization("Service01AutoScaling", CpuUtilizationScalingProps.builder()
            .targetUtilizationPercent(50)
            .scaleInCooldown(Duration.seconds(60)) //Valores sensiveis
            .scaleOutCooldown(Duration.seconds(60))
            .build());


    }
}
