package info.lockhead.infrastructure;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionAttributes;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlCorsOptions;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.HttpMethod;
import software.amazon.awscdk.services.lambda.nodejs.NodejsFunction;
import software.constructs.Construct;

public class PaperSizeStack extends Stack {
	private FunctionUrl functionUrl;

	public PaperSizeStack(final Construct parent, final String id) {
		this(parent, id, null);
	}
	/*
	 input event:
	 {
 	"Country": "Germany"
	}
	 */
	public PaperSizeStack(final Construct parent, final String id, final StackProps props) {
		super(parent, id, props);

		NodejsFunction paperSize = NodejsFunction.Builder.create(this, "PaperSizeHandler").entry("paper-size/lib/paper-size.ts")
				.handler("handler").memorySize(128).depsLockFilePath("paper-size/package-lock.json").build();

//		final Function paperSize = Function.Builder.create(this, "PaperSizeHandler")
//				.runtime(software.amazon.awscdk.services.lambda.Runtime.NODEJS_16_X) // execution environment
//				.code(Code.fromAsset("lambda-typescript-2/lib")) // code loaded from the "lambda" directory
//				.memorySize(64).handler("papersize.handler") // file is "hello", function is "handler"
//				.build();

		functionUrl = paperSize.addFunctionUrl(FunctionUrlOptions.builder().authType(FunctionUrlAuthType.NONE)
				.cors(FunctionUrlCorsOptions.builder().allowedHeaders(Arrays.asList("*"))
						.allowedMethods(Arrays.asList(HttpMethod.ALL)).allowedOrigins(Arrays.asList("*")).build())
				.build());
	}

	public String getFunctionUrl() {
		if (functionUrl == null) {
			return "";
		}
		return functionUrl.getUrl();
	}
}
