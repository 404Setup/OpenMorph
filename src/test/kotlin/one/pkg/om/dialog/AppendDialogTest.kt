package one.pkg.om.dialog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppendDialogTest {

    @Test
    fun `should implement IDialog and have correct key`() {
        val dialog = AppendDialog()

        assertTrue(dialog is IDialog, "AppendDialog should implement IDialog")
        assertEquals("append", dialog.key, "Key should be 'append'")
    }
}
