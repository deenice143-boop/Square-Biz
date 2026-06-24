package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class TicketValidationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testFormValidationAndSuccessToast() {
        // Set up the app UI
        composeTestRule.setContent {
            MyApplicationTheme {
                LandingPageScreen()
            }
        }

        // 1. Initially, validation errors and toast should not exist
        composeTestRule.onNodeWithTag("error_tech_id_empty").assertDoesNotExist()
        composeTestRule.onNodeWithTag("error_issue_type_empty").assertDoesNotExist()
        composeTestRule.onNodeWithTag("success_toast_container").assertDoesNotExist()

        // 2. Open the 'File Incident' dialog (simulator_file_incident_btn is on the main scrollable screen)
        composeTestRule.onNodeWithTag("simulator_file_incident_btn").performScrollTo().performTouchInput { click() }
        composeTestRule.waitForIdle()

        // 3. Click submit with empty values to trigger client-side validation errors (no scroll needed)
        composeTestRule.onNodeWithTag("submit_create_ticket_btn", useUnmergedTree = true).performTouchInput { click() }
        composeTestRule.waitForIdle()

        // PRINT SEMANTICS TREE FOR DEBUGGING
        println("SEMTREE_PRINT_START")
        try {
            println(composeTestRule.onRoot(useUnmergedTree = true).printToString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("SEMTREE_PRINT_END")

        // 4. Verify validation error messages are displayed
        composeTestRule.onNodeWithTag("error_tech_id_empty", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("error_issue_type_empty", useUnmergedTree = true).assertExists()

        // 5. Fill out the Technician ID and Issue Type fields
        composeTestRule.onNodeWithTag("form_tech_id_input", useUnmergedTree = true).performTextInput("TECH-UNIT-TEST")
        composeTestRule.onNodeWithTag("form_issue_type_input", useUnmergedTree = true).performTextInput("Database Patch")
        composeTestRule.waitForIdle()

        // 6. Disable auto-advance to prevent the Toast's timed exit-delay from completing instantly
        composeTestRule.mainClock.autoAdvance = false

        // 7. Submit the validated form
        composeTestRule.onNodeWithTag("submit_create_ticket_btn", useUnmergedTree = true).performTouchInput { click() }

        // 8. Advance clock by 100ms to let the recomposition cycle render the toast
        composeTestRule.mainClock.advanceTimeBy(100)

        // 9. Verify success toast is visible
        composeTestRule.onNodeWithTag("success_toast_container", useUnmergedTree = true).assertExists()

        // 10. Re-enable autoAdvance for clean tear down
        composeTestRule.mainClock.autoAdvance = true
    }
}
