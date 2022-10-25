package info.lockhead.infrastructure;

import java.util.Arrays;
import java.util.List;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.cloudfront.Behavior;
import software.amazon.awscdk.services.cloudfront.CloudFrontWebDistribution;
import software.amazon.awscdk.services.cloudfront.S3OriginConfig;
import software.amazon.awscdk.services.cloudfront.SourceConfiguration;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.eventsources.SnsEventSource;
import software.amazon.awscdk.services.lambda.nodejs.NodejsFunction;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.LifecycleRule;
import software.amazon.awscdk.services.sns.ITopic;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.ssm.ParameterDataType;
import software.amazon.awscdk.services.ssm.StringParameter;
import software.constructs.Construct;

public class FlutterStack extends Stack {
	private Bucket deploymentBucket;
	private Bucket apkBucket;

	public FlutterStack(final Construct parent, final String id) {
		super(parent, id);
		LifecycleRule rule = LifecycleRule.builder().enabled(true).expiration(Duration.days(10)).build();
		createDeploymentBucket(rule);
		createApkBuildResultBucket(rule);
		createIosTriggerLambda();
		createSSMParameterForIosBuild();
		createCloudFrontForDeploymentBucket();
	}
	
	private void createDeploymentBucket(LifecycleRule rule) {
		BucketProps appBucketProps = BucketProps.builder().bucketName("cdk-codepipeline-flutter")
				.websiteIndexDocument("index.html")
				.blockPublicAccess(
						BlockPublicAccess.Builder.create().blockPublicPolicy(false).blockPublicAcls(false).build())
				.publicReadAccess(true).versioned(false).lifecycleRules(Arrays.asList(rule)).build();
		deploymentBucket = new Bucket(this, "cdk-codepipeline-flutter", appBucketProps);
	}

	private void createApkBuildResultBucket(LifecycleRule rule) {
		BucketProps appApkBucketProps = BucketProps.builder().bucketName("cdk-codepipeline-flutter-apk")
				.websiteIndexDocument("index.html")
				.blockPublicAccess(
						BlockPublicAccess.Builder.create().blockPublicPolicy(false).blockPublicAcls(false).build())
				.publicReadAccess(false).versioned(false).lifecycleRules(Arrays.asList(rule)).build();
		apkBucket = new Bucket(this, "cdk-codepipeline-flutter-apk", appApkBucketProps);
	}

	private void createCloudFrontForDeploymentBucket() {
		CloudFrontWebDistribution distribution = CloudFrontWebDistribution.Builder.create(this, "FlutterCloudfrontDistribution")
		         .originConfigs(List.of(SourceConfiguration.builder()
		                 .s3OriginSource(S3OriginConfig.builder()
		                         .s3BucketSource(deploymentBucket)
		                         .build())
		                 .behaviors(List.of(Behavior.builder().isDefaultBehavior(true).build()))
		                 .build()))
		         .build();
		CfnOutput.Builder exportUrl = CfnOutput.Builder.create(this, "FlutterCloudfrontURL");
		exportUrl.exportName("FlutterCloudfrontURL");
		exportUrl.value(distribution.getDistributionDomainName());
		exportUrl.build();
		CfnOutput.Builder create = CfnOutput.Builder.create(this, "FlutterCloudfrontID");
		create.exportName("FlutterCloudfrontID");
		create.value(distribution.getDistributionId());
		create.build();
		
	}

	private void createSSMParameterForIosBuild() {
		software.amazon.awscdk.services.ssm.StringParameter.Builder parameter = StringParameter.Builder.create(this, "SSMParameterIOS");
		parameter.dataType(ParameterDataType.TEXT);
		parameter.parameterName("/codepipeline/build-ios-app");
		parameter.stringValue("false");
		parameter.build();
	}

	private void createIosTriggerLambda() {
		NodejsFunction iOsBuild = NodejsFunction.Builder.create(this, "TriggerIOSBuildHandler")
				.entry("ios-build/lib/ios-build.ts").handler("handler").memorySize(128).runtime(software.amazon.awscdk.services.lambda.Runtime.NODEJS_16_X)
				.depsLockFilePath("ios-build/package-lock.json").build();

		PolicyStatement stsAccess = PolicyStatement.Builder.create().effect(Effect.ALLOW).resources(Arrays.asList("*"))
				.actions(Arrays.asList("ssm:DescribeParameters", "ssm:GetParameters", "ssm:GetParameter",
						"ssm:GetParameterHistory", "sts:*", "s3:*", "sns:*"))
				.build();
		iOsBuild.addToRolePolicy(stsAccess);

		ITopic fromTopicArn = Topic.fromTopicArn(this, "fromTopicArn", Fn.importValue("FlutterCDKSNSTarget"));
		iOsBuild.addEventSource(SnsEventSource.Builder.create(fromTopicArn).build());
	}



	public Bucket getDeploymentBucket() {
		return deploymentBucket;
	}

	public Bucket getApkBucket() {
		return apkBucket;
	}

}
