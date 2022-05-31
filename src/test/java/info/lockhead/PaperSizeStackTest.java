package info.lockhead;

import org.junit.jupiter.api.Test;

import info.lockhead.infrastructure.PaperSizeStack;
import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

class PaperSizeStackTest {

	@Test
	void testOneBucket() {
		App app = new App();

		PaperSizeStack uxStack = new PaperSizeStack(app, "pb-hw-test-stack");

		Template template = Template.fromStack(uxStack);
		template.resourceCountIs("AWS::Lambda::Function", 1);
	}

}
