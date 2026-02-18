package one.pkg.om.dialog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InfoDialogTest {

    @Test
    fun `test key is info`() {
        val dialog = InfoDialog()
        assertEquals("info", dialog.key)
    }
}
