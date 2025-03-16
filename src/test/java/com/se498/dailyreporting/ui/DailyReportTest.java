package com.se498.dailyreporting.ui;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium tests for Daily Report functionality
 */
public class DailyReportTest extends BaseSeleniumTest {

    private String createTestReport() {

        // Now navigate to the create report page
        navigateTo("/ui/reports/new");

        // Take screenshot before filling the form
        takeScreenshot("before-create-report-form");

        // Wait for page to load completely
        try {
            Thread.sleep(1500); // Give the page a moment to render completely
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Dump the page source for debugging
        System.out.println("*** PAGE SOURCE - REPORT FORM ***");
        System.out.println(driver.getPageSource());
        System.out.println("*** END PAGE SOURCE ***");

        // Fill in the report form
        WebElement projectIdField = waitAndFind(By.id("projectId"));
        projectIdField.sendKeys("TEST-PROJECT-" + System.currentTimeMillis());

        WebElement reportDateField = waitAndFind(By.id("reportDate"));
        setDateField(reportDateField, getTodayDate());

        WebElement notesField = waitAndFind(By.id("notes"));
        notesField.sendKeys("This is a test report created by Selenium.");

        // Take screenshot after filling the form
        takeScreenshot("after-filling-report-form");

        // Try multiple ways to find the create report button
        WebElement createButton = null;

        // Method 1: By text content
        if (doesElementExist(By.xpath("//button[contains(text(), 'Create Report')]"))) {
            createButton = driver.findElement(By.xpath("//button[contains(text(), 'Create Report')]"));
        }
        // Method 2: By button with specific classes
        else if (doesElementExist(By.cssSelector("button.btn.btn-success"))) {
            createButton = driver.findElement(By.cssSelector("button.btn.btn-success"));
        }
        // Method 3: By icon inside button
        else if (doesElementExist(By.cssSelector("button i.fas.fa-plus"))) {
            createButton = driver.findElement(By.cssSelector("button i.fas.fa-plus")).findElement(By.xpath("./.."));
        }
        // Method 4: Any submit button inside the form
        else if (doesElementExist(By.cssSelector("form[action='/ui/reports/new'] button[type='submit']"))) {
            createButton = driver.findElement(By.cssSelector("form[action='/ui/reports/new'] button[type='submit']"));
        }

        if (createButton == null) {
            // If we still can't find it, try a more general approach - find all buttons and try to identify the right one
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            for (WebElement button : buttons) {
                String buttonText = button.getText().trim();
                System.out.println("Found button: " + buttonText);
                if (buttonText.contains("Create") || buttonText.contains("Save") || buttonText.contains("Submit")) {
                    createButton = button;
                    break;
                }
            }
        }

        // If we still can't find the button, try using JavaScript to submit the form
        if (createButton == null) {
            System.out.println("Could not find Create button using standard methods. Trying to submit form using JavaScript.");
            WebElement form = driver.findElement(By.cssSelector("form[action='/ui/reports/new']"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", form);
        } else {
            // Submit the form by clicking the button
            createButton.click();
        }

        // Wait for redirect to the report view page
        wait.until(ExpectedConditions.urlContains("/ui/reports/"));

        // Take screenshot after redirect
        takeScreenshot("after-report-creation");

        // Extract the report ID from the URL
        String currentUrl = driver.getCurrentUrl();
        String reportId = currentUrl.substring(currentUrl.lastIndexOf('/') + 1);

        return reportId;
    }

    @Test
    public void testReportListPage() {

        navigateTo("/ui/reports");

        // Verify the search form is present
        WebElement searchForm = waitAndFind(By.cssSelector("form[action='/ui/reports']"));
        assertTrue(searchForm.isDisplayed(), "Search form should be displayed");

        // Verify the filter fields
        assertTrue(doesElementExist(By.id("projectId")), "Project ID filter should exist");
        assertTrue(doesElementExist(By.id("status")), "Status filter should exist");
        assertTrue(doesElementExist(By.id("startDate")), "Start date filter should exist");
        assertTrue(doesElementExist(By.id("endDate")), "End date filter should exist");

        // Verify the "New Report" button - try multiple possible selectors
        try {
            // Wait a bit longer for the UI to stabilize
            Thread.sleep(1000);

            // Try different selectors to find the New Report button
            WebElement newReportButton = null;

            if (doesElementExist(By.xpath("//a[contains(text(), 'New Report')]"))) {
                newReportButton = driver.findElement(By.xpath("//a[contains(text(), 'New Report')]"));
            } else if (doesElementExist(By.cssSelector("a.btn-success"))) {
                newReportButton = driver.findElement(By.cssSelector("a.btn-success"));
            } else if (doesElementExist(By.cssSelector("a[href*='/reports/new']"))) {
                newReportButton = driver.findElement(By.cssSelector("a[href*='/reports/new']"));
            }

            assertNotNull(newReportButton, "New Report button should be found on the page");
            assertTrue(newReportButton.isDisplayed(), "New Report button should be displayed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted: " + e.getMessage());
        }

        // Verify the status legend
        WebElement statusLegend = waitAndFind(By.xpath("//h5[text()='Report Status Legend']"));
        assertTrue(statusLegend.isDisplayed(), "Status legend should be displayed");
    }

    @Test
    public void testCreateAndViewReport() {
        // Create a new report
        String reportId = createTestReport();

        // Take a screenshot after report creation
        takeScreenshot("report-created");

        // Verify we're on the report view page
        WebElement reportHeader = waitAndFind(By.cssSelector(".card-header h3"));
        assertTrue(reportHeader.getText().contains("Daily Report"), "Header should indicate we're viewing a report");

        // Verify report details
        WebElement statusBadge = waitAndFind(By.cssSelector(".badge"));
        assertEquals("DRAFT", statusBadge.getText(), "New report should have DRAFT status");
    }
}