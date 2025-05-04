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

    suspend fun exportCsv(csv: String): File = withContext(Dispatchers.IO) {
        val meta = File().apply {
            name = "export_${System.currentTimeMillis()}.csv"
            parents = listOf("root")
        }
        val content = ByteArrayContent.fromString("text/csv", csv)
        drive.files().create(meta, content)
            .setFields("id,name")
            .execute()
    }

    suspend fun importCsv(fileId: String): String = withContext(Dispatchers.IO) {
        val out = ByteArrayOutputStream()
        drive.files().get(fileId)
            .executeMediaAndDownloadTo(out)
        out.toString(Charsets.UTF_8.name())
    }

    suspend fun findLatestCsv(): File? = withContext(Dispatchers.IO) {
        val response = drive.files().list()
            .setSpaces("drive")
            .setQ("mimeType='text/csv' and name contains 'export_' and trashed=false")
            .setOrderBy("createdTime desc")
            .setPageSize(1)
            .setFields("files(id,name)")
            .execute()
        response.files.firstOrNull()
    }
}
