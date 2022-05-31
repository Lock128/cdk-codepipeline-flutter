package info.lockhead;

import info.lockhead.infrastructure.CheckAgeLambdaStack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;

public class CheckAgeLambdaStage extends Stage {
	public CheckAgeLambdaStage(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CheckAgeLambdaStage(final Construct scope, final String id, final StageProps props) {
        super(scope, id, props);

        new CheckAgeLambdaStack(this, "CheckAgeLambdaStack");

    }
}
