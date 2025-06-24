package com.example.sonar;

import io.cucumber.gherkin.Gherkin;
import io.cucumber.messages.IdGenerator;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.GherkinDocument;
import io.cucumber.messages.types.Feature;
import io.cucumber.messages.types.Scenario;
import io.cucumber.messages.types.Tag;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;

import java.util.List;
import java.util.stream.Collectors;

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
        Iterable<InputFile> featureFiles = fileSystem.inputFiles(file -> file.extension().equals("feature") && file.type() == Type.MAIN);

        for (InputFile featureFile : featureFiles) {
            String content = featureFile.contents();

            List<Envelope> envelopes = Gherkin.fromSources(
                    List.of(new Gherkin.PickleSource(featureFile.file().toPath(), content)),
                    true,
                    true,
                    true,
                    IdGenerator.Incrementing::new
            );

            for (Envelope envelope : envelopes) {
                if (envelope.getGherkinDocument().isPresent()) {
                    GherkinDocument doc = envelope.getGherkinDocument().get();
                    Feature feature = doc.getFeature().orElse(null);
                    if (feature != null) {
                        boolean anyScenarioHasTag = feature.getChildren().stream()
                                .filter(child -> child.getScenario().isPresent())
                                .map(child -> child.getScenario().get())
                                .anyMatch(scenario -> {
                                    List<String> tags = scenario.getTags().stream()
                                            .map(Tag::getName)
                                            .map(String::toLowerCase)
                                            .collect(Collectors.toList());
                                    return tags.contains("@smoketest") || tags.contains("@regressiontest");
                                });

                        if (!anyScenarioHasTag) {
                            int line = feature.getLocation() != null ? feature.getLocation().getLine() : 1;

                            NewIssue newIssue = context.newIssue()
                                    .forRule(FeatureTagRulesDefinition.REPOSITORY_KEY + ":" + FeatureTagRulesDefinition.RULE_KEY)
                                    .at(newIssueLocation(context, featureFile, line))
                                    .message("El archivo debe contener al menos un Scenario o Scenario Outline con tag @smokeTest o @regressionTest correctamente escritos.");
                            newIssue.save();
                        }
                    }
                }
            }
        }
    }

    private NewIssueLocation newIssueLocation(SensorContext context, InputFile file, int line) {
        return context.newIssueLocation()
                .on(file)
                .at(file.selectLine(line))
                .message("Falta al menos un tag @smokeTest o @regressionTest en algún Scenario o Scenario Outline.");
    }
}
