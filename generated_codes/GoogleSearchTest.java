import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.IRetryAnalyzer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleSearchTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final int EXPLICIT_WAIT_TIMEOUT = 15;
    private final int RETRY_COUNT = 3;
    private final String GOOGLE_HOMEPAGE_URL = "https://www.google.com";
    private final String SEARCH_INPUT_ID = "APjFqb";
    private final String SEARCH_BUTTON_NAME = "btnK";
    private final String CAPTCHA_ELEMENT_ID = "captcha"; // Example ID, adjust as needed
    private final String SEARCH_RESULTS_LOCATOR = "//div[@id='search']//a"; // Example XPath, adjust as needed
    private final String SEARCH_INPUT_CSS = "textarea.gLFyf[title='Ara'][aria-label='Ara'][placeholder][autocomplete='off'][id='APjFqb'][name='q'][role='combobox']";
    private final String SEARCH_BUTTON_CSS = "input.gNO89b[aria-label*='Google\\'da Ara'][name='btnK'][role='button'][type='submit']";

    private Logger logger = Logger.getLogger(GoogleSearchTest.class.getName());

    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setupTest() {
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().deleteAllCookies();
        wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_TIMEOUT));
        logger.setLevel(Level.INFO);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(result.getName());
        }
        if (driver != null) {
            driver.quit();
        }
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void googleSearchTest() {
        try {
            // 1. Open Google homepage.
            driver.get(GOOGLE_HOMEPAGE_URL);
            logger.info("Opened Google homepage.");

            // 2. Verify the search input field is visible.
            WebElement searchInput = findElement(By.id(SEARCH_INPUT_ID), By.cssSelector(SEARCH_INPUT_CSS), null);
            Assert.assertTrue(searchInput.isDisplayed(), "Search input field is not visible.");
            logger.info("Verified the search input field is visible.");

            // 3. Type "automation testing" in the search box.
            searchInput.click(); // Simulate human behavior
            searchInput.sendKeys("automation testing");
            logger.info("Typed 'automation testing' in the search box.");

            // 4. Press Enter or click the search button.
            WebElement searchButton = findElement(By.name(SEARCH_BUTTON_NAME), By.cssSelector(SEARCH_BUTTON_CSS), null);
            clickElementWithFallback(searchButton);
            logger.info("Pressed Enter or clicked the search button.");

            // 5. Verify search results are displayed with relevant links.
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath(SEARCH_RESULTS_LOCATOR)));
            List<WebElement> searchResults = driver.findElements(By.xpath(SEARCH_RESULTS_LOCATOR));
            Assert.assertTrue(searchResults.size() > 0, "No search results displayed.");
            logger.info("Verified search results are displayed with relevant links.");

            // Additional verification: Check if at least one result contains "automation testing"
            boolean foundRelevantResult = false;
            for (WebElement result : searchResults) {
                if (result.getText().toLowerCase().contains("automation testing")) {
                    foundRelevantResult = true;
                    break;
                }
            }
            Assert.assertTrue(foundRelevantResult, "No relevant search results found.");
            logger.info("Verified at least one search result contains 'automation testing'.");

        } catch (Exception e) {
            logger.severe("Test failed: " + e.getMessage());
            throw e; // Re-throw the exception to mark the test as failed.
        }
    }

    private WebElement findElement(By byId, By byCss, By byXpath) {
        WebElement element = null;
        try {
            if (byId != null) {
                element = driver.findElement(byId);
                return element;
            }
        } catch (NoSuchElementException ignored) {
            // Try next locator
        }

        try {
            if (byCss != null) {
                element = driver.findElement(byCss);
                return element;
            }
        } catch (NoSuchElementException ignored) {
            // Try next locator
        }

        try {
            if (byXpath != null) {
                element = driver.findElement(byXpath);
                return element;
            }
        } catch (NoSuchElementException ignored) {
            // Element not found
        }

        throw new NoSuchElementException("Element not found using any of the provided locators: id=" + byId + ", css=" + byCss + ", xpath=" + byXpath);
    }

    private void clickElementWithFallback(WebElement element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
        } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
            logger.warning("Click failed, falling back to JavaScript click: " + e.getMessage());
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        }
    }

    private void handleModalsAndOverlays() {
        // Implement logic to check for and dismiss modals/overlays.
        // This is a placeholder, as the specific implementation depends on the website.
        // Example:
        try {
            WebElement modalCloseButton = driver.findElement(By.cssSelector(".modal-close-button"));
            if (modalCloseButton.isDisplayed()) {
                modalCloseButton.click();
                wait.until(ExpectedConditions.invisibilityOf(modalCloseButton));
                Thread.sleep(2000); // Wait for 2 seconds
            }
        } catch (NoSuchElementException | InterruptedException e) {
            // Modal not present or handling failed, continue.
        }
    }

    private boolean isCaptchaPresent() {
        try {
            driver.findElement(By.id(CAPTCHA_ELEMENT_ID));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private void captureScreenshot(String testName) {
        try {
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            File destinationFile = new File("screenshots/" + testName + "_" + timestamp + ".png");
            Files.createDirectories(destinationFile.getParentFile().toPath());
            Files.copy(scrFile.toPath(), destinationFile.toPath());
            logger.info("Screenshot saved to: " + destinationFile.getAbsolutePath());
        } catch (IOException e) {
            logger.severe("Failed to capture screenshot: " + e.getMessage());
        }
    }

    public static class RetryAnalyzer implements IRetryAnalyzer {
        private int retryCount = 0;

        @Override
        public boolean retry(ITestResult result) {
            if (retryCount < 3) {
                retryCount++;
                return true;
            }
            return false;
        }
    }
}