package info.lockhead.infrastructure;

import java.util.Arrays;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.LifecycleRule;
import software.constructs.Construct;

public class FlutterStack extends Stack {
    private Bucket deploymentBucket;

	public FlutterStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public FlutterStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);
		LifecycleRule rule = LifecycleRule.builder().enabled(true).expiration(Duration.days(10)).build();
		BucketProps appBucketProps = BucketProps.builder().bucketName("cdk-codepipeline-flutter")
				.blockPublicAccess(
						BlockPublicAccess.Builder.create().blockPublicPolicy(false).blockPublicAcls(false).build()).publicReadAccess(false).versioned(false).lifecycleRules(Arrays.asList(rule))
				.build();
		deploymentBucket=new Bucket(this, "cdk-codepipeline-flutter", appBucketProps);
		
    
    }

	public Bucket getDeploymentBucket() {
		return deploymentBucket;
	}

}
