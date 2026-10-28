package com.group.xlibris.e2e;

import com.group.xlibris.e2e.pages.LoginPage;
import com.group.xlibris.e2e.pages.BooksPage;
import com.group.xlibris.e2e.pages.BookFormPage;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.BrowserType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BooksBrowserE2EIT {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private LoginPage loginPage;
    private BooksPage booksPage;
    private BookFormPage bookFormPage;

    @BeforeEach
    void setUp() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        context = browser.newContext();
        page = context.newPage();
        loginPage = new LoginPage(page);
        booksPage = new BooksPage(page);
        bookFormPage = new BookFormPage(page);
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @Test
    void userCanLoginAndOpenBooks() {
        loginPage.open();
        loginPage.login("user@xlibris.com", "Password123");
        booksPage.open();
        assertTrue(booksPage.isOpened());
        assertTrue(booksPage.isAddButtonVisible());
    }

    @Test
    void userCanCreateBook() {
        loginPage.open();
        loginPage.login("user@xlibris.com", "Password123");
        booksPage.open();
        booksPage.clickAddBook();
        String title = "Playwright Book " + UUID.randomUUID();
        bookFormPage.fillForm(title, "Book created by Playwright browser test");
        bookFormPage.submit();
        assertTrue(booksPage.isOpened());
        assertTrue(booksPage.hasBook(title));
        assertTrue(booksPage.hasSuccessMessage());
    }

    @Test
    void moderatorCannotCreateBook() {
        loginPage.open();
        loginPage.login("moderator@xlibris.com", "Password123");
        booksPage.open();
        assertTrue(booksPage.isOpened());
        assertFalse(booksPage.isAddButtonVisible());
        page.navigate("http://localhost:8080/books/new");
        var response = page.navigate("http://localhost:8080/books/new");
        assertNotNull(response);
        assertEquals(403, response.status());
    }
}