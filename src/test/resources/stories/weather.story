Meta:
@author Sergey Sundukovskiy

Narrative:
As a construction site manager
I want to get weather information for my job site
So that I can plan work activities accordingly and ensure safety

Scenario: Retrieving current weather by zip code
Given I am an authenticated user
When I request current weather for zip code "10001" in country "US"
Then I should receive valid weather information
And the response should include temperature, humidity and wind speed

Scenario: Retrieving current weather by city
Given I am an authenticated user
When I request current weather for city "New York" in country "US"
Then I should receive valid weather information
And the location information should match "New York, US"

Scenario: Getting weather alerts
Given I am an authenticated user
When I request weather alerts for zip code "10001"
Then I should receive a list of active weather alerts if any
And the response should include alert count and severity information

Scenario: Weather data caching
Given the weather service has cached data for zip code "10001"
When I request weather data for zip code "10001" within the cache validity period
Then the response should use cached data
And the response should indicate it came from cache

Scenario: Converting temperature from Fahrenheit to Celsius
Given I am an authenticated user
When I request to convert 32 degrees Fahrenheit to Celsius
Then the response should be 0 degrees Celsius

Scenario: Converting temperature from Celsius to Fahrenheit
Given I am an authenticated user
When I request to convert 100 degrees Celsius to Fahrenheit
Then the response should be 212 degrees Fahrenheit