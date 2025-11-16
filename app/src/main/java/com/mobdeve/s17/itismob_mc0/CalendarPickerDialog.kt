package com.mobdeve.s17.itismob_mc0

import android.app.DatePickerDialog
import android.content.Context
import java.util.Calendar

object CalendarPickerDialog {

    fun show(context: Context, onDateSelected: (year: Int, month: Int, day: Int) -> Unit) {

        val calendar = Calendar.getInstance()

        val dialog = DatePickerDialog(
            context,
            { _, y, m, d ->
                onDateSelected(y, m, d)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.show()
    }
}
