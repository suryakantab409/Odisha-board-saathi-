package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SeedDataProvider
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.BoardType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BoardSaathi Odisha", appName)
    }

    @Test
    fun `starter catalog strictly separates BSE 10 and CHSE 12 Arts`() {
        val bseSubjects = SeedDataProvider.starterSubjects.filter { it.boardId == BoardType.BSE_10.id }
        val chseSubjects = SeedDataProvider.starterSubjects.filter { it.boardId == BoardType.CHSE_12_ARTS.id }

        assertTrue(bseSubjects.isNotEmpty())
        assertTrue(chseSubjects.isNotEmpty())
        assertTrue(bseSubjects.none { subj -> chseSubjects.any { it.id == subj.id } })

        val bseMcqs = SeedDataProvider.starterMcqs.filter { it.boardId == BoardType.BSE_10.id }
        val chseMcqs = SeedDataProvider.starterMcqs.filter { it.boardId == BoardType.CHSE_12_ARTS.id }
        assertTrue(bseMcqs.isNotEmpty())
        assertTrue(chseMcqs.isNotEmpty())
        assertTrue(bseMcqs.all { it.optionsEn.size == 4 && it.optionsOr.size == 4 })
        assertTrue(chseMcqs.all { it.optionsEn.size == 4 && it.optionsOr.size == 4 })
    }

    @Test
    fun `bilingual strings support English and Odia`() {
        val en = AppStrings.get(AppLanguage.EN)
        val or = AppStrings.get(AppLanguage.OR)
        assertEquals("BoardSaathi Odisha", en.appTitle)
        assertEquals("ବୋର୍ଡସାଥୀ ଓଡ଼ିଶା", or.appTitle)
    }
}
