package info.lockhead;

import info.lockhead.infrastructure.FlutterStack;
import software.amazon.awscdk.Stage;
import software.constructs.Construct;

public class FlutterBuildStage extends Stage {

	public FlutterBuildStage(final Construct scope, final String id) {
		super(scope, id);

		new FlutterStack(this, "FlutterStack");

	}
}
