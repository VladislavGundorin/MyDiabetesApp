package com.example.mydiabetesapp.feature.export.data

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import androidx.core.net.toUri
import com.example.mydiabetesapp.feature.glucose.data.GlucoseDao
import com.example.mydiabetesapp.feature.hba1c.data.Hba1cDao
import com.example.mydiabetesapp.feature.pressure.data.BloodPressureDao
import com.example.mydiabetesapp.feature.profile.data.UserProfile
import com.example.mydiabetesapp.feature.profile.data.UserProfileDao
import com.example.mydiabetesapp.feature.weight.data.WeightDao
import kotlinx.coroutines.flow.first
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

class ReportRepository(
    private val reportDao   : ReportDao,
    private val profileDao  : UserProfileDao,
    private val glucoseDao  : GlucoseDao,
    private val weightDao   : WeightDao,
    private val pressureDao : BloodPressureDao,
    private val hba1cDao    : Hba1cDao
) {

    fun reports() = reportDao.getAll()

    suspend fun createReport(
        ctx  : Context,
        start: LocalDate,
        end  : LocalDate
    ): ReportEntry {
        val from    = start.format(DATE_FMT)
        val to      = end  .format(DATE_FMT)
        val profile = profileDao.getUserById(1)
            ?: UserProfile(1, "—", "—", 0, 0f, 0f)

        val allGlucose  = glucoseDao.getAllEntries().first()
        val glucose     = allGlucose.filter  { it.date.parse() in start..end }

        val allWeight   = weightDao.getAllWeightEntries().first()
        val weight      = allWeight .filter  { it.date.parse() in start..end }

        val allPressure = pressureDao.getAll().first()
        val pressure    = allPressure.filter  { it.date.parse() in start..end }

        val allHba1c    = hba1cDao.getAll().first()
        val hba1c       = allHba1c  .filter  { it.date.parse() in start..end }

        val fileName = "diabetes_report_${start}_$end.pdf"
        val file     = File(
            ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        )

        val pdf       = PdfDocument()
        var pageIndex = 1
        var page      = pdf.startPage(pageInfo(pageIndex))
        var y         = 20f
        val paint     = TextPaint().apply { textSize = 12f }

        fun write(line: String) {
            if (y > 800f) {
                pdf.finishPage(page)
                pageIndex++
                page = pdf.startPage(pageInfo(pageIndex))
                y = 20f
            }
            page.canvas.drawText(line, 20f, y, paint)
            y += 16f
        }

        try {
            write("Диабет‑отчёт  •  $from – $to  (${ChronoUnit.DAYS.between(start, end)+1} дн.)")
            write("Пользователь: ${profile.name}   •   ${profile.gender}, ${profile.age} лет")
            write("Рост: ${profile.height} см   Вес: ${profile.weight} кг")
            write("")

            fun <T> section(
                title : String,
                header: String,
                data  : List<T>,
                map   : (T) -> String
            ) {
                write(title)
                write(header)
                data.forEach { write(map(it)) }
                write("")
            }

            section(
                "Глюкоза",
                "Дата        Время  Категория          Знач., ммоль/л",
                glucose
            ) { "%-12s%-8s%-18s%.2f".format(it.date, it.time, it.category, it.glucoseLevel) }

            section(
                "Вес",
                "Дата        Время  Вес, кг",
                weight
            ) { "%-12s%-8s%.1f".format(it.date, it.time, it.weight) }

            section(
                "АД / Пульс",
                "Дата        Время  Сист  Диаст  Пульс",
                pressure
            ) { "%-12s%-8s%5d%7d%7d".format(it.date, it.time, it.systolic, it.diastolic, it.pulse) }

            section(
                "HbA1c",
                "Дата              HbA1c, %",
                hba1c
            ) { "%-16s%.2f".format(it.date, it.hba1c) }

            pdf.finishPage(page)
            pdf.writeTo(file.outputStream())
        } finally {
            pdf.close()
        }

        val report = ReportEntry(
            fileName  = fileName,
            uri       = file.toUri().toString(),
            startDate = from,
            endDate   = to,
            daysCount = (ChronoUnit.DAYS.between(start, end) + 1).toInt()
        )
        reportDao.insert(report)
        return report
    }

    suspend fun delete(report: ReportEntry) {
        runCatching { File(android.net.Uri.parse(report.uri).path!!).delete() }
        reportDao.delete(report)
    }

    private fun pageInfo(num: Int) =
        PdfDocument.PageInfo.Builder(595, 842, num)
            .create()

    companion object {
        private val DATE_FMT = DateTimeFormatter
            .ofPattern("dd.MM.yyyy", Locale.getDefault())

        private fun String.parse() = LocalDate.parse(this, DATE_FMT)

        private operator fun ClosedRange<LocalDate>.contains(d: LocalDate) =
            !d.isBefore(start) && !d.isAfter(endInclusive)
    }
}
