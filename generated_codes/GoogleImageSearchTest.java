import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class GoogleImageSearchTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeTest
    public void setUp() {
        // Set up ChromeDriver
        System.setProperty("webdriver.chrome.driver", "chromedriver"); // Replace with the actual path to your chromedriver
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void googleImageSearchTest() {
        // Test Case Steps:

        // 1) Open Google homepage.
        driver.get("https://www.google.com/");

        // 2) Click on "Images" link in the navigation.
        WebElement imagesLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Images']")));
        imagesLink.click();

        // 3) Verify you are on the Google Images page.
        Assert.assertEquals(driver.getCurrentUrl(), "https://www.google.com/imghp?hl=en&ogbl", "URL should be Google Images page.");

        // 4) Type "mountain landscape" in the search box.
        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("q")));
        searchBox.click(); // Click on the input field first
        searchBox.sendKeys("mountain landscape");

        // 5) Press Enter or click the search button.
        searchBox.submit(); // Submitting the form is equivalent to pressing Enter

        // 6) Verify image results titles are relevant.
        // Consider the first images that presents on the google search doesnt have a content on their alt tag.
        // They are just category indicators.
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("img[alt]")));
        List<WebElement> imageResults = driver.findElements(By.cssSelector("img[alt]"));

        // Check the first 5 image results for relevance
        int imagesToCheck = Math.min(5, imageResults.size());
        for (int i = 0; i < imagesToCheck; i++) {
            String altText = imageResults.get(i).getAttribute("alt");
            System.out.println("Image " + (i + 1) + " alt text: " + altText);
            Assert.assertTrue(altText.toLowerCase().contains("mountain") || altText.toLowerCase().contains("landscape") || altText.toLowerCase().contains("mountain landscape"), "Image " + (i + 1) + " alt text is not relevant: " + altText);
        }
    }

    @AfterTest
    public void tearDown() {
        // Quit the WebDriver
        if (driver != null) {
            driver.quit();
        }
    }
}