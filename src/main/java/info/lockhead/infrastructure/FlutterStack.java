package info.lockhead.infrastructure;

import java.util.Arrays;

import info.lockhead.CdkPipelineStack;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.SnsEventSource;
import software.amazon.awscdk.services.lambda.nodejs.NodejsFunction;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.LifecycleRule;
import software.amazon.awscdk.services.sns.ITopic;
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
		
		NodejsFunction iOsBuild = NodejsFunction.Builder.create(this, "TriggerIOSBuildHandler").entry("ios-build/lib/ios-build.ts")
				.handler("handler").memorySize(128).depsLockFilePath("ios-build/package-lock.json").build();
		
		iOsBuild.addEventSource(SnsEventSource.Builder.create((ITopic) cdkPipelineStack.getSnsTopic()).build());

	}

	public Bucket getDeploymentBucket() {
		return deploymentBucket;
	}
	public Bucket getApkBucket() {
		return apkBucket;
	}

}
