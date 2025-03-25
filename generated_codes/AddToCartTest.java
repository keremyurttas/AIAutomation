import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
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
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddToCartTest {

    private WebDriver driver;
    private Logger logger = Logger.getLogger(AddToCartTest.class.getName());
    private String baseUrl = "https://www.trendyol.com";

    @BeforeClass
    public void setup() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        logger.setLevel(Level.INFO);
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void addToCartTest() {
        try {
            driver.get(baseUrl);
            handleGenderModal();
            searchProduct("tshirt");
            selectProduct();
            selectSizeIfAvailable();
            addToCart();
            goToCart();
            verifyCartDetails();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            captureScreenshot(driver, "addToCartTest_failure");
            Assert.fail("Test failed: " + e.getMessage());
        }
    }

    private void handleGenderModal() {
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(10))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            WebElement modalCloseButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.modal-close")));
            modalCloseButton.click();
            logger.info("Gender selection modal closed.");
            Thread.sleep(2000); // Wait after closing modal
        } catch (Exception e) {
            logger.info("Gender selection modal not found or already closed.");
        }
    }

    private void searchProduct(String productName) {
        try {
            WebElement searchBox = findElement(By.id("search-input"), By.cssSelector("input[type='text'][placeholder='Aradığınız ürün, kategori veya markayı yazınız']"), By.xpath("//input[@type='text' and @placeholder='Aradığınız ürün, kategori veya markayı yazınız']"));
            searchBox.click();
            searchBox.sendKeys(productName);
            searchBox.sendKeys(Keys.ENTER);
            logger.info("Searched for product: " + productName);

            // Verify search results appear
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(35))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.prdct-cntnr-wrppr")));
            logger.info("Search results loaded successfully.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to search for product: " + e.getMessage(), e);
            captureScreenshot(driver, "searchProduct_failure");
            Assert.fail("Failed to search for product: " + e.getMessage());
        }
    }

    private void selectProduct() {
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(35))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            WebElement productLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.prdct-cntnr-wrppr > div:first-child > a")));
            productLink.click();
            logger.info("Selected a product from search results.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to select product: " + e.getMessage(), e);
            captureScreenshot(driver, "selectProduct_failure");
            Assert.fail("Failed to select product: " + e.getMessage());
        }
    }

    private void selectSizeIfAvailable() {
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(35))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            // Check if size selection is available
            List<WebElement> sizeOptions = driver.findElements(By.cssSelector("div.size-choose > div > button"));
            if (!sizeOptions.isEmpty()) {
                WebElement firstAvailableSize = wait.until(ExpectedConditions.elementToBeClickable(sizeOptions.get(0)));
                firstAvailableSize.click();
                logger.info("Selected size: " + firstAvailableSize.getText());
            } else {
                logger.info("No size selection available for this product.");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to select size (if available): " + e.getMessage(), e);
        }
    }

    private void addToCart() {
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(35))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            WebElement addToCartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.add-to-bs-btn")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", addToCartButton);
            Thread.sleep(500); // Small pause to ensure element is in view
            addToCartButton.click();
            logger.info("Clicked 'Add to Cart' button.");

            // Verify item was successfully added (example: check for success message)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.success-message-container")));
            logger.info("Product added to cart successfully.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to add to cart: " + e.getMessage(), e);
            captureScreenshot(driver, "addToCart_failure");
            Assert.fail("Failed to add to cart: " + e.getMessage());
        }
    }

    private void goToCart() {
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(35))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            WebElement cartButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.link.account-basket")));
            cartButton.click();
            logger.info("Navigated to cart page.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to navigate to cart: " + e.getMessage(), e);
            captureScreenshot(driver, "goToCart_failure");
            Assert.fail("Failed to navigate to cart: " + e.getMessage());
        }
    }

    private void verifyCartDetails() {
        try {
            Wait<WebDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(35))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoring(NoSuchElementException.class);

            // Verify product details (example: product name)
            WebElement productNameElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.ty-link.ty-link-primary")));
            String productName = productNameElement.getText();
            Assert.assertNotNull(productName, "Product name is not displayed.");
            logger.info("Product name in cart: " + productName);

            // Verify total amount
            WebElement totalAmountElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.pb-summary-total")));
            String totalAmountText = totalAmountElement.getText();
            Assert.assertNotNull(totalAmountText, "Total amount is not displayed.");
            logger.info("Total amount in cart: " + totalAmountText);

            logger.info("Cart details verified successfully.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to verify cart details: " + e.getMessage(), e);
            captureScreenshot(driver, "verifyCartDetails_failure");
            Assert.fail("Failed to verify cart details: " + e.getMessage());
        }
    }

    private WebElement findElement(By id, By css, By xpath) {
        try {
            return driver.findElement(id);
        } catch (NoSuchElementException e) {
            logger.info("Element not found by ID, trying CSS selector...");
        }

        try {
            return driver.findElement(css);
        } catch (NoSuchElementException e) {
            logger.info("Element not found by CSS, trying XPath...");
        }

        try {
            return driver.findElement(xpath);
        } catch (NoSuchElementException e) {
            logger.warning("Element not found by XPath.");
            throw new NoSuchElementException("Element not found using any locator.");
        }
    }

    private WebElement findLoginButton() {
        try {
            return driver.findElement(By.cssSelector("div.link.account-user > p.link-text"));
        } catch (NoSuchElementException e) {
            logger.info("Login button not found by CSS, trying XPath 1...");
        }

        try {
            return driver.findElement(By.xpath("//p[contains(text(),'Giriş') and contains(@class,'link-text')]"));
        } catch (NoSuchElementException e) {
            logger.info("Login button not found by XPath 1, trying XPath 2...");
        }

        try {
            return driver.findElement(By.xpath("//div[contains(@class,'account-nav')]//p[contains(text(),'Giriş')]"));
        } catch (NoSuchElementException e) {
            logger.warning("Login button not found by XPath 2. Trying JavaScript click...");
            try {
                WebElement loginButton = driver.findElement(By.cssSelector("div.link.account-user > p.link-text"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginButton);
                return loginButton;
            } catch (NoSuchElementException ex) {
                logger.severe("Login button not found using any locator or JavaScript.");
                throw new NoSuchElementException("Login button not found.");
            }
        }
    }

    private void captureScreenshot(WebDriver driver, String testName) {
        try {
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filePath = "screenshots/" + testName + "_" + timestamp + ".png";
            File destFile = new File(filePath);
            new File("screenshots").mkdirs(); // Ensure directory exists
            Files.copy(screenshotFile.toPath(), destFile.toPath());
            logger.info("Screenshot captured: " + filePath);
        } catch (IOException e) {
            logger.severe("Failed to capture screenshot: " + e.getMessage());
        }
    }

    @AfterClass
    public void teardown() {
        if (driver != null) {
            driver.quit();
            logger.info("WebDriver closed.");
        }
    }

    public static class RetryAnalyzer implements IRetryAnalyzer {
        private int retryCount = 0;
        private static final int maxRetryCount = 3;

        @Override
        public boolean retry(ITestResult result) {
            if (retryCount < maxRetryCount) {
                retryCount++;
                System.out.println("Retrying test " + result.getName() + " attempt " + retryCount + " of " + maxRetryCount);
                return true;
            }
            return false;
        }
    }
}