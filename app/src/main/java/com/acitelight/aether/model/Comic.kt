package com.acitelight.aether.model

import com.acitelight.aether.service.ApiClient

class Comic(
    val comic: ComicResponse,
    val id: String
)
{
    fun getPage(pageNumber: Int, api: ApiClient): String
    {
        return "${api.getBase()}api/image/$id/${comic.list[pageNumber]}"
    }

    fun getPage(pageName: String, api: ApiClient): String?
    {
        val v = comic.list.indexOf(pageName)
        if(v >= 0)
        {
            return getPage(v, api)
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
            var r = 1
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

    fun getPageChapterIndex(page: Int): Pair<BookMark, Int>
    {
        var p = page
        while(p >= 0 && !comic.bookmarks.any{ x -> x.page == comic.list[p] })
        {
            p--
        }
        if(p < 0) return Pair(BookMark(name="null", page=comic.list[0]), page + 1)
        for(i in comic.bookmarks)
        {
            if(i.page == comic.list[p])
            {
                return Pair(i, page - comic.list.indexOf(i.page) + 1)
            }
        }

        return Pair(BookMark(name="null", page=comic.list[0]), page + 1)
    }
}