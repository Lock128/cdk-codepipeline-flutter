package info.lockhead;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class CdkPipelineApp {
    public static void main(final String[] args) {
        App app = new App();

        new CdkPipelineStack(app, "CDKCodepipelineFlutterStack", "main", StackProps.builder()
                .build());
        
        app.synth();
    }
}

