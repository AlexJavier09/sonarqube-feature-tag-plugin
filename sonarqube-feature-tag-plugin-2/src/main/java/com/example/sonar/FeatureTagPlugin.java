package com.example.sonar;

import org.sonar.api.Plugin;

public class FeatureTagPlugin implements Plugin {
    @Override
    public void define(Context context) {
        context.addExtension(FeatureTagRulesDefinition.class);
        context.addExtension(FeatureTagSensor.class);
    }
}
