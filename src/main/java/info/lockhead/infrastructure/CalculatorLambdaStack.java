package info.lockhead.infrastructure;

import java.util.Arrays;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionUrl;
import software.amazon.awscdk.services.lambda.FunctionUrlAuthType;
import software.amazon.awscdk.services.lambda.FunctionUrlCorsOptions;
import software.amazon.awscdk.services.lambda.FunctionUrlOptions;
import software.amazon.awscdk.services.lambda.HttpMethod;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

public class CalculatorLambdaStack extends Stack {
	private FunctionUrl functionUrl;

	public CalculatorLambdaStack(final Construct parent, final String id) {
		this(parent, id, null);
	}
	/*
	 input event:
	 {
 	"Country": "Germany"
	}
	 */
	public CalculatorLambdaStack(final Construct parent, final String id, final StackProps props) {
		super(parent, id, props);

		Function calculator = Function.Builder.create(this, "CalculatorLambdaHandler").code(Code.fromAsset("calculator"))
				.handler("calculator.lambda_handler").memorySize(128).runtime(Runtime.PYTHON_3_9).build();

		functionUrl = calculator.addFunctionUrl(FunctionUrlOptions.builder().authType(FunctionUrlAuthType.NONE)
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
