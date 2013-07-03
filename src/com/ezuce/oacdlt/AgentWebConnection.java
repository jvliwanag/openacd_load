package com.ezuce.oacdlt;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AgentWebConnection extends BaseAgentConnection {

	private final String loginURI;
	private WebDriver driver;

	public AgentWebConnection(String username, String password,
			AgentConnectionListener listener, Phone phone, String loginURI) {
		super(username, password, listener, phone);

		this.loginURI = loginURI;
	}

	@Override
	public void connect() {
		driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.get(loginURI);

		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.id("password")).sendKeys(password);
		driver.findElement(By.id("login-button")).click();
	}

	@Override
	public void disconnect() {
		driver.findElement(By.id("logout-link")).click();
		driver.close();
	}

	@Override
	public void goAvailable() {
		if (isReleased()) {
			clickReleaseToggleButton();
		}
	}

	@Override
	public void goReleased() {
		if (!isReleased()) {
			clickReleaseToggleButton();
		}
	}

	private void clickReleaseToggleButton() {
		new WebDriverWait(driver, 5).until(
				ExpectedConditions.elementToBeClickable(By
						.id("agent-state-session-button"))).click();
	}

	private boolean isReleased() {
		return new WebDriverWait(driver, 5)
				.until(ExpectedConditions.presenceOfElementLocated(By
						.xpath("//button[@id='agent-state-session-button']/div/i")))
				.getAttribute("class").equals("icon-play");
	}

	@Override
	public void hangUp() {
		driver.findElement(By.xpath("//button[@data-class='smHangupButton'"))
				.click();
	}

	@Override
	public void endWrapup() {
		driver.findElement(
				By.xpath("//button[@data-class='smEndWrapupButton']")).click();
	}

	@Override
	public void sendRPC(String method, Object... args) {

	}

}
