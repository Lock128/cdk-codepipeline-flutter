package info.lockhead;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.pipelines.AddStageOpts;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.ConnectionSourceOptions;
import software.amazon.awscdk.pipelines.ShellStep;
import software.amazon.awscdk.pipelines.StageDeployment;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.codebuild.LinuxBuildImage;
import software.amazon.awscdk.services.codebuild.PipelineProject;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.constructs.Construct;

public class CdkPipelineStack extends Stack {
	public CdkPipelineStack(final Construct parent, final String id, final String branch) {
		this(parent, id, branch, null);
	}

	public CdkPipelineStack(final Construct parent, final String id, final String branch, final StackProps props) {
		super(parent, id, props);

		String connectionArn = "arn:aws:codestar-connections:eu-central-1:916032256060:connection/12b42dad-052d-4e38-ad41-f96eccb43132";
		final CodePipeline pipeline = CodePipeline.Builder.create(this, getCodepipelineName(branch))
				.pipelineName(getCodepipelineName(branch)).dockerEnabledForSynth(true)
				.synth(CodeBuildStep.Builder.create("SynthStep")
						.input(CodePipelineSource.connection("lock128/cdk-codepipeline-flutter", branch,
								ConnectionSourceOptions.builder().connectionArn(connectionArn).build()))
						.installCommands(List.of("npm install -g aws-cdk" // Commands to run before build
						)).commands(List.of("mvn test", "mvn package", // Language-specific build commands
								"npx cdk synth", // Synth command (always same)
								"bash start_codecov.sh"))
						.build())
				.build();

		pipeline.addStage(new CheckAgeLambdaStage(this, "DeployCheckAgeLambda"), getCheckAgeStageOpts());
		pipeline.addStage(new PaperSizeStage(this, "DeployPaperSizeStage"), getPaperSizeStageOpts());
		pipeline.addStage(new CalculatorStage(this, "DeployCalculatorStage"), getCalculatorStageOpts());

		PolicyStatement flutterDeployPermission = getDeployPermissions();
		CodeBuildStep buildAndDeployManual = CodeBuildStep.Builder.create("Execute Flutter Build and CodeCov")
				.commands(getFlutterBuildShellSteps()).rolePolicyStatements(Arrays.asList(flutterDeployPermission))
				.build();

		pipeline.addStage(new FlutterBuildStage(this, "FlutterBuildStage"),
				getFlutterStageOptions(buildAndDeployManual));

	}

	private PolicyStatement getDeployPermissions() {
		return PolicyStatement.Builder.create().effect(Effect.ALLOW).resources(Arrays.asList("*"))
				.actions(Arrays.asList("ssm:DescribeParameters", "ssm:GetParameters", "ssm:GetParameter",
						"ssm:GetParameterHistory", "cloudformation:*", "s3:*", "apigateway:*", "acm:*", "iam:PassRole"))
				.build();
	}

	private List<String> getFlutterBuildShellSteps() {
		return List.of("git clone https://github.com/flutter/flutter.git -b stable --depth 1",
				"export PATH=\"$PATH:`pwd`/flutter/bin\"", "flutter precache", "flutter doctor", "flutter doctor",
				"flutter devices", "cd ui", "flutter test", "flutter build web --verbose", "flutter build apk --no-shrink", "bash ../start_codecov.sh",
				"aws s3 sync build/web s3://cdk-codepipeline-flutter", "aws s3 sync build/app s3://cdk-codepipeline-flutter-apk");
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

	private AddStageOpts getFlutterStageOptions(CodeBuildStep buildAndDeployManual) {
		return AddStageOpts.builder()
				.pre(List.of(ShellStep.Builder.create("Install Flutter")
						.commands(List.of("git clone https://github.com/flutter/flutter.git -b stable --depth 1",
								"export PATH=\"$PATH:`pwd`/flutter/bin\"", "flutter precache", "flutter doctor", "flutter devices"))
						.build()))
				.post(List.of(buildAndDeployManual)).build();
	}

	private String getCodepipelineName(String branch) {
		String string = "CDKCodepipelineFlutterStack" + branch.replace("-", "").replace("/", "");
		if (string.length() > 50) {
			string = string.substring(0, 50);
		}
		return string;
	}
}