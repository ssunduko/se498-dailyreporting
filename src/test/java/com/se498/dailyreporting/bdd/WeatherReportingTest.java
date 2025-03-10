package com.se498.dailyreporting.bdd;

import com.se498.dailyreporting.bdd.steps.DailyReportSteps;
import com.se498.dailyreporting.bdd.steps.WeatherSteps;
import com.se498.dailyreporting.service.DailyReportingService;
import com.se498.dailyreporting.service.WeatherReportingService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class WeatherReportingTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WeatherSteps weatherSteps;

    @Autowired
    private DailyReportSteps dailyReportSteps;


    @Test
    public void testWeatherStories() {
        log.info("Starting Weather Stories test");

        // Use autowired steps if available, otherwise create manually
        WeatherSteps stepsToUse = weatherSteps;
        if (stepsToUse == null) {
            log.info("WeatherSteps not autowired, creating manually");
            WeatherReportingService service = Mockito.mock(WeatherReportingService.class);
            stepsToUse = new WeatherSteps();
            setField(stepsToUse, "weatherService", service);
        } else {
            log.info("Using autowired WeatherSteps: {}", stepsToUse);
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
            log.error("Story file not found at: {}", storyPath);
        } else {
            log.info("Found story file at: {}", getClass().getClassLoader().getResource(storyPath));
        }

        // Run the stories
        log.info("Running weather.story");
        embedder.runStoriesAsPaths(Arrays.asList(storyPath));
        log.info("Completed weather.story test");
    }

    @Test
    public void testDailyReportStories() {
        log.info("Starting Daily Report Stories test");

        // Use autowired steps if available, otherwise create manually
        DailyReportSteps stepsToUse = dailyReportSteps;
        if (stepsToUse == null) {
            log.info("DailyReportSteps not autowired, creating manually");
            DailyReportingService service = Mockito.mock(DailyReportingService.class);
            stepsToUse = new DailyReportSteps();
            setField(stepsToUse, "reportingService", service);
        } else {
            log.info("Using autowired DailyReportSteps: {}", stepsToUse);
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
            log.error("Story file not found at: {}", storyPath);
        } else {
            log.info("Found story file at: {}", getClass().getClassLoader().getResource(storyPath));
        }

        // Run the stories
        log.info("Running daily_report.story");
        embedder.runStoriesAsPaths(Arrays.asList(storyPath));
        log.info("Completed daily_report.story test");
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
            log.info("Adding step instance: {}", (step != null ? step.getClass().getName() : "null"));

            // List annotated methods if step is not null
            if (step != null) {
                log.info("Methods in {}:", step.getClass().getSimpleName());
                for (java.lang.reflect.Method method : step.getClass().getMethods()) {
                    if (method.isAnnotationPresent(org.jbehave.core.annotations.Given.class) ||
                            method.isAnnotationPresent(org.jbehave.core.annotations.When.class) ||
                            method.isAnnotationPresent(org.jbehave.core.annotations.Then.class)) {
                        log.info("  {}: {}", method.getName(), Arrays.toString(method.getAnnotations()));
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
            log.info("WeatherSteps class found on classpath: {}", weatherStepsClass);

            Class<?> dailyReportStepsClass = Class.forName("com.se498.dailyreporting.bdd.steps.DailyReportSteps");
            log.info("DailyReportSteps class found on classpath: {}", dailyReportStepsClass);
        } catch (ClassNotFoundException e) {
            log.error("Class not found on classpath: {}", e.getMessage());
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
                log.info("Successfully set field {} to {}", fieldName, value);
            } else {
                log.error("Could not find field {} in class {}", fieldName, target.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Could not set field {}: {}", fieldName, e.getMessage());
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
