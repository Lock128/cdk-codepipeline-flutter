package info.lockhead;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.pipelines.AddStageOpts;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.ConnectionSourceOptions;
import software.amazon.awscdk.pipelines.ShellStep;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.codebuild.LinuxBuildImage;
import software.amazon.awscdk.services.codepipeline.Pipeline;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.sns.Topic;
import software.amazon.awscdk.services.sns.subscriptions.EmailSubscription;
import software.constructs.Construct;

public class CdkPipelineStack extends Stack {
	private SnsTopic snsTopic;

	public CdkPipelineStack(final Construct parent, final String id, final String branch) {
		this(parent, id, branch, null);
	}

	public CdkPipelineStack(final Construct parent, final String id, final String branch, final StackProps props) {
		super(parent, id, props);

		String connectionArn = "arn:aws:codestar-connections:eu-central-1:916032256060:connection/12b42dad-052d-4e38-ad41-f96eccb43132";
		final CodePipeline pipeline = CodePipeline.Builder.create(this, getCodepipelineName(branch))
				.pipelineName(getCodepipelineName(branch)).dockerEnabledForSynth(true)
				.synth(CodeBuildStep.Builder.create("SynthStep")
						.input(CodePipelineSource.connection("Lock128/cdk-codepipeline-flutter", branch,
								ConnectionSourceOptions.builder().connectionArn(connectionArn).build()))
						.installCommands(List.of("npm install -g aws-cdk" // Commands to run before build
						)).commands(List.of("mvn test", "mvn package", // Language-specific build commands
								"npx cdk synth", // Synth command (always same)
								"bash start_codecov.sh"))
						.build())
				.build();

//		pipeline.addStage(new CheckAgeLambdaStage(this, "DeployCheckAgeLambda"), getCheckAgeStageOpts());
//		pipeline.addStage(new PaperSizeStage(this, "DeployPaperSizeStage"), getPaperSizeStageOpts());
//		pipeline.addStage(new CalculatorStage(this, "DeployCalculatorStage"), getCalculatorStageOpts());

		PolicyStatement flutterDeployPermission = getDeployPermissions();
		Map<String, Object> android = new TreeMap<String, Object>();
		android.put("android", "latest");
		android.put("java", "corretto8");
		Map<String, Object> runtimeVersion = new TreeMap<String, Object>();
		runtimeVersion.put("runtime-versions", android);
		Map<String, Object> installSpec = new TreeMap<String, Object>();
		installSpec.put("install", runtimeVersion);
		Map<String, Object> buildSpec = new TreeMap<String, Object>();
		buildSpec.put("phases", installSpec);
		CodeBuildStep buildAndDeployManual = CodeBuildStep.Builder.create("Execute Flutter Build and CodeCov")
				.buildEnvironment(BuildEnvironment.builder()
						.buildImage(LinuxBuildImage.fromDockerRegistry("instrumentisto/flutter:3")).build())
				.partialBuildSpec(BuildSpec.fromObject(buildSpec)).installCommands(getFlutterInstallCommands())
				.commands(getFlutterBuildShellSteps()).rolePolicyStatements(Arrays.asList(flutterDeployPermission))
				.build();
		CodeBuildStep startiOsBuild = CodeBuildStep.Builder.create("Start iOS build on Codemagic")
				.commands(List.of("pwd", "ls -al")).rolePolicyStatements(Arrays.asList(flutterDeployPermission))
				.build();

		pipeline.addStage(new FlutterBuildStage(this, "FlutterBuildStage"),
				getFlutterStageOptions(buildAndDeployManual, startiOsBuild));

		snsTopic = SnsTopic.Builder.create(Topic.Builder.create(this, "pipelineNotificationTopic-flutterbuild")
				.topicName("DeliveryPipelineTopic-flutterbuild").build()).build();
		snsTopic.getTopic().addToResourcePolicy(PolicyStatement.Builder.create().sid("AllowCodestarNotifications")
				.effect(Effect.ALLOW).actions(Arrays.asList("SNS:Publish")).resources(Arrays.asList("*"))
				.principals(Arrays.asList(new ServicePrincipal("codestar-notifications.amazonaws.com"))).build());

		snsTopic.getTopic().addSubscription(EmailSubscription.Builder.create("lockhead@lockhead.net").build());

		CfnOutput.Builder create = CfnOutput.Builder.create(this, "CognitoIdpUserTableName");
		create.exportName("FlutterCDKSNSTarget");
		create.value(snsTopic.getTopic().getTopicArn());
		create.build();

		try {
			Pipeline detailedPipeline = pipeline.getPipeline();
			detailedPipeline.notifyOnAnyStageStateChange(id,
					Topic.fromTopicArn(this, "snstopicPipelineNotification", snsTopic.getTopic().getTopicArn()));
		} catch (Exception e) {
			if (e.getLocalizedMessage().contains("Pipeline not created yet")) {
				System.err.println("Error attaching notification to pipeline - pipeline not yet created");
				e.printStackTrace();
			} else {
				throw e;
			}
		}

	}

	private PolicyStatement getDeployPermissions() {
		return PolicyStatement.Builder.create().effect(Effect.ALLOW).resources(Arrays.asList("*"))
				.actions(Arrays.asList("ssm:DescribeParameters", "ssm:GetParameters", "ssm:GetParameter",
						"ssm:GetParameterHistory", "cloudformation:*", "s3:*", "apigateway:*", "acm:*", "iam:PassRole"))
				.build();
	}

	private AddStageOpts getFlutterBuildStageOpts() {
		return AddStageOpts.builder()
				.pre(List.of(ShellStep.Builder
						.create("Execute TypescriptTests").commands(List.of("cd ios-build", "npm install", "npm test",
								"ls -al", "ls -al coverage", "cd ..", "ls -al ios-build", "bash start_codecov.sh"))
						.build()))
				.build();
	}

	private AddStageOpts getCheckAgeStageOpts() {
		return AddStageOpts.builder()
				.pre(List.of(ShellStep.Builder.create("Execute TypescriptTests")
						.commands(List.of("cd check-age", "npm install", "npm test", "ls -al", "ls -al coverage",
								"cd ..", "ls -al check-age", "ls -al check-age", "bash start_codecov.sh"))
						.build()))
				.build();
	}

	private AddStageOpts getPaperSizeStageOpts() {
		return AddStageOpts.builder()
				.pre(List.of(ShellStep.Builder.create("Execute TypescriptTests 2nd function")
						.commands(List.of("cd paper-size", "npm install", "npm test", "ls -al", "ls -al coverage",
								"cd ..", "ls -al paper-size", "ls -al paper-size", "bash start_codecov.sh"))
						.build()))
				.build();
	}

	private AddStageOpts getCalculatorStageOpts() {
		return AddStageOpts.builder().pre(List.of(ShellStep.Builder.create("Build Calculator Lambda")
				.commands(List.of("ls -al paper-size", "bash start_codecov.sh")).build())).build();
	}

	private List<String> getFlutterBuildShellSteps() {
		return List.of("cd ui", "flutter test", "flutter build web --verbose", "flutter build apk --no-shrink",
				"bash ../start_codecov.sh", "aws s3 sync build/web s3://cdk-codepipeline-flutter",
				"aws s3 sync build/app s3://cdk-codepipeline-flutter-apk",
				"curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -", "sudo apt-get install -y nodejs",
				"cd ../ios-build", "npm install");
	}

	private AddStageOpts getFlutterStageOptions(CodeBuildStep buildAndDeployManual, CodeBuildStep startiOsBuild) {
		return AddStageOpts.builder()
				.pre(List.of(ShellStep.Builder.create("Install Flutter").commands(getFlutterInstallCommands()).build()))
				.post(List.of(buildAndDeployManual, startiOsBuild)).build();
	}

	private List<String> getFlutterInstallCommands() {
		return List.of("echo $PATH",
				"curl \"https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip\" -o \"awscliv2.zip\"",
				"unzip awscliv2.zip", "sudo ./aws/install --bin-dir /usr/local/bin --update", "echo $PATH",
				"curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -", "sudo apt-get install -y nodejs",
				"cd ios-build", "npm install", "ls -al node_modules", "npm test", "ls -al", "ls -al coverage", "cd ..", "ls -al ios-build",
				"bash start_codecov.sh");
	}

	private String getCodepipelineName(String branch) {
		String string = "CDKCodepipelineFlutterStack" + branch.replace("-", "").replace("/", "");
		if (string.length() > 50) {
			string = string.substring(0, 50);
		}
		return string;
	}

	public SnsTopic getSnsTopic() {
		return snsTopic;
	}
}