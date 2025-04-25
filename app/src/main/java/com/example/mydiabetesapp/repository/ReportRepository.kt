package com.example.mydiabetesapp.repository

import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import com.example.mydiabetesapp.data.database.*
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ReportRepository(
    private val reportDao : ReportDao,
    private val glucoseDao: GlucoseDao,
    private val weightDao : WeightDao
) {
    fun reports(): Flow<List<ReportEntry>> = reportDao.getAll()

    suspend fun createReport(ctx: Context, start: LocalDate, end: LocalDate): ReportEntry {
        val from = start.format(DATE_FMT)
        val to   = end  .format(DATE_FMT)

        val glucose = glucoseDao.getBetween(from, to)
        val weight  = weightDao .getBetween(from, to)

        val csv = buildString {
            appendLine("Date,Time,Type,Value,Unit")
            glucose.forEach { appendLine("${it.date},${it.time},Glucose,${it.glucoseLevel},mmol/L") }
            weight .forEach { appendLine("${it.date},${it.time},Weight,${it.weight},kg")           }
        }

        val fileName = "diabetes_report_${start}_${end}.csv"
        val file = File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        file.writeText(csv)

        val report = ReportEntry(
            fileName  = fileName,
            uri       = file.toUri().toString(),
            startDate = from,
            endDate   = to,
            daysCount = ChronoUnit.DAYS.between(start, end).toInt() + 1
        )
        reportDao.insert(report)
        return report
    }

    suspend fun delete(report: ReportEntry) {
        kotlin.runCatching { File(android.net.Uri.parse(report.uri).path!!).delete() }
        reportDao.delete(report)
    }

    companion object {
        private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}
