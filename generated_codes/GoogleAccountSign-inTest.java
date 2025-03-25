import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleAccountSign_inTest {

    private WebDriver driver;
    private static final Logger LOGGER = Logger.getLogger(GoogleAccountSign_inTest.class.getName());
    private static final String GOOGLE_HOMEPAGE = "https://www.google.com/";
    private static final String SIGN_IN_PAGE_TITLE = "Google Hesapları"; // Or a similar localized string
    private static final String EMAIL_INPUT_ID = "identifierId";
    private static final String NEXT_BUTTON_ID = "identifierNext";
    private static final String PASSWORD_INPUT_NAME = "Passwd"; // Or a similar localized string
    private static final String CAPTCHA_ELEMENT_ID = "captcha"; // Example ID, adjust as needed
    private static final int RETRY_COUNT = 3;
    private int retryAttempt = 0;

    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setupTest() {
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().deleteAllCookies();
        LOGGER.setLevel(Level.INFO);
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
    public void testGoogleSignIn() {
        try {
            // 1. Open Google homepage
            driver.get(GOOGLE_HOMEPAGE);
            LOGGER.info("Opened Google homepage: " + GOOGLE_HOMEPAGE);

            // 2. Click on the "Sign in" button
            WebElement signInButton = findElement("Sign in button", By.xpath("//a[contains(@href, 'ServiceLogin') and contains(., 'Oturum açın')]"), By.cssSelector("a.gb_Ua.gb_zd.gb_qd.gb_hd"), By.linkText("Sign in"));
            clickElement(signInButton, "Sign in button");
            LOGGER.info("Clicked on the Sign in button");

            // 3. Verify you are redirected to the Google sign-in page
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.titleContains(SIGN_IN_PAGE_TITLE));
            Assert.assertTrue(driver.getTitle().contains(SIGN_IN_PAGE_TITLE), "Not redirected to the Google sign-in page.");
            LOGGER.info("Verified redirection to the Google sign-in page.");

            // 4. Enter a test email address
            WebElement emailInput = findElement("Email input", By.id(EMAIL_INPUT_ID), By.name("identifier"), By.cssSelector("input[type='email']"));
            clickElement(emailInput, "Email input");
            emailInput.sendKeys("test@example.com");
            LOGGER.info("Entered email address: test@example.com");

            // 5. Click Next
            WebElement nextButton = findElement("Next button", By.id(NEXT_BUTTON_ID), By.xpath("//button[contains(., 'İleri')]"), By.cssSelector("button[type='button']"));
            clickElement(nextButton, "Next button");
            LOGGER.info("Clicked Next button");

            // 6. Verify the password field appears
            try {
                WebElement passwordInput = waitAndFindElement(By.name(PASSWORD_INPUT_NAME), 15);
                Assert.assertTrue(passwordInput.isDisplayed(), "Password field is not displayed.");
                LOGGER.info("Verified that the password field appears.");
            } catch (NoSuchElementException e) {
                // 7. Handle captcha validation
                if (isCaptchaPresent()) {
                    LOGGER.warning("Captcha detected. Test failed.");
                    captureScreenshot("CaptchaDetected");
                    Assert.fail("Captcha detected. Test failed.");
                } else {
                    throw e; // Re-throw if it's not a captcha
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            captureScreenshot("TestFailed");
            Assert.fail("Test failed: " + e.getMessage());
        }
    }

    private WebElement findElement(String elementName, By... bys) {
        WebElement element = null;
        for (By by : bys) {
            try {
                element = driver.findElement(by);
                LOGGER.info("Found element: " + elementName + " using " + by);
                return element;
            } catch (NoSuchElementException e) {
                LOGGER.fine("Element not found: " + elementName + " using " + by);
            }
        }
        throw new NoSuchElementException("Element not found: " + elementName + " using all locators.");
    }

    private WebElement waitAndFindElement(By by, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    private void clickElement(WebElement element, String elementName) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            LOGGER.info("Clicked element: " + elementName);
        } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
            LOGGER.warning("Click failed, attempting JavaScript click for: " + elementName);
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        } catch (TimeoutException e) {
            LOGGER.warning("Click failed, element not clickable within timeout for: " + elementName);
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
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
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filePath = "screenshots/" + testName + "_" + timestamp + ".png";
            File destFile = new File(filePath);
            new File("screenshots").mkdirs(); // Ensure directory exists
            Files.copy(screenshotFile.toPath(), destFile.toPath());
            LOGGER.info("Screenshot saved to: " + filePath);
        } catch (IOException e) {
            LOGGER.severe("Failed to capture screenshot: " + e.getMessage());
        }
    }

    public static class RetryAnalyzer implements org.testng.IRetryAnalyzer {
        private int retryCount = 0;
        private static final int maxRetryCount = RETRY_COUNT;

        @Override
        public boolean retry(ITestResult result) {
            if (retryCount < maxRetryCount) {
                retryCount++;
                LOGGER.warning("Retrying test " + result.getName() + " attempt " + retryCount + " of " + maxRetryCount);
                return true;
            }
            return false;
        }
    }
}