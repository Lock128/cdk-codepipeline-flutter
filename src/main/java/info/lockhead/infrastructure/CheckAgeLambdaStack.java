package info.lockhead.infrastructure;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

public class CheckAgeLambdaStack extends Stack {
    public CheckAgeLambdaStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public CheckAgeLambdaStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);
    // Defines a new lambda resource
    final Function checkAge = Function.Builder.create(this, "CheckAgeHandler")
        .runtime(software.amazon.awscdk.services.lambda.Runtime.NODEJS_14_X)    // execution environment
        .code(Code.fromAsset("lambda-typescript/lib"))  // code loaded from the "lambda" directory
        .handler("check-age.handler")        // file is "hello", function is "handler"
        .build();
    }
    
}
