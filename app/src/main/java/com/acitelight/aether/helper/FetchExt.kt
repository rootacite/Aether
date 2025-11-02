package com.acitelight.aether.helper

import com.tonyodev.fetch2.Download

enum class DownloadType
{
    Comic,
    VideoMainFile,
    VideoGallery, // Include cover
    VideoSubtitle,
    Unknown
}

fun Download.getType(): DownloadType
{
    return when(this.extras.getString("type", ""))
    {
        "comic" -> DownloadType.Comic
        "main" -> DownloadType.VideoMainFile
        "cover", "gallery" -> DownloadType.VideoGallery
        "subtitle" -> DownloadType.VideoSubtitle
        else -> DownloadType.Unknown
    }
}

fun Download.getVideoClass(): String
{
    return this.extras.getString("class", "")
}

fun Download.getId(): String
{
    return this.extras.getString("id", "")
}

fun Download.getName(): String
{
    return this.extras.getString("name", "")
}

fun Download.getGroup(): String
{
    return this.extras.getString("group", "")
}

fun Download.getComicCover(): String
{
    return this.extras.getString("cover", "")
}