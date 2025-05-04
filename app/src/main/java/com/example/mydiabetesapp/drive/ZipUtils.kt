package com.example.mydiabetesapp.drive

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ZipUtils {

    /**
     * Упаковывает набор пар (имя файла → байты) в один ZIP‑архив и возвращает его как ByteArray.
     */
    fun packToZip(entries: Map<String, ByteArray>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            for ((name, data) in entries) {
                val entry = ZipEntry(name)
                zos.putNextEntry(entry)
                zos.write(data)
                zos.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    /**
     * Распаковывает ZIP‑архив (в виде ByteArray) и возвращает Map<имя файла, его байты>.
     */
    fun unpackZip(zipBytes: ByteArray): Map<String, ByteArray> {
        val result = mutableMapOf<String, ByteArray>()
        ByteArrayInputStream(zipBytes).use { bais ->
            ZipInputStream(bais).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val name = entry.name
                    val buffer = ByteArrayOutputStream()
                    zis.copyTo(buffer)
                    result[name] = buffer.toByteArray()
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
        return result
    }
}
