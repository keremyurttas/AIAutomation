import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
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

public class AddToCartTest {

    private WebDriver driver;
    private static final Logger LOGGER = Logger.getLogger(AddToCartTest.class.getName());
    private String baseUrl = "https://www.trendyol.com/"; // Replace with your base URL

    @BeforeClass
    public void setUpClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().deleteAllCookies();
        driver.get(baseUrl);
        log("Navigated to: " + baseUrl);
    }

    @Test
    public void testAddToCart() {
        try {
            logStep("Starting AddToCart test");

            // 1. Close the gender selection modal if present
            handleGenderSelectionModal();
            captureScreenshotOnStep("Gender Modal Handled");

            // 2. Search for 'tshirt'
            searchForProduct("tshirt");
            captureScreenshotOnStep("Searched for tshirt");

            // 3. Select a product from the search results
            selectProduct();
            captureScreenshotOnStep("Selected a product");

            // 4. Select size if presented
            selectSizeIfAvailable();
            captureScreenshotOnStep("Selected size if available");

            // 5. Add to cart
            addToCart();
            captureScreenshotOnStep("Added to cart");

            // 6. Go to cart
            goToCart();
            captureScreenshotOnStep("Navigated to cart");

            // 7. Verify product details and total amount
            verifyCartDetails();
            captureScreenshotOnStep("Verified cart details");

            logStep("AddToCart test completed successfully");

        } catch (Exception e) {
            captureScreenshotOnFailure("testAddToCart");
            LOGGER.log(Level.SEVERE, "Test failed: " + e.getMessage(), e);
            Assert.fail("Test failed: " + e.getMessage());
        }
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshotOnFailure(result.getMethod().getMethodName());
        }
        if (driver != null) {
            driver.quit();
            log("Driver quit");
        }
    }

    // Helper Methods

    private void handleGenderSelectionModal() {
        try {
            By modalCloseButtonLocator = By.xpath("//div[@class='modal-content']/button[@class='close']");
            WebElement closeButton = findElementByXpath("//div[@class='modal-content']/button[@class='close']");

            if (closeButton != null && closeButton.isDisplayed()) {
                log("Gender selection modal found. Closing...");
                clickWebElementForTpath(modalCloseButtonLocator);
                log("Gender selection modal closed.");
                captureScreenshotOnStep("Gender Modal Closed");
            } else {
                log("Gender selection modal not found.");
            }
        } catch (NoSuchElementException e) {
            log("Gender selection modal not found.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error handling gender selection modal: " + e.getMessage(), e);
        }
    }

    private void searchForProduct(String productName) {
        By searchBoxLocator = By.xpath("//input[@placeholder='Aradığınız ürün, kategori veya markayı yazınız']");
        sendKeysElementTPath(searchBoxLocator, true, productName);
        log("Entered '" + productName + "' in the search box.");

        try {
            WebElement searchBox = driver.findElement(By.xpath("//input[@placeholder='Aradığınız ürün, kategori veya markayı yazınız']"));
            searchBox.sendKeys(Keys.ENTER);
            log("Pressed Enter key.");
        } catch (NoSuchElementException e) {
            LOGGER.log(Level.SEVERE, "Search box not found: " + e.getMessage(), e);
            Assert.fail("Search box not found: " + e.getMessage());
        }
    }

    private void selectProduct() {
        By productLocator = By.xpath("//div[@class='p-card-wrppr with-campaign-view']//a");
        try {
            clickWebElementForTpath(productLocator);
            log("Clicked on a product from the search results.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not click on product: " + e.getMessage(), e);
            Assert.fail("Could not click on product: " + e.getMessage());
        }
    }

    private void selectSizeIfAvailable() {
        try {
            By sizeLocator = By.xpath("//div[@class='size-choose']/button[not(@disabled)]");
            WebElement sizeElement = driver.findElement(By.xpath("//div[@class='size-choose']/button[not(@disabled)]"));

            if (sizeElement != null && sizeElement.isDisplayed()) {
                clickWebElementForTpath(sizeLocator);
                log("Selected available size.");
            } else {
                log("No available size to select.");
            }
        } catch (NoSuchElementException e) {
            log("No size options found.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error selecting size: " + e.getMessage(), e);
        }
    }

    private void addToCart() {
        By addToCartButtonLocator = By.xpath("//button[@class='add-to-basket']");
        try {
            clickWebElementForTpath(addToCartButtonLocator);
            log("Clicked 'Add to Cart' button.");
            // Wait for confirmation (e.g., a success message or cart count update)
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("basketBtn"))); // Example: wait for cart icon to be clickable
            log("Item added to cart successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not add to cart: " + e.getMessage(), e);
            Assert.fail("Could not add to cart: " + e.getMessage());
        }
    }

    private void goToCart() {
        By cartButtonLocator = By.xpath("//a[@class='link account-basket']");
        try {
            clickWebElementForTpath(cartButtonLocator);
            log("Clicked on the cart button.");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.urlContains("/sepet"));
            log("Navigated to the cart page.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not navigate to cart: " + e.getMessage(), e);
            Assert.fail("Could not navigate to cart: " + e.getMessage());
        }
    }

    private void verifyCartDetails() {
        try {
            // Verify product details
            WebElement productTitleElement = findElementByCssSelector(".pb-item-name");
            String productTitle = productTitleElement.getText();
            Assert.assertNotNull(productTitle, "Product title is missing in the cart.");
            log("Product title in cart: " + productTitle);

            // Verify total amount
            WebElement totalAmountElement = findElementByCssSelector(".total-price");
            String totalAmountText = totalAmountElement.getText();
            Assert.assertNotNull(totalAmountText, "Total amount is missing in the cart.");
            log("Total amount in cart: " + totalAmountText);

            log("Product details and total amount verified successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying cart details: " + e.getMessage(), e);
            Assert.fail("Error verifying cart details: " + e.getMessage());
        }
    }

    // Element Locator Methods
    private WebElement findElementById(String id) {
        log("Finding element by ID: " + id);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(id)));
            log("Element found by ID: " + id);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with ID '" + id + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByName(String name) {
        log("Finding element by Name: " + name);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.name(name)));
            log("Element found by Name: " + name);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with Name '" + name + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByCssSelector(String cssSelector) {
        log("Finding element by CSS Selector: " + cssSelector);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));
            log("Element found by CSS Selector: " + cssSelector);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with CSS Selector '" + cssSelector + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByXpath(String xpath) {
        log("Finding element by XPath: " + xpath);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
            log("Element found by XPath: " + xpath);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with XPath '" + xpath + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByLinkText(String linkText) {
        log("Finding element by Link Text: " + linkText);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText(linkText)));
            log("Element found by Link Text: " + linkText);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with Link Text '" + linkText + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    private WebElement findElementByPartialLinkText(String partialLinkText) {
        log("Finding element by Partial Link Text: " + partialLinkText);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText(partialLinkText)));
            log("Element found by Partial Link Text: " + partialLinkText);
            return element;
        } catch (Exception e) {
            String errorMessage = "Element with Partial Link Text '" + partialLinkText + "' not found: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            throw new NoSuchElementException(errorMessage);
        }
    }

    // Screenshot Capture Methods
    private void captureScreenshotOnStep(String stepName) {
        captureScreenshot(stepName, "step");
    }

    private void captureScreenshotOnFailure(String methodName) {
        captureScreenshot(methodName, "failure");
    }

    private void captureScreenshot(String context, String type) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = String.format("screenshot_%s_%s_%s.png", timestamp, context, type);
        File screenshotFile = new File("screenshots/" + filename);

        try {
            Path screenshotsDir = Paths.get("screenshots");
            if (!Files.exists(screenshotsDir)) {
                Files.createDirectories(screenshotsDir);
            }

            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            Files.copy(sourceFile.toPath(), screenshotFile.toPath());

            log("Screenshot captured: " + screenshotFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error capturing screenshot: " + e.getMessage(), e);
        }
    }

    // Logging Methods
    private void log(String message) {
        LOGGER.info(message);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + message); // Log to console as well
    }

    private void logStep(String step) {
        log("Step: " + step);
    }
}