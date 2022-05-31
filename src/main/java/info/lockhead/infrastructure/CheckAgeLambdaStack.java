package info.lockhead.infrastructure;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlCorsOptions;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.HttpMethod;
import software.amazon.awscdk.services.lambda.nodejs.NodejsFunction;
import software.constructs.Construct;

public class CheckAgeLambdaStack extends Stack {
	private @NotNull FunctionUrl functionUrl;

	public CheckAgeLambdaStack(final Construct parent, final String id) {
		this(parent, id, null);
	}

	public CheckAgeLambdaStack(final Construct parent, final String id, final StackProps props) {
		super(parent, id, props);
		// Defines a new lambda resource
		
		NodejsFunction checkAge = NodejsFunction.Builder.create(this, "CheckAgeHandler").entry("lambda-typescript/lib/check-age.ts")
				.entry("handler").memorySize(64).build();
		
		
		functionUrl = checkAge.addFunctionUrl(FunctionUrlOptions.builder().authType(FunctionUrlAuthType.NONE)
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
