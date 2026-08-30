import XCTest

/// Drives the real app on a simulator. iOS never recreates the composition on rotation, so these
/// tests pin down that the demo keeps its Store state and its navigation position across a device
/// rotation, and that `onSetup()` is not repeated.
final class DemoUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
        app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.staticTexts["onSetup() calls: 1"].waitForExistence(timeout: 30))
    }

    override func tearDownWithError() throws {
        XCUIDevice.shared.orientation = .portrait
        app = nil
    }

    func testCounterStateSurvivesRotation() {
        app.buttons["+"].tap()
        app.buttons["+"].tap()
        XCTAssertTrue(app.staticTexts["2"].waitForExistence(timeout: 5))

        XCUIDevice.shared.orientation = .landscapeLeft
        XCTAssertTrue(app.staticTexts["2"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["onSetup() calls: 1"].exists)

        XCUIDevice.shared.orientation = .portrait
        XCTAssertTrue(app.staticTexts["2"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["onSetup() calls: 1"].exists)
    }

    func testBackStackSurvivesRotation() {
        app.buttons["+"].tap()
        XCTAssertTrue(app.staticTexts["1"].waitForExistence(timeout: 5))

        app.buttons["Show details"].tap()
        XCTAssertTrue(app.staticTexts["Count details"].waitForExistence(timeout: 5))

        XCUIDevice.shared.orientation = .landscapeLeft
        XCTAssertTrue(app.staticTexts["Count details"].waitForExistence(timeout: 5))

        XCUIDevice.shared.orientation = .portrait
        XCTAssertTrue(app.staticTexts["Count details"].waitForExistence(timeout: 5))

        app.buttons["Back"].tap()
        XCTAssertTrue(app.staticTexts["onSetup() calls: 1"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["1"].exists)
    }
}
