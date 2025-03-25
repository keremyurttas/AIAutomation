import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.*;
import org.testng.IRetryAnalyzer;
import org.testng.Reporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleAccountSigninTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String baseUrl = "https://www.google.com/";
    private final String testEmail = "test@example.com";
    private final String signInButtonLabel = "Oturum açın"; // Turkish for "Sign in"
    private final String emailOrPhoneLabel = "E-posta veya telefon"; // Turkish for "Email or phone"
    private final String nextButtonLabel = "İleri"; // Turkish for "Next"
    private final String passwordFieldLabel = "Şifrenizi girin"; // Turkish for "Enter your password"
    private final String captchaIdentifier = "captcha";

    private static final Logger LOGGER = Logger.getLogger(GoogleAccountSigninTest.class.getName());

    @BeforeClass
    public void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setupTest() {
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1920, 1080));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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

    private void captureScreenshot(String testName) {
        try {
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filePath = "screenshots/" + testName + "_" + timestamp + ".png";
            File destFile = new File(filePath);
            Files.copy(screenshotFile.toPath(), destFile.toPath());
            LOGGER.log(Level.SEVERE, "Screenshot captured: " + filePath);
            Reporter.log("<br><img src='" + destFile.getAbsolutePath() + "' height='400'/><br>");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to capture screenshot: " + e.getMessage(), e);
        }
    }

    private WebElement findElement(String identifier) {
        WebElement element = null;
        try {
            element = driver.findElement(By.id(identifier));
            LOGGER.info("Found element by ID: " + identifier);
            return element;
        } catch (NoSuchElementException ignored) {
        }

        try {
            element = driver.findElement(By.cssSelector(identifier));
            LOGGER.info("Found element by CSS: " + identifier);
            return element;
        } catch (NoSuchElementException ignored) {
        }

        try {
            element = driver.findElement(By.xpath(identifier));
            LOGGER.info("Found element by XPath: " + identifier);
            return element;
        } catch (NoSuchElementException e) {
            String errorMessage = "Element not found with id, css, or xpath: " + identifier;
            LOGGER.severe(errorMessage);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private void dismissModalsAndOverlays() {
        try {
            // Example: Dismiss a cookie consent modal
            WebElement cookieAcceptButton = driver.findElement(By.id("L2AGLb"));
            if (cookieAcceptButton.isDisplayed()) {
                cookieAcceptButton.click();
                Thread.sleep(2000); // Wait for modal to disappear
                try {
                    driver.findElement(By.id("L2AGLb"));
                    LOGGER.warning("Cookie consent modal still present after clicking accept.");
                } catch (NoSuchElementException e) {
                    LOGGER.info("Cookie consent modal successfully dismissed.");
                }
            }
        } catch (NoSuchElementException e) {
            LOGGER.info("No cookie consent modal found.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.severe("Interrupted while waiting for modal to disappear: " + e.getMessage());
        }
    }

    private void clickElementWithFallback(String id, String css, String xpath) {
        WebElement element = null;
        try {
            try {
                element = findElement(id);
            } catch (NoSuchElementException ignored) {
                try {
                    element = findElement(css);
                } catch (NoSuchElementException ignored2) {
                    element = findElement(xpath);
                }
            }

            wait.until(ExpectedConditions.elementToBeClickable(element));
            element.click();
            LOGGER.info("Clicked element using standard click.");
        } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
            LOGGER.warning("Element not clickable, falling back to JavaScript click: " + e.getMessage());
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        } catch (TimeoutException e) {
            LOGGER.warning("Timeout waiting for element to be clickable, falling back to JavaScript click: " + e.getMessage());
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].click();", element);
        }
    }

    private void handleCaptcha() {
        try {
            WebElement captchaElement = driver.findElement(By.id(captchaIdentifier));
            if (captchaElement.isDisplayed()) {
                LOGGER.warning("Captcha detected. Please solve it manually and then wait for the test to continue.");
                Thread.sleep(60000); // Wait for 60 seconds for manual solving
            }
        } catch (NoSuchElementException e) {
            LOGGER.info("No captcha detected.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.severe("Interrupted while waiting for captcha to be solved: " + e.getMessage());
        }
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void googleAccountSigninTest() {
        // 1. Open Google homepage
        driver.get(baseUrl);
        LOGGER.info("Opened Google homepage: " + baseUrl);

        // 2. Click on the "Sign in" button
        dismissModalsAndOverlays();
        clickElementWithFallback("gb_70", "a.gb_Ua.gb_zd.gb_qd.gb_hd", "//a[@aria-label='" + signInButtonLabel + "']");
        LOGGER.info("Clicked on the 'Sign in' button.");

        // 3. Verify you are redirected to the Google sign-in page
        wait.until(ExpectedConditions.titleContains("Google Hesapları"));
        Assert.assertTrue(driver.getTitle().contains("Google Hesapları"), "Title should contain 'Google Hesapları'");
        LOGGER.info("Verified redirection to Google sign-in page.");

        // 4. Enter a test email address
        handleCaptcha();
        WebElement emailInput = findElement("identifierId");
        emailInput.click();
        emailInput.sendKeys(testEmail);
        LOGGER.info("Entered email address: " + testEmail);

        // 5. Click Next
        clickElementWithFallback("identifierNext", "button.VfPpkd-LgbsSe.VfPpkd-LgbsSe-OWXEXe-k8QpJ.VfPpkd-LgbsSe-OWXEXe-dgl2Hf.nCP5yc.AjY5Oe.DuMIQc.LQeN7.BqKGqe.Jskylb.TrZEUc.lw1w4b", "//button[.//span[text()='" + nextButtonLabel + "']]");
        LOGGER.info("Clicked 'Next' button.");

        // 6. Verify the password field appears
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("Passwd")));
            WebElement passwordField = driver.findElement(By.name("Passwd"));
            Assert.assertTrue(passwordField.isDisplayed(), "Password field should be displayed.");
            LOGGER.info("Verified that the password field appears.");
            LOGGER.info("Successfully signed in with " + testEmail + " and verified that the password field appears.");
        } catch (TimeoutException e) {
            LOGGER.severe("Password field did not appear within the timeout.");
            captureScreenshot("passwordFieldNotAppeared");
            Assert.fail("Password field did not appear within the timeout.");
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