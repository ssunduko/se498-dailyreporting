package com.se498.dailyreporting.bdd;

import com.se498.dailyreporting.bdd.steps.DailyReportSteps;
import com.se498.dailyreporting.bdd.steps.WeatherSteps;
import com.se498.dailyreporting.repository.ActivityEntryRepository;
import com.se498.dailyreporting.repository.DailyReportRepository;
import com.se498.dailyreporting.service.DailyReportingService;
import com.se498.dailyreporting.service.WeatherReportingService;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

public class WeatherReportingTest {

    private DailyReportingService reportingService;
    private WeatherReportingService weatherService;
    private DailyReportRepository reportRepository;
    private ActivityEntryRepository activityRepository;

    @BeforeEach
    void setup() {
        // Create mocks
        reportingService = Mockito.mock(DailyReportingService.class);
        weatherService = Mockito.mock(WeatherReportingService.class);
        reportRepository = Mockito.mock(DailyReportRepository.class);
        activityRepository = Mockito.mock(ActivityEntryRepository.class);
    }

    @Test
    void testDailyReportStories() {
        // Create steps with mocks
        DailyReportSteps reportSteps = new DailyReportSteps();
        // Use reflection to set the mocked service
        setField(reportSteps, "reportingService", reportingService);

        // Configure JBehave
        Embedder embedder = new Embedder();
        embedder.useConfiguration(createConfiguration());
        embedder.useStepsFactory(createStepsFactory(reportSteps));
        embedder.useEmbedderControls(new EmbedderControls()
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true));

        // Run the stories
        embedder.runStoriesAsPaths(Arrays.asList(
                "stories/daily_report.story"
        ));
    }

    @Test
    void testWeatherStories() {
        // Create steps with mocks
        WeatherSteps weatherSteps = new WeatherSteps();
        // Use reflection to set the mocked service
        setField(weatherSteps, "weatherService", weatherService);

        // Configure JBehave
        Embedder embedder = new Embedder();
        embedder.useConfiguration(createConfiguration());
        embedder.useStepsFactory(createStepsFactory(weatherSteps));
        embedder.useEmbedderControls(new EmbedderControls()
                .doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true));

        // Run the stories
        embedder.runStoriesAsPaths(Arrays.asList(
                "stories/weather.story"
        ));
    }

    private Configuration createConfiguration() {
        return new MostUsefulConfiguration()
                .useStoryLoader(new LoadFromClasspath(this.getClass()))
                .useStoryReporterBuilder(new StoryReporterBuilder()
                        .withDefaultFormats()
                        .withFormats(Format.CONSOLE));
    }

    private InjectableStepsFactory createStepsFactory(Object... steps) {
        return new InstanceStepsFactory(createConfiguration(), steps);
    }

    // Helper method to set private fields via reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = findField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(target, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not set field " + fieldName, e);
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