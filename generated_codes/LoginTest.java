import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginTest {

    private WebDriver driver;
    private static final Logger LOGGER = Logger.getLogger(LoginTest.class.getName());
    private static final String BASE_URL = "https://www.trendyol.com/"; // Replace with your base URL

    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); // Reduced implicit wait
        LOGGER.setLevel(Level.INFO);
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testLogin() {
        try {
            driver.get(BASE_URL);

            // Close gender selection modal if present
            try {
                WebElement genderModalCloseButton = findElement(By.xpath("//div[@class='modal-close']"));
                if (genderModalCloseButton != null && genderModalCloseButton.isDisplayed()) {
                    genderModalCloseButton.click();
                    Thread.sleep(2000); // Wait 2 seconds after closing modal
                    LOGGER.info("Gender selection modal closed.");
                }
            } catch (NoSuchElementException e) {
                LOGGER.info("Gender selection modal not found.");
            }

            // Navigate to the main page if not already there
            if (!driver.getCurrentUrl().startsWith(BASE_URL)) {
                driver.get(BASE_URL);
            }

            // Scroll to the account section
            scrollToElement(By.cssSelector("div.link.account-user > p.link-text"));

            // Click "Giriş Yap" link with multiple selectors
            By[] loginSelectors = {
                    By.cssSelector("div.link.account-user > p.link-text"),
                    By.xpath("//p[contains(text(),'Giriş') and contains(@class,'link-text')]"),
                    By.xpath("//div[contains(@class,'account-nav')]//p[contains(text(),'Giriş')]")
            };
            WebElement girisYapLink = findElementWithMultipleSelectors(loginSelectors);

            if (girisYapLink != null) {
                try {
                    girisYapLink.click();
                    LOGGER.info("Clicked 'Giriş Yap' link.");
                } catch (ElementClickInterceptedException e) {
                    // Try JavaScript click if element is intercepted
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", girisYapLink);
                    LOGGER.warning("ElementClickInterceptedException: Used JavaScript click for 'Giriş Yap'.");
                }
            } else {
                Assert.fail("Could not find 'Giriş Yap' link.");
            }

            // Verify URL contains "giris"
            waitForUrlToContain("giris", 35);
            Assert.assertTrue(driver.getCurrentUrl().contains("giris"), "URL does not contain 'giris'.");
            LOGGER.info("URL contains 'giris'.");

            // Enter email and password
            WebElement emailInput = findElement(By.id("login-email"));
            emailInput.sendKeys("testest23451@gmail.com");
            LOGGER.info("Entered email.");

            WebElement passwordInput = findElement(By.id("login-password-input"));
            passwordInput.sendKeys("Testest23451");
            LOGGER.info("Entered password.");

            // Click login/submit button
            WebElement loginButton = findElement(By.cssSelector("button.q-primary.q-fluid.q-button-medium.q-button.submit[type='submit']"));
            try {
                loginButton.click();
                LOGGER.info("Clicked login button.");
            } catch (ElementClickInterceptedException e) {
                // Try JavaScript click if element is intercepted
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
                LOGGER.warning("ElementClickInterceptedException: Used JavaScript click for login button.");
            }

            // Verify login success by checking for "Hesabım" text
            waitForElementVisibility(By.xpath("//p[contains(text(),'Hesabım')]"), 35);
            WebElement hesabimText = findElement(By.xpath("//p[contains(text(),'Hesabım')]"));
            Assert.assertTrue(hesabimText.isDisplayed(), "'Hesabım' text is not visible.");
            LOGGER.info("'Hesabım' text is visible. Login successful.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            captureScreenshot(driver, "login_failure");
            Assert.fail("Test failed: " + e.getMessage());
        }
    }

    private WebElement findElement(By by) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(35))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(by));
        } catch (TimeoutException e) {
            LOGGER.warning("Element not found using: " + by.toString());
            return null;
        }
    }

    private WebElement findElementWithMultipleSelectors(By[] selectors) {
        WebElement element = null;
        for (By selector : selectors) {
            try {
                element = findElement(selector);
                if (element != null) {
                    return element;
                }
            } catch (Exception e) {
                LOGGER.warning("Element not found using: " + selector.toString());
            }
        }
        return null;
    }

    private void waitForUrlToContain(String text, int timeoutInSeconds) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutInSeconds))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        wait.until(ExpectedConditions.urlContains(text));
    }

    private void waitForElementVisibility(By by, int timeoutInSeconds) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutInSeconds))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private void scrollToElement(By by) {
        try {
            WebElement element = findElement(by);
            if (element != null) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
                Thread.sleep(500); // Small wait after scrolling
                LOGGER.info("Scrolled to element: " + by.toString());
            } else {
                LOGGER.warning("Element not found, cannot scroll: " + by.toString());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.severe("Interrupted while scrolling: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Error while scrolling: " + e.getMessage());
        }
    }

    private void captureScreenshot(WebDriver driver, String screenshotName) {
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            File source = ts.getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filePath = "screenshots/" + screenshotName + "_" + timestamp + ".png";
            File target = new File(filePath);
            Files.createDirectories(target.getParentFile().toPath());
            Files.copy(source.toPath(), target.toPath());
            LOGGER.info("Screenshot captured: " + filePath);
        } catch (IOException e) {
            LOGGER.severe("Failed to capture screenshot: " + e.getMessage());
        }
    }

    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
            LOGGER.info("Driver quit.");
        }
    }

    public static class RetryAnalyzer implements IRetryAnalyzer {
        private int retryCount = 0;
        private static final int maxRetryCount = 3;

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