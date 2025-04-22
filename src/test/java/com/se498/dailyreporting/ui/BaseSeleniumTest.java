package com.se498.dailyreporting.ui;


import com.se498.dailyreporting.TestDailyReportingApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base class for Selenium tests with common setup and utility methods
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {TestDailyReportingApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseSeleniumTest {

    @LocalServerPort
    protected int port;

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        // Set up ChromeDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();

        // Set up ChromeDriver with headless option
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        // Add more stability options
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-gpu");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-infobars");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15)); // Increased timeout
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Navigate to a specific page in the application
     * @param path the path to navigate to
     */
    protected void navigateTo(String path) {
        driver.get("http://localhost:" + port + path);

        // Wait for page to fully load
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
    }

    /**
     * Wait for an element to be visible and clickable
     * @param by the locator for the element
     * @return the WebElement
     */
    protected WebElement waitAndFind(By by) {
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(by));
        } catch (TimeoutException e) {
            // Take a screenshot to help debug
            takeScreenshot("element-not-found-" + by.toString().replaceAll("[^a-zA-Z0-9]", "_"));

            // Output the page source for debugging
            System.err.println("=== PAGE SOURCE ===");
            System.err.println(driver.getPageSource());
            System.err.println("===================");

            // Re-throw the exception
            throw e;
        }
    }

    /**
     * Take a screenshot and save it to the "screenshots" directory
     * @param fileName base filename for the screenshot
     */
    protected void takeScreenshot(String fileName) {
        try {
            // Create screenshots directory if it doesn't exist
            Path screenshotsDir = Paths.get("target", "screenshots");
            if (!Files.exists(screenshotsDir)) {
                Files.createDirectories(screenshotsDir);
            }

            // Take screenshot
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            // Generate unique filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path destination = screenshotsDir.resolve(fileName + "_" + timestamp + ".png");

            // Save screenshot
            Files.copy(screenshot.toPath(), destination);
            System.out.println("Screenshot saved to: " + destination);
        } catch (IOException e) {
            System.err.println("Failed to save screenshot: " + e.getMessage());
        }
    }


    /**
     * Select value from a dropdown by its visible text
     * @param selectElement the select element
     * @param text the text to select
     */
    protected void selectByVisibleText(WebElement selectElement, String text) {
        Select select = new Select(selectElement);
        select.selectByVisibleText(text);
    }

    /**
     * Get today's date in yyyy-MM-dd format
     * @return today's date as string
     */
    protected String getTodayDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * Set a value in a date input field
     * @param dateField the date input field
     * @param dateStr the date string in yyyy-MM-dd format
     */
    protected void setDateField(WebElement dateField, String dateStr) {
        // Date fields sometimes need special handling in different browsers
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1]", dateField, dateStr);
    }

    /**
     * Check if an element exists on the page
     * @param by the locator for the element
     * @return true if the element exists, false otherwise
     */
    protected boolean doesElementExist(By by) {
        try {
            return !driver.findElements(by).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}