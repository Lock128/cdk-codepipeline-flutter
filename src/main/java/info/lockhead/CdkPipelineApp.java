package info.lockhead;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class CdkPipelineApp {
    public static void main(final String[] args) {
        App app = new App();

        new CdkPipelineStack(app, "CodecovCdkPipelineStack", "main", StackProps.builder()
                .build());
        new CdkPipelineStack(app, "CodecovCdkPipelineStack-branch", "Johannes-Koch/added-india-and-argentina-1653224899205", StackProps.builder()
                .build());     
        
        app.synth();
    }
}

