const { Builder } = require("selenium-webdriver");
const chrome = require("selenium-webdriver/chrome");

const options = new chrome.Options().headless(); // Chạy chế độ không giao diện
const driver = new Builder()
  .forBrowser("chrome")
  .setChromeOptions(options)
  .build();
