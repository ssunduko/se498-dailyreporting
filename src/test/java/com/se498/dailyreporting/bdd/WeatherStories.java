package com.se498.dailyreporting.bdd;

import com.se498.dailyreporting.config.JBehaveConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.StoryFinder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * JBehave story runner for Weather Service related stories
 */
@Component
public class WeatherStories extends JBehaveConfiguration {

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(
                CodeLocations.codeLocationFromClass(this.getClass()),
                Arrays.asList("**/stories/weather.story"),
                Arrays.asList(""));
    }
}
