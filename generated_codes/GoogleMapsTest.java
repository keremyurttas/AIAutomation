import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleMapsTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final Logger LOGGER = Logger.getLogger(GoogleMapsTest.class.getName());

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(35));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(35));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(35));
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testGoogleMaps() {
        try {
            // 1. Open Google homepage.
            driver.get("https://www.google.com");
            LOGGER.info("Opened Google homepage.");

            // 2. Click on the Google Apps menu (grid icon).
            WebElement googleAppsButton = findElement(null, "div.gb_md.gb_Td", "html/body/div/div/div/div/div/div/div[2]/div/div/div/a");
            clickElement(googleAppsButton, "Google Apps button");
            LOGGER.info("Clicked on the Google Apps menu.");

            // 3. Click on the Maps app icon.
            WebElement mapsAppIcon = findElement(null, "a.tX9u1b[href='https://maps.google.com/']", "html/body/div/c-wiz/div/div/c-wiz/div/div/div/div[2]/div/ul/li[3]/a");
            clickElement(mapsAppIcon, "Maps app icon");
            LOGGER.info("Clicked on the Maps app icon.");

            // 4. Verify Google Maps has loaded.
            wait.until(ExpectedConditions.urlContains("maps.google.com"));
            LOGGER.info("Verified Google Maps has loaded.");

            // 5. Type "coffee shops near me" in the search box.
            WebElement searchBox = findElement("searchboxinput", "input.searchboxinput.xiQnY", "html/body/div/div[3]/div[8]/div[3]/div/div/div/div/form/input");
            searchBox.sendKeys("coffee shops near me");
            LOGGER.info("Typed 'coffee shops near me' in the search box.");

            // 6. Press Enter or click the search button.
            WebElement searchButton = findElement("searchbox-searchbutton", "button.mL3xi[aria-label='Ara']", "html/body/div/div[3]/div[8]/div[3]/div/div/div/div/div/button");
            clickElement(searchButton, "Search button");
            LOGGER.info("Clicked the search button.");

            // 7. Verify map displays markers for coffee shops in the vicinity.
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div[jsaction*='mouseover:pane.focus']")));
            List<WebElement> coffeeShopMarkers = driver.findElements(By.cssSelector("div[jsaction*='mouseover:pane.focus']"));
            Assert.assertTrue(coffeeShopMarkers.size() > 0, "Map does not display markers for coffee shops.");
            LOGGER.info("Verified map displays markers for coffee shops in the vicinity.");

            LOGGER.info("Verified Google Maps functionality. Opened Google homepage, clicked on the Google Apps menu (grid icon), clicked on the Maps app icon, typed 'coffee shops near me' in the search box, pressed Enter or clicked the search button, and verified map displays markers for coffee shops in the vicinity.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            captureScreenshot(driver, "testFailure");
            Assert.fail("Test failed: " + e.getMessage());
        }
    }

    private WebElement findElement(String id, String cssSelector, String xpath) {
        WebElement element = null;
        try {
            if (id != null && !id.isEmpty()) {
                element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
                LOGGER.info("Found element by ID: " + id);
                return element;
            }
            if (cssSelector != null && !cssSelector.isEmpty()) {
                element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));
                LOGGER.info("Found element by CSS Selector: " + cssSelector);
                return element;
            }
            if (xpath != null && !xpath.isEmpty()) {
                element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
                LOGGER.info("Found element by XPath: " + xpath);
                return element;
            }
        } catch (Exception e) {
            LOGGER.warning("Element not found using ID, CSS, or XPath: " + e.getMessage());
        }
        return element;
    }

    private void clickElement(WebElement element, String elementName) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            LOGGER.info("Clicked on " + elementName);
        } catch (Exception e) {
            LOGGER.warning("Failed to click on " + elementName + " using standard click: " + e.getMessage());
            try {
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                executor.executeScript("arguments[0].click();", element);
                LOGGER.info("Clicked on " + elementName + " using JavaScript executor.");
            } catch (Exception ex) {
                LOGGER.severe("Failed to click on " + elementName + " even with JavaScript executor: " + ex.getMessage());
                throw new RuntimeException("Failed to click on " + elementName + ": " + ex.getMessage());
            }
        }
    }


    @AfterMethod
    public void teardown(ITestResult result) {
        if (driver != null) {
            driver.quit();
            LOGGER.info("Driver quit.");
        }
    }

    private void captureScreenshot(WebDriver driver, String screenshotName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            java.io.File source = ts.getScreenshotAs(OutputType.FILE);
            java.io.File destination = new java.io.File(screenshotName + ".png");
            org.apache.commons.io.FileUtils.copyFile(source, destination);
            LOGGER.info("Screenshot taken: " + destination.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.severe("Failed to capture screenshot: " + e.getMessage());
        }
    }

    public static class RetryAnalyzer implements IRetryAnalyzer {
        private int retryCount = 0;
        private static final int maxRetryCount = 3;

        @Override
        public boolean retry(ITestResult result) {
            if (retryCount < maxRetryCount) {
                retryCount++;
                LOGGER.warning("Retrying test " + result.getName() + " - Retry count: " + retryCount);
                return true;
            }
            return false;
        }
    }
}