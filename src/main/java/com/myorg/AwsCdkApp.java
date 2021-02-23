package com.myorg;

import software.amazon.awscdk.core.App;

import java.util.Arrays;

public class AwsCdkApp {
    public static void main(final String[] args) {
        App app = new App();

        new AwsCdkStack(app, "AwsCdkStack");

        app.synth();
    }
}
