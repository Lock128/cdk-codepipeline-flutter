package info.lockhead;

import info.lockhead.infrastructure.FlutterStack;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.constructs.Construct;

public class FlutterBuildStage extends Stage {
	public FlutterBuildStage(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public FlutterBuildStage(final Construct scope, final String id, final StageProps props) {
        super(scope, id, props);

        new FlutterStack(this, "FlutterStack");
    }
}
