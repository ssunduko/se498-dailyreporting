package com.se498.dailyreporting.config;

import com.se498.dailyreporting.bdd.steps.DailyReportSteps;
import com.se498.dailyreporting.bdd.steps.WeatherSteps;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryLoader;
import org.jbehave.core.io.UnderscoredCamelCaseResolver;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Base configuration for JBehave BDD tests
 */
@Component
@ComponentScan(basePackages = {"com.se498.dailyreporting.bdd.steps"})
public abstract class JBehaveConfiguration extends JUnitStories {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private DailyReportSteps dailyReportSteps;

    @Autowired(required = false)
    private WeatherSteps weatherSteps;

    public JBehaveConfiguration() {
        // Enable verbose output for debugging
        System.setProperty("org.jbehave.core.embedder.Embedder.verboseFailures", "true");
        System.setProperty("org.jbehave.core.embedder.Embedder.verboseFiltering", "true");

        Embedder embedder = configuredEmbedder();
        embedder.useEmbedderControls(
                new EmbedderControls()
                        .doIgnoreFailureInStories(true)
                        .doIgnoreFailureInView(true)
                        .doVerboseFailures(true)
                        .doVerboseFiltering(true)
        );
    }

    @Override
    public Configuration configuration() {
        // Using RegexPrefixCapturingPatternParser for better step matching
        return new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromClasspath(this.getClass()))
                .useStoryParser(new RegexStoryParser())
                .useStoryPathResolver(new UnderscoredCamelCaseResolver())
                .useStepPatternParser(new RegexPrefixCapturingPatternParser())
                .useParameterConverters(new ParameterConverters())
                .useStoryReporterBuilder(
                        new StoryReporterBuilder()
                                .withDefaultFormats()
                                .withFormats(Format.CONSOLE, Format.TXT, Format.HTML)
                );
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        // Debug information
        System.out.println("Creating steps factory with DailyReportSteps: " + dailyReportSteps);
        System.out.println("Creating steps factory with WeatherSteps: " + weatherSteps);

        List<Object> stepInstances = new ArrayList<>();
        if (dailyReportSteps != null) {
            stepInstances.add(dailyReportSteps);
        }
        if (weatherSteps != null) {
            stepInstances.add(weatherSteps);
        }

        return new InstanceStepsFactory(configuration(), stepInstances.toArray());
    }

    // Debug method to print step information
    protected void printStepInfo(Object stepInstance) {
        if (stepInstance == null) {
            System.out.println("Step instance is null");
            return;
        }

        System.out.println("Step class: " + stepInstance.getClass().getName());

        // Print all method annotations
        for (java.lang.reflect.Method method : stepInstance.getClass().getMethods()) {
            org.jbehave.core.annotations.Given given = method.getAnnotation(org.jbehave.core.annotations.Given.class);
            org.jbehave.core.annotations.When when = method.getAnnotation(org.jbehave.core.annotations.When.class);
            org.jbehave.core.annotations.Then then = method.getAnnotation(org.jbehave.core.annotations.Then.class);

            if (given != null) {
                System.out.println("  @Given(\"" + given.value() + "\") -> " + method.getName());
            }
            if (when != null) {
                System.out.println("  @When(\"" + when.value() + "\") -> " + method.getName());
            }
            if (then != null) {
                System.out.println("  @Then(\"" + then.value() + "\") -> " + method.getName());
            }
        }
    }
}