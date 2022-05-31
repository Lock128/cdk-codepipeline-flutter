package info.lockhead;

import org.junit.jupiter.api.Test;

import info.lockhead.infrastructure.FlutterStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

class FlutterStackTest {

	@Test
	void testOneBucket() {
		App app = new App();

		FlutterStack uxStack = new FlutterStack(app, "pb-hw-test-stack");

		Template template = Template.fromStack(uxStack);
		template.resourceCountIs("AWS::S3::Bucket", 1);
	}

}
