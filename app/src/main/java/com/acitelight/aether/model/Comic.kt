package com.acitelight.aether.model

import com.acitelight.aether.service.ApiClient

class Comic(
    val comic: ComicResponse,
    val id: String,
    val token: String
)
{
    fun getPage(pageNumber: Int): String
    {
        return "${ApiClient.base}api/image/$id/${comic.list[pageNumber]}?token=$token"
    }

    fun getPage(pageName: String): String?
    {
        val v = comic.list.indexOf(pageName)
        if(v >= 0)
        {
            return getPage(v)
        }
        return null
    }

    fun getPageIndex(pageName: String): Int
    {
        return comic.list.indexOf(pageName)
    }

    fun getChapterLength(pageName: String): Int
    {
        var v = comic.list.indexOf(pageName)
        if(v >= 0)
        {
            var r: Int = 1
            v+=1
            while(v < comic.list.size && !comic.bookmarks.any{
                x -> x.page == comic.list[v]
                }){
                r++
                v+=1
            }

            return r
        }

        return -1
    }

    fun getPageChapterIndex(page: Int): Pair<BookMark, Int>?
    {
        var p = page
        while(p >= 0 && !comic.bookmarks.any{ x -> x.page == comic.list[p] })
        {
            p--
        }
        for(i in comic.bookmarks)
        {
            if(i.page == comic.list[p])
            {
                return Pair(i, page - comic.list.indexOf(i.page) + 1)
            }
        }

        return null
    }
}