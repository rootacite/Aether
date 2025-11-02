package com.acitelight.aether.helper

import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import okio.buffer
import okio.source
import java.io.File
import java.io.FileNotFoundException
import java.util.zip.ZipFile

class ZipEntryFetcher(
    private val zipFile: File,
    private val entryPath: String,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        var zipFileHandle: ZipFile? = null
        try {
            val zf = ZipFile(zipFile)
            zipFileHandle = zf

            val entry = zf.getEntry(entryPath)
                ?: throw FileNotFoundException("Entry not found in zip: $entryPath")

            val inputStream = zf.getInputStream(entry)

            val source = object : okio.ForwardingSource(inputStream.source()) {
                override fun close() {
                    super.close()
                    zipFileHandle.close()
                }
            }

            return SourceFetchResult(
                source = ImageSource(source.buffer(), options.fileSystem),
                mimeType = null,
                dataSource = DataSource.DISK
            )
        } catch (e: Exception) {
            zipFileHandle?.close()
            return null
        }
    }
}