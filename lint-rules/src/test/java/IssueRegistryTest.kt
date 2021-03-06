package com.bugtsa.casher

import com.android.tools.lint.detector.api.TextFormat
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class IssueRegistryTest {

    @Test
    fun `check explanation for hamcrest import is correct`() {
        val output = IssueRegistry().issues
                .joinToString(separator = "\n") { "- **${it.id}** - ${it.getExplanation(TextFormat.RAW)}" }

        assertThat("""
        - **HamcrestImport** - Use Google Truth instead
        - **DirectColorUse** - Avoid direct use of colors in XML files. This will cause issues with different theme (eg. night) support
        - **LogWtfUsageError** - This lint check prevents usage of `Log.wtf()`.
        """.trimIndent()).isEqualTo(output)
    }
}