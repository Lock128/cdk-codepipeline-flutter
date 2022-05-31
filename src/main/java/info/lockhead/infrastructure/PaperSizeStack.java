package info.lockhead.infrastructure;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.constructs.Construct;

public class PaperSizeStack extends Stack {
    public PaperSizeStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public PaperSizeStack(final Construct parent, final String id, final StackProps props) {
        super(parent, id, props);
    // Defines a new lambda resource
    final Function paperSize = Function.Builder.create(this, "PaperSizeStack")
        .runtime(software.amazon.awscdk.services.lambda.Runtime.NODEJS_14_X)    // execution environment
        .code(Code.fromAsset("lambda-typescript-2/lib"))  // code loaded from the "lambda" directory
        .handler("paper-size.handler")        // file is "hello", function is "handler"
        .build();
    }
}
