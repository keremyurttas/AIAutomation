import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleAdvancedSearchTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private Actions actions;
    private static final Logger LOGGER = Logger.getLogger(GoogleAdvancedSearchTest.class.getName());

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        actions = new Actions(driver);
        LOGGER.setLevel(Level.INFO); // Set the desired logging level
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testGoogleAdvancedSearch() {
        try {
            // 1. Open Google homepage
            driver.get("https://www.google.com/");
            LOGGER.info("Opened Google homepage");

            // 2. Click on Settings near the bottom-right
            WebElement settingsButton = findElement(By.xpath("//div[contains(text(),'Settings')]"));
            clickElement(settingsButton, "Settings button");

            // 3. Click on "Advanced search" option
            WebElement advancedSearchOption = findElement(By.xpath("//a[contains(text(),'Advanced Search')]"));
            clickElement(advancedSearchOption, "Advanced search option");

            // 4. Verify advanced search page has loaded
            Assert.assertTrue(driver.getTitle().contains("Advanced Search"), "Advanced search page did not load.");
            LOGGER.info("Advanced search page loaded successfully.");

            // 5. Fill in "all these words" with "automation testing"
            WebElement allTheseWordsInput = findElement(By.id("xQOrbm"));
            clickElement(allTheseWordsInput, "All these words input");
            allTheseWordsInput.sendKeys("automation testing");
            LOGGER.info("Filled 'all these words' with 'automation testing'");

            // 6. Fill in "site or domain" with "github.com"
            WebElement siteOrDomainInput = findElement(By.id("hqSQbc"));
            clickElement(siteOrDomainInput, "Site or domain input");
            siteOrDomainInput.sendKeys("github.com");
            LOGGER.info("Filled 'site or domain' with 'github.com'");

            // 7. Click the Advanced Search button
            WebElement advancedSearchButton = findElement(By.xpath("//div[@class='jhpipd']/input[@class='gNO89b']"));
            clickElement(advancedSearchButton, "Advanced Search button");
            LOGGER.info("Clicked Advanced Search button");

            // 8. Verify search results are from github.com and contain "automation testing"
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
            List<WebElement> searchResults = driver.findElements(By.xpath("//div[@id='search']//a"));
            boolean allResultsValid = true;
            for (WebElement result : searchResults) {
                String url = result.getAttribute("href");
                if (url != null && !url.contains("github.com")) {
                    allResultsValid = false;
                    break;
                }
                if (result.getText() != null && !result.getText().toLowerCase().contains("automation testing")) {
                    allResultsValid = false;
                    break;
                }
            }
            Assert.assertTrue(allResultsValid, "Search results are not all from github.com and do not all contain 'automation testing'.");
            LOGGER.info("Verified search results are from github.com and contain 'automation testing'");

        } catch (Exception e) {
            LOGGER.severe("Test failed: " + e.getMessage());
            captureScreenshot(driver, "testFailure");
            throw e;
        }
    }

    private WebElement findElement(By by) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(by));
            return driver.findElement(by);
        } catch (Exception e) {
            LOGGER.warning("Element not found using: " + by.toString() + ". Error: " + e.getMessage());
            return null;
        }
    }

    private void clickElement(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            LOGGER.info("Clicked " + elementName);
        } catch (Exception e) {
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                LOGGER.info("Clicked " + elementName + " using JavaScript executor");
            } catch (Exception ex) {
                LOGGER.severe("Failed to click " + elementName + " using both regular click and JavaScript executor. Error: " + ex.getMessage());
                captureScreenshot(driver, "clickFailure_" + elementName);
                throw new RuntimeException("Failed to click " + elementName, ex);
            }
        }
    }

    private void captureScreenshot(WebDriver driver, String screenshotName) {
        try {
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filePath = "screenshots/" + screenshotName + "_" + timestamp + ".png";
            File destFile = new File(filePath);
            new File("screenshots").mkdirs(); // Ensure the directory exists
            Files.copy(screenshotFile.toPath(), destFile.toPath());
            LOGGER.info("Screenshot saved to: " + filePath);
        } catch (IOException e) {
            LOGGER.severe("Failed to capture screenshot: " + e.getMessage());
        }
    }

    public static class RetryAnalyzer implements IRetryAnalyzer {
        private int retryCount = 0;
        private static final int maxRetryCount = 3;

        @Override
        public boolean retry(ITestResult result) {
            if (retryCount < maxRetryCount) {
                LOGGER.info("Retrying test " + result.getName() + " attempt " + (retryCount + 1) + " of " + maxRetryCount);
                retryCount++;
                return true;
            }
            return false;
        }
    }
}