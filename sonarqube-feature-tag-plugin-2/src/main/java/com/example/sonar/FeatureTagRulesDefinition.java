package com.example.sonar;

import org.sonar.api.server.rule.RulesDefinition;

public class FeatureTagRulesDefinition implements RulesDefinition {

    public static final String REPOSITORY_KEY = "feature-tag-repo";
    public static final String RULE_KEY = "missing-smoke-or-regression-tag";

    @Override
    public void define(Context context) {
        NewRepository repository = context.createRepository(REPOSITORY_KEY, "feature");
        repository.setName("Feature Tag Rules");

        repository.createRule(RULE_KEY)
                .setName("Archivo debe tener al menos un Scenario con tag @smokeTest o @regressionTest")
                .setHtmlDescription("Cada archivo .feature debe contener al menos un Scenario o Scenario Outline con al menos uno de los tags @smokeTest o @regressionTest correctamente escritos.")
                .setTags("convention", "cucumber", "gherkin")
                .setSeverity(org.sonar.api.rule.Severity.MAJOR);

        repository.done();
    }
}
