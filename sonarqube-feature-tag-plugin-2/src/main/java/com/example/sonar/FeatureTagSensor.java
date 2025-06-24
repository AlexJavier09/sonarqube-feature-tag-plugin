package com.example.sonar;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.util.List;

public class FeatureTagSensor implements Sensor {

    private final FileSystem fileSystem;

    public FeatureTagSensor(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("Feature Tag Validator Sensor");
    }

@Override
public void execute(SensorContext context) {
    RuleKey ruleKey = RuleKey.of(FeatureTagRulesDefinition.REPOSITORY_KEY, FeatureTagRulesDefinition.RULE_KEY);

    Iterable<InputFile> featureFiles = fileSystem.inputFiles(file -> file.filename().endsWith(".feature") && file.type() == Type.MAIN);

    for (InputFile featureFile : featureFiles) {
        String content = featureFile.contents();
        List<String> lines = java.util.Arrays.asList(content.split("\\r?\\n"));

        boolean foundTagInFile = false;
        boolean scenarioHasTag = false;

        for (String lineRaw : lines) {
            String line = lineRaw.trim();

            if (line.startsWith("@")) {
                if (line.toLowerCase().contains("@smoketest") || line.toLowerCase().contains("@regressiontest")) {
                    scenarioHasTag = true;
                }
            } else if (line.startsWith("Scenario") || line.startsWith("Scenario Outline")) {
                if (scenarioHasTag) {
                    foundTagInFile = true;
                }
                scenarioHasTag = false;
            }
        }

        if (scenarioHasTag) {
            foundTagInFile = true;
        }

        if (!foundTagInFile) {
            NewIssue newIssue = context.newIssue().forRule(ruleKey);
            NewIssueLocation location = context.newIssueLocation()
                    .on(featureFile)
                    .at(featureFile.selectLine(1))
                    .message("El archivo debe contener al menos un Scenario o Scenario Outline con tag @smokeTest o @regressionTest correctamente escritos.");
            newIssue.at(location).save();
        }
    }
}
}
