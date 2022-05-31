package info.lockhead;

import info.lockhead.infrastructure.PaperSizeStack;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;

public class PaperSizeStage extends Stage {
	public PaperSizeStage(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public PaperSizeStage(final Construct scope, final String id, final StageProps props) {
        super(scope, id, props);

        new PaperSizeStack(this, "CheckAgeLambdaStack");
    }
}
