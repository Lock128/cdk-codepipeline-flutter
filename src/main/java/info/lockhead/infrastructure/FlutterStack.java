package info.lockhead.infrastructure;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import info.lockhead.CdkPipelineStack;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.codepipeline.IPipeline;
import software.amazon.awscdk.services.codepipeline.Pipeline;
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
import software.constructs.Construct;

public class FlutterStack extends Stack {
	private Bucket deploymentBucket;
	private Bucket apkBucket;

	public FlutterStack(final Construct parent, final String id) {
		this(parent, id, null);
	}

	public FlutterStack(final Construct parent, final String id, final CdkPipelineStack cdkPipelineStack) {
		super(parent, id);
		LifecycleRule rule = LifecycleRule.builder().enabled(true).expiration(Duration.days(10)).build();
		BucketProps appBucketProps = BucketProps.builder().bucketName("cdk-codepipeline-flutter")
				.websiteIndexDocument("index.html")
				.blockPublicAccess(
						BlockPublicAccess.Builder.create().blockPublicPolicy(false).blockPublicAcls(false).build())
				.publicReadAccess(true).versioned(false).lifecycleRules(Arrays.asList(rule)).build();
		deploymentBucket = new Bucket(this, "cdk-codepipeline-flutter", appBucketProps);
		BucketProps appApkBucketProps = BucketProps.builder().bucketName("cdk-codepipeline-flutter-apk")
				.websiteIndexDocument("index.html")
				.blockPublicAccess(
						BlockPublicAccess.Builder.create().blockPublicPolicy(false).blockPublicAcls(false).build())
				.publicReadAccess(false).versioned(false).lifecycleRules(Arrays.asList(rule)).build();
		apkBucket = new Bucket(this, "cdk-codepipeline-flutter-apk", appApkBucketProps);

		NodejsFunction iOsBuild = NodejsFunction.Builder.create(this, "TriggerIOSBuildHandler")
				.entry("ios-build/lib/ios-build.ts").handler("handler").memorySize(128)
				.depsLockFilePath("ios-build/package-lock.json").build();

		PolicyStatement stsAccess = PolicyStatement.Builder.create().effect(Effect.ALLOW).resources(Arrays.asList("*"))
				.actions(Arrays.asList("ssm:DescribeParameters", "ssm:GetParameters", "ssm:GetParameter",
						"ssm:GetParameterHistory", "sts:*", "s3:*", "sns:*"))
				.build();
		iOsBuild.addToRolePolicy(stsAccess);

		if (cdkPipelineStack != null && cdkPipelineStack.getSnsTopic() != null) {
			iOsBuild.addEventSource(SnsEventSource.Builder.create((ITopic) cdkPipelineStack.getSnsTopic()).build());
		} else {
			System.err.println("Error adding EventSource - cdkPipelineStack=" + cdkPipelineStack);
			IPipeline fromPipelineArn = Pipeline.fromPipelineArn(this, "pipelineFromArn", "arn:aws:codepipeline:eu-central-1:916032256060:CDKCodepipelineFlutterStackmain");
			ITopic fromTopicArn = Topic.fromTopicArn(this, "fromTopicArn","arn:aws:sns:eu-central-1:916032256060:DeliveryPipelineTopic-flutterbuild");
			iOsBuild.addEventSource(SnsEventSource.Builder.create(fromTopicArn).build());
		}

	}

	public Bucket getDeploymentBucket() {
		return deploymentBucket;
	}

	public Bucket getApkBucket() {
		return apkBucket;
	}

}
