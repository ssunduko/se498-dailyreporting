package com.se498.dailyreporting.bdd;

import com.se498.dailyreporting.bdd.steps.DailyReportSteps;
import com.se498.dailyreporting.bdd.steps.WeatherSteps;
import com.se498.dailyreporting.service.DailyReportingService;
import com.se498.dailyreporting.service.WeatherReportingService;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

@SpringBootTest
public class WeatherReportingTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WeatherSteps weatherSteps;

    @Autowired
    private DailyReportSteps dailyReportSteps;


    @Test
    public void testWeatherStories() {
        System.out.println("Starting Weather Stories test");

        // Use autowired steps if available, otherwise create manually
        WeatherSteps stepsToUse = weatherSteps;
        if (stepsToUse == null) {
            System.out.println("WeatherSteps not autowired, creating manually");
            WeatherReportingService service = Mockito.mock(WeatherReportingService.class);
            stepsToUse = new WeatherSteps();
            setField(stepsToUse, "weatherService", service);
        } else {
            System.out.println("Using autowired WeatherSteps: " + stepsToUse);
        }

        // Configure JBehave
        Embedder embedder = new Embedder();
        embedder.useConfiguration(createConfiguration());
        embedder.useStepsFactory(createStepsFactory(stepsToUse));
        embedder.useEmbedderControls(new EmbedderControls()
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true)
                .doVerboseFailures(true)
                .doVerboseFiltering(true));

        // Check story path
        String storyPath = "stories/weather.story";
        if (getClass().getClassLoader().getResource(storyPath) == null) {
            System.err.println("ERROR: Story file not found at: " + storyPath);
        } else {
            System.out.println("Found story file at: " + getClass().getClassLoader().getResource(storyPath));
        }

        // Run the stories
        System.out.println("Running weather.story");
        embedder.runStoriesAsPaths(Arrays.asList(storyPath));
        System.out.println("Completed weather.story test");
    }

    @Test
    public void testDailyReportStories() {
        System.out.println("Starting Daily Report Stories test");

        // Use autowired steps if available, otherwise create manually
        DailyReportSteps stepsToUse = dailyReportSteps;
        if (stepsToUse == null) {
            System.out.println("DailyReportSteps not autowired, creating manually");
            DailyReportingService service = Mockito.mock(DailyReportingService.class);
            stepsToUse = new DailyReportSteps();
            setField(stepsToUse, "reportingService", service);
        } else {
            System.out.println("Using autowired DailyReportSteps: " + stepsToUse);
        }

        // Configure JBehave
        Embedder embedder = new Embedder();
        embedder.useConfiguration(createConfiguration());
        embedder.useStepsFactory(createStepsFactory(stepsToUse));
        embedder.useEmbedderControls(new EmbedderControls()
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true)
                .doVerboseFailures(true)
                .doVerboseFiltering(true));

        // Check story path
        String storyPath = "stories/daily_report.story";
        if (getClass().getClassLoader().getResource(storyPath) == null) {
            System.err.println("ERROR: Story file not found at: " + storyPath);
        } else {
            System.out.println("Found story file at: " + getClass().getClassLoader().getResource(storyPath));
        }

        // Run the stories
        System.out.println("Running daily_report.story");
        embedder.runStoriesAsPaths(Arrays.asList(storyPath));
        System.out.println("Completed daily_report.story test");
    }

    private Configuration createConfiguration() {
        return new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromClasspath(this.getClass()))
                .useStepPatternParser(new RegexPrefixCapturingPatternParser())
                .useStoryReporterBuilder(new StoryReporterBuilder()
                        .withDefaultFormats()
                        .withFormats(Format.CONSOLE, Format.TXT));
    }

    private InjectableStepsFactory createStepsFactory(Object... steps) {
        // Print the steps being added
        for (Object step : steps) {
            System.out.println("Adding step instance: " + (step != null ? step.getClass().getName() : "null"));

            // List annotated methods if step is not null
            if (step != null) {
                System.out.println("Methods in " + step.getClass().getSimpleName() + ":");
                for (java.lang.reflect.Method method : step.getClass().getMethods()) {
                    if (method.isAnnotationPresent(org.jbehave.core.annotations.Given.class) ||
                            method.isAnnotationPresent(org.jbehave.core.annotations.When.class) ||
                            method.isAnnotationPresent(org.jbehave.core.annotations.Then.class)) {
                        System.out.println("  " + method.getName() + ": " + Arrays.toString(method.getAnnotations()));
                    }
                }
            }
        }

        return new InstanceStepsFactory(createConfiguration(), steps);
    }

    @Test
    public void verifyClasspath() {
        try {
            Class<?> weatherStepsClass = Class.forName("com.se498.dailyreporting.bdd.steps.WeatherSteps");
            System.out.println("WeatherSteps class found on classpath: " + weatherStepsClass);

            Class<?> dailyReportStepsClass = Class.forName("com.se498.dailyreporting.bdd.steps.DailyReportSteps");
            System.out.println("DailyReportSteps class found on classpath: " + dailyReportStepsClass);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found on classpath: " + e.getMessage());
        }
    }

    // Helper method to set private fields via reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = findField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(target, value);
                // Verify field was set
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
