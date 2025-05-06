package com.example.mydiabetesapp.drive

import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class DriveServiceHelper(
    credential: GoogleAccountCredential,
    appName: String
) {
    private val drive = Drive.Builder(
        AndroidHttp.newCompatibleTransport(),
        GsonFactory.getDefaultInstance(),
        credential
    )
        .setApplicationName(appName)
        .build()

    suspend fun exportAllAsZip(
        profileCsv: String,
        glucoseCsv: String,
        weightCsv: String,
        hba1cCsv: String,
        pulseCsv: String
    ): File = withContext(Dispatchers.IO) {
        val zipBytes: ByteArray = ZipUtils.packToZip(mapOf(
            "profile.csv" to profileCsv.toByteArray(Charsets.UTF_8),
            "glucose.csv" to glucoseCsv.toByteArray(Charsets.UTF_8),
            "weight.csv"  to weightCsv.toByteArray(Charsets.UTF_8),
            "hba1c.csv"   to hba1cCsv.toByteArray(Charsets.UTF_8),
            "pulse.csv"   to pulseCsv.toByteArray(Charsets.UTF_8)
        ))

        val meta = File().apply {
            name = "mydiabetes_backup.zip"
            parents = listOf("root")
        }
        val content = ByteArrayContent("application/zip", zipBytes)
        drive.files().create(meta, content)
            .setFields("id,name,createdTime")
            .execute()
    }

    suspend fun findLatestBackup(): File? = withContext(Dispatchers.IO) {
        val result = drive.files().list()
            .setQ("name = 'mydiabetes_backup.zip' and mimeType='application/zip' and 'root' in parents")
            .setOrderBy("createdTime desc")
            .setPageSize(1)
            .setFields("files(id,name,createdTime)")
            .execute()
        result.files.firstOrNull()
    }

    suspend fun importAllFromZip(fileId: String): Map<String, String> = withContext(Dispatchers.IO) {
        val out = ByteArrayOutputStream()
        drive.files().get(fileId)
            .executeMediaAndDownloadTo(out)
        val zipBytes = out.toByteArray()

        val rawMap: Map<String, ByteArray> = ZipUtils.unpackZip(zipBytes)

        rawMap.mapValues { (_, bytes) ->
            String(bytes, Charsets.UTF_8)
        }
    }
}
