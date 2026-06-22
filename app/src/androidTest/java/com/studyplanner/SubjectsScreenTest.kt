package com.studyplanner

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end Compose UI test that launches MainActivity via Hilt,
 * navigates to the Subjects tab, and tests the full add-subject flow.
 *
 * Requires a connected device or emulator (androidTest source set).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SubjectsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun subjectsTab_isReachable_viaBottomNav() {
        composeRule.onNodeWithText("Subjects").performClick()
        composeRule.onNodeWithText("Subjects", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun addSubject_showsInList() {
        // Navigate to Subjects
        composeRule.onNodeWithText("Subjects").performClick()

        // Tap FAB
        composeRule.onNodeWithText("Add Subject").performClick()

        // Enter subject name
        composeRule.onNodeWithText("Subject name").performTextInput("Physics")

        // Confirm
        composeRule.onNodeWithText("Save").performClick()

        // Verify it appears in the list
        composeRule.onNodeWithText("Physics").assertIsDisplayed()
    }

    @Test
    fun deleteSubject_removesItFromList() {
        // Navigate to Subjects
        composeRule.onNodeWithText("Subjects").performClick()

        // Add a subject first
        composeRule.onNodeWithText("Add Subject").performClick()
        composeRule.onNodeWithText("Subject name").performTextInput("Temp Subject")
        composeRule.onNodeWithText("Save").performClick()
        composeRule.onNodeWithText("Temp Subject").assertIsDisplayed()

        // Delete it
        composeRule
            .onNodeWithContentDescription("Delete subject")
            .performClick()

        // Should be gone (or empty state visible)
        composeRule.onNodeWithText("Temp Subject").assertDoesNotExist()
    }

    @Test
    fun emptyState_shown_when_no_subjects() {
        // Navigate to Subjects (fresh DB has no subjects)
        composeRule.onNodeWithText("Subjects").performClick()
        composeRule
            .onNodeWithText("No subjects yet. Add one to get started!", substring = true)
            .assertIsDisplayed()
    }
}
