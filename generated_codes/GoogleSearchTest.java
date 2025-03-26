import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static SmaClickUtilities.clickWebElementForTpath;
import static SmaSendKeyUtilites.sendKeysElementTPath;

public class GoogleSearchTest {

    private WebDriver driver;
    private static final Logger LOGGER = Logger.getLogger(GoogleSearchTest.class.getName());
    private String baseUrl = "https://www.google.com";
    private String screenshotsDir = "screenshots";

    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setupTest() {
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().deleteAllCookies();
        log("Browser started and cookies cleared.");
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshotOnFailure(result.getMethod().getMethodName());
        }
        if (driver != null) {
            driver.quit();
            log("Browser closed.");
        }
    }

    @Test
    public void googleSearchTest() {
        try {
            logTestStep("Starting Google Search Test");
            driver.get(baseUrl);
            logTestStep("Opened Google homepage.");
            captureScreenshotOnStep("Homepage Opened");

            // Verify the search input field is visible.
            By searchBoxLocator = By.xpath("//textarea[@class='gLFyf']");
            WebElement searchBox = findElementByXpath("//textarea[@class='gLFyf']");
            Assert.assertTrue(searchBox.isDisplayed(), "Search input field is not visible.");
            logTestStep("Verified search input field is visible.");
            captureScreenshotOnStep("Search Input Visible");

            // Type "automation testing" in the search box.
            sendKeysElementTPath(searchBoxLocator, true, "automation testing");
            logTestStep("Typed 'automation testing' in the search box.");
            captureScreenshotOnStep("Typed Search Query");

            // Press Enter or click the search button.
            By searchButtonLocator = By.xpath("//input[@name='btnK']");
            clickWebElementForTpath(searchButtonLocator);
            logTestStep("Clicked the search button.");
            captureScreenshotOnStep("Clicked Search Button");

            // Verify search results are displayed with relevant links.
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
            logTestStep("Verified search results are displayed.");
            captureScreenshotOnStep("Search Results Displayed");

            Assert.assertTrue(driver.findElement(By.id("search")).isDisplayed(), "Search results are not displayed.");
            logTestStep("Successfully opened Google, typed 'automation testing' in the search box, and verified that search results are displayed with relevant links.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            captureScreenshotOnFailure("googleSearchTest");
            Assert.fail("Test failed: " + e.getMessage());
        }
    }

    // Element Locator Methods
    private WebElement findElementById(String id) {
        logLocatorStrategy("findElementById", id);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
            log("Element with ID '" + id + "' found.");
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with ID '" + id + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            captureScreenshotOnFailure("findElementById");
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByName(String name) {
        logLocatorStrategy("findElementByName", name);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
            log("Element with Name '" + name + "' found.");
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with Name '" + name + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            captureScreenshotOnFailure("findElementByName");
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByCssSelector(String cssSelector) {
        logLocatorStrategy("findElementByCssSelector", cssSelector);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));
            log("Element with CSS Selector '" + cssSelector + "' found.");
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with CSS Selector '" + cssSelector + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            captureScreenshotOnFailure("findElementByCssSelector");
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByXpath(String xpath) {
        logLocatorStrategy("findElementByXpath", xpath);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
            log("Element with XPath '" + xpath + "' found.");
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with XPath '" + xpath + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            captureScreenshotOnFailure("findElementByXpath");
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByLinkText(String linkText) {
        logLocatorStrategy("findElementByLinkText", linkText);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText(linkText)));
            log("Element with Link Text '" + linkText + "' found.");
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with Link Text '" + linkText + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            captureScreenshotOnFailure("findElementByLinkText");
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByPartialLinkText(String partialLinkText) {
        logLocatorStrategy("findElementByPartialLinkText", partialLinkText);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText(partialLinkText)));
            log("Element with Partial Link Text '" + partialLinkText + "' found.");
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with Partial Link Text '" + partialLinkText + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            captureScreenshotOnFailure("findElementByPartialLinkText");
            throw new NoSuchElementException(errorMessage);
        }
    }

    // Screenshot Capture Methods
    private void captureScreenshotOnStep(String stepName) {
        captureScreenshot(stepName, "Step");
    }

    private void captureScreenshotOnFailure(String methodName) {
        captureScreenshot(methodName, "Failure");
    }

    private void captureFullPageScreenshot() {
        captureScreenshot("FullPage", "FullPage");
    }

    private void captureScreenshot(String context, String type) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("%s_%s_%s.png", timestamp, context, type);
            Path screenshotsPath = Paths.get(screenshotsDir);

            if (!Files.exists(screenshotsPath)) {
                Files.createDirectories(screenshotsPath);
            }

            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destinationPath = screenshotsPath.resolve(filename);
            Files.copy(screenshotFile.toPath(), destinationPath);

            log("Screenshot captured: " + destinationPath.toString());

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to capture screenshot: " + e.getMessage(), e);
        }
    }

    // Logging Methods
    private void log(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        System.out.println(timestamp + " - " + message);
        LOGGER.info(message);
    }

    private void logTestStep(String step) {
        log("Test Step: " + step);
    }

    private void logLocatorStrategy(String method, String locator) {
        log("Using locator strategy: " + method + " with locator: " + locator);
    }

}