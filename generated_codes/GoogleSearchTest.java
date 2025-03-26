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
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static SmaClickUtilities.clickWebElementForTpath;
import static SmaSendKeyUtilites.sendKeysElementTPath;

public class GoogleSearchTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final Logger logger = Logger.getLogger(GoogleSearchTest.class.getName());
    private static final String SCREENSHOTS_DIR = "screenshots";

    @BeforeSuite
    public void setupSuite() {
        // Configure logger
        configureLogger();

        // Create screenshots directory if it doesn't exist
        File dir = new File(SCREENSHOTS_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                logger.info("Screenshots directory created: " + SCREENSHOTS_DIR);
            } else {
                logger.severe("Failed to create screenshots directory: " + SCREENSHOTS_DIR);
            }
        }
    }

    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setupTest() {
        logTestStep("Starting test setup");
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().deleteAllCookies();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        logTestStep("Test setup complete");
    }

    @Test
    public void googleSearchTest() {
        try {
            logTestStep("Opening Google homepage");
            driver.get("https://www.google.com");
            captureScreenshotOnStep("Google homepage opened");

            logTestStep("Verifying search input field is visible");
            WebElement searchInput = findElementByXpath("//textarea[@id='APjFqb']");
            Assert.assertTrue(searchInput.isDisplayed(), "Search input field is not visible");
            captureScreenshotOnStep("Search input field verified");

            logTestStep("Typing 'automation testing' in the search box");
            sendKeysElementTPath(By.xpath("//textarea[@id='APjFqb']"), false, "automation testing");
            captureScreenshotOnStep("Typed 'automation testing' in the search box");

            logTestStep("Clicking the search button");
            clickWebElementForTpath(By.xpath("//input[@name='btnK']"));
            captureScreenshotOnStep("Clicked the search button");

            logTestStep("Verifying search results are displayed with relevant links");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search")));
            Assert.assertTrue(driver.findElement(By.id("search")).isDisplayed(), "Search results are not displayed");
            captureScreenshotOnStep("Search results verified");

            logTestStep("Test completed successfully");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            captureScreenshotOnFailure("googleSearchTest");
            Assert.fail("Test failed: " + e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        logTestStep("Starting test teardown");
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshotOnFailure(result.getMethod().getMethodName());
        }
        if (driver != null) {
            driver.quit();
        }
        logTestStep("Test teardown complete");
    }

    // Locator Methods
    private WebElement findElementById(String id) {
        logLocatorStrategy("findElementById", id);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
            logger.info("Element found with id: " + id);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with id '" + id + "' not found: " + e.getMessage();
            logger.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByName(String name) {
        logLocatorStrategy("findElementByName", name);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
            logger.info("Element found with name: " + name);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with name '" + name + "' not found: " + e.getMessage();
            logger.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByCssSelector(String cssSelector) {
        logLocatorStrategy("findElementByCssSelector", cssSelector);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));
            logger.info("Element found with CSS selector: " + cssSelector);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with CSS selector '" + cssSelector + "' not found: " + e.getMessage();
            logger.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByXpath(String xpath) {
        logLocatorStrategy("findElementByXpath", xpath);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
            logger.info("Element found with xpath: " + xpath);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with xpath '" + xpath + "' not found: " + e.getMessage();
            logger.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByLinkText(String linkText) {
        logLocatorStrategy("findElementByLinkText", linkText);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText(linkText)));
            logger.info("Element found with link text: " + linkText);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with link text '" + linkText + "' not found: " + e.getMessage();
            logger.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByPartialLinkText(String partialLinkText) {
        logLocatorStrategy("findElementByPartialLinkText", partialLinkText);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText(partialLinkText)));
            logger.info("Element found with partial link text: " + partialLinkText);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with partial link text '" + partialLinkText + "' not found: " + e.getMessage();
            logger.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    // Screenshot Methods
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
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = SCREENSHOTS_DIR + "/" + type + "_" + context + "_" + timestamp + ".png";

        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            Path destination = Paths.get(filename);
            Files.copy(source.toPath(), destination);
            logger.info("Screenshot captured: " + filename);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to capture screenshot: " + e.getMessage(), e);
        }
    }

    // Logging Methods
    private void logTestStep(String message) {
        logger.info(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + message);
    }

    private void logLocatorStrategy(String strategy, String locator) {
        logger.info("Using locator strategy: " + strategy + " with locator: " + locator);
    }

    private void configureLogger() {
        // Set logger level
        logger.setLevel(Level.INFO);

        // Console handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        logger.addHandler(consoleHandler);

        // File handler
        try {
            FileHandler fileHandler = new FileHandler("test.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create log file handler: " + e.getMessage(), e);
        }

        // Disable parent handlers
        logger.setUseParentHandlers(false);
    }

    // Modal and Overlay Handling (Example - Adapt to your specific needs)
    private void handleModal() {
        try {
            // Example: Check for a modal and close it
            WebElement modalCloseButton = driver.findElement(By.cssSelector(".modal-close-button"));
            if (modalCloseButton.isDisplayed()) {
                logTestStep("Modal detected. Closing modal.");
                modalCloseButton.click();
                captureScreenshotOnStep("Modal closed");
            }
        } catch (NoSuchElementException e) {
            // No modal found
            logger.info("No modal found.");
        }
    }
}