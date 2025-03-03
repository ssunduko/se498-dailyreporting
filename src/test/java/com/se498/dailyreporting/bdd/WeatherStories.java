package com.se498.dailyreporting.bdd;

import com.se498.dailyreporting.bdd.steps.WeatherSteps;
import com.se498.dailyreporting.config.JBehaveConfiguration;
import com.se498.dailyreporting.service.WeatherReportingService;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.mockito.Mockito;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * JBehave story runner for Weather Service related stories
 */
@Component
public class WeatherStories extends JBehaveConfiguration {

    private WeatherSteps weatherSteps;

    public WeatherStories() {
        System.out.println("WeatherStories constructor called");

        // Create steps manually instead of relying on autowiring
        weatherSteps = new WeatherSteps();
        WeatherReportingService weatherService = Mockito.mock(WeatherReportingService.class);
        setField(weatherSteps, "weatherService", weatherService);

        System.out.println("Created WeatherSteps manually: " + weatherSteps);
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        if (weatherSteps != null) {
            System.out.println("Using manually created WeatherSteps: " + weatherSteps);
            printStepInfo(weatherSteps);
            return new InstanceStepsFactory(configuration(), weatherSteps);
        } else {
            System.out.println("WARNING: weatherSteps is null, using empty steps factory");
            return new InstanceStepsFactory(configuration());
        }
    }

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromClasspath(this.getClass()))
                .useStepPatternParser(new RegexPrefixCapturingPatternParser())
                .useStoryReporterBuilder(
                        new StoryReporterBuilder()
                                .withDefaultFormats()
                                .withFormats(Format.CONSOLE, Format.TXT, Format.HTML)
                );
    }

    @Override
    public List<String> storyPaths() {
        // Print story paths for debugging
        List<String> paths = new StoryFinder().findPaths(
                CodeLocations.codeLocationFromClass(this.getClass()),
                Arrays.asList("**/stories/weather.story"),
                Arrays.asList(""));

        System.out.println("Story paths found: " + paths);

        // Verify story files exist
        for (String path : paths) {
            if (getClass().getClassLoader().getResource(path) == null) {
                System.err.println("WARNING: Story file not found at: " + path);
            } else {
                System.out.println("Found story file: " + path);
            }
        }

        return paths;
    }

    // Helper method to set private fields via reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = findField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(target, value);
                System.out.println("Successfully set field " + fieldName + " to " + value);
            } else {
                System.err.println("Could not find field " + fieldName + " in class " + target.getClass().getName());
            }
        } catch (Exception e) {
            System.err.println("Could not set field " + fieldName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        Class<?> searchType = clazz;
        while (searchType != null) {
            for (java.lang.reflect.Field field : searchType.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            searchType = searchType.getSuperclass();
        }
        return null;
    }
}
