package com.acitelight.aether.helper

import coil3.ImageLoader
import coil3.Uri
import coil3.fetch.Fetcher
import coil3.request.Options
import java.io.File

class ZipUriFetcherFactory : Fetcher.Factory<Uri> {
    override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
        val isZipUri = data.scheme == "file" &&
                data.path?.endsWith(".zip", ignoreCase = true) == true &&
                data.query != null

        if (!isZipUri) {
            return null
        }

        val zipFilePath = data.path!!
        val entryPath = data.query!!.split("=").last()

        return ZipEntryFetcher(File(zipFilePath), entryPath, options)
    }
}