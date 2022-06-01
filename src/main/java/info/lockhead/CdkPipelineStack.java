package info.lockhead;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import software.amazon.awscdk.services.codebuild.PipelineProject;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.constructs.Construct;

public class CdkPipelineStack extends Stack {
	public CdkPipelineStack(final Construct parent, final String id, final String branch) {
		this(parent, id, branch, null);
	}

	public CdkPipelineStack(final Construct parent, final String id, final String branch, final StackProps props) {
		super(parent, id, props);

		String connectionArn = "arn:aws:codestar-connections:eu-central-1:916032256060:connection/12b42dad-052d-4e38-ad41-f96eccb43132";
		Artifact sourceBuildOutput = new Artifact();

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

		pipeline.addStage(new CheckAgeLambdaStage(this, "DeployLambdaFunction"),
				AddStageOpts.builder()
						.pre(List.of(ShellStep.Builder.create("Execute TypescriptTests")
								.commands(List.of("cd lambda-typescript", "npm install", "npm test", "ls -al",
										"ls -al coverage", "cd ..", "ls -al lambda-typescript",
										"ls -al lambda-typescript-2", "bash start_codecov.sh"))
								.build()))
						.build());
		pipeline.addStage(new PaperSizeStage(this, "PaperSizeStage"),
				AddStageOpts.builder()
						.pre(List.of(ShellStep.Builder.create("Execute TypescriptTests 2nd function")
								.commands(List.of("cd lambda-typescript-2", "npm install", "npm test", "ls -al",
										"ls -al coverage", "cd ..", "ls -al lambda-typescript",
										"ls -al lambda-typescript-2", "bash start_codecov.sh"))
								.build()))
						.build());

		pipeline.addStage(new FlutterBuildStage(this, "FlutterBuildStage"),
				AddStageOpts.builder().pre(List.of(ShellStep.Builder.create("Install Flutter")
						.commands(
								List.of("git clone https://github.com/flutter/flutter.git -b stable --depth 1",
										"export PATH=\"$PATH:`pwd`/flutter/bin\"", "flutter precache", "flutter doctor",
										"flutter doctor", "flutter devices"))
						.build())).post(
								List.of(ShellStep.Builder.create("Execute Flutter Build and CodeCov")
										.commands(List.of(
												"git clone https://github.com/flutter/flutter.git -b stable --depth 1",
												"export PATH=\"$PATH:`pwd`/flutter/bin\"", "flutter precache",
												"flutter doctor", "flutter doctor", "flutter devices", "cd ui",
												"flutter test", "flutter build web --verbose", "bash ../start_codecov.sh",
												"aws s3 sync build/web/* s3://cdk-codepipeline-flutter"))
										.build()))
						.build());

	}

	private PipelineProject getTypescriptTestProject() {
		PipelineProject lambdaBuild = PipelineProject.Builder.create(this, "TypeScriptLambdaTest")
				.buildSpec(BuildSpec.fromObject(new HashMap<String, Object>() {
					{
						put("version", "0.2");
						put("phases", new HashMap<String, Object>() {
							{
								put("install", new HashMap<String, List<String>>() {
									{
										put("commands", Arrays.asList("ls -al"));
									}
								});
								put("build", new HashMap<String, List<String>>() {
									{
										put("commands", Arrays.asList("cd lambda-typescript", "npm test"));
									}
								});
							}
						});
						put("artifacts", new HashMap<String, Object>() {
							{
								put("base-directory", "lambda-typescript/coverage");
								put("files", Arrays.asList("**/*"));
							}
						});
					}
				})).environment(BuildEnvironment.builder().buildImage(LinuxBuildImage.AMAZON_LINUX_2_3).build())
				.build();

		return lambdaBuild;
	}

	private String getCodepipelineName(String branch) {
		String string = "CDKCodepipelineFlutterStack" + branch.replace("-", "").replace("/", "");
		if (string.length() > 50) {
			string = string.substring(0, 50);
		}
		return string;
	}
}