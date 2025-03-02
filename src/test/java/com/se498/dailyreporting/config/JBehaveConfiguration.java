package com.se498.dailyreporting.config;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.ParameterConverters;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;

/**
 * Base configuration for JBehave stories
 */
@SpringBootTest
public abstract class JBehaveConfiguration extends JUnitStories {


    public JBehaveConfiguration() {
        super();
        Embedder embedder = configuredEmbedder();
        embedder.embedderControls()
                .doGenerateViewAfterStories(true)
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(false)
                .useStoryTimeouts("300");
    }

    @Override
    public Configuration configuration() {
        // Register custom parameter converters
        ParameterConverters parameterConverters = new ParameterConverters();
        parameterConverters.addConverters(
                // Custom date converter for handling date parameters in stories
                new ParameterConverters.DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
                // Add other converters as needed
                new ParameterConverters.EnumConverter()
        );

        return new MostUsefulConfiguration()
                .useStoryControls(new StoryControls()
                        .doDryRun(false)
                        .doSkipScenariosAfterFailure(false))
                .useStoryLoader(new LoadFromClasspath(this.getClass()))
                .useStoryParser(new RegexStoryParser())
                .usePendingStepStrategy(new FailingUponPendingStep())
                .useParameterConverters(parameterConverters)
                .useStoryReporterBuilder(new StoryReporterBuilder()
                        .withCodeLocation(CodeLocations.codeLocationFromClass(this.getClass()))
                        .withDefaultFormats()
                        .withFormats(Format.CONSOLE, Format.TXT, Format.HTML, Format.XML));
    }

}