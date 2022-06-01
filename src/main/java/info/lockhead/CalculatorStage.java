package info.lockhead;

import info.lockhead.infrastructure.CalculatorLambdaStack;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;

public class CalculatorStage extends Stage {
	public CalculatorStage(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CalculatorStage(final Construct scope, final String id, final StageProps props) {
        super(scope, id, props);

        new CalculatorLambdaStack(this, "CalculatorLambdaStack");
    }
}
