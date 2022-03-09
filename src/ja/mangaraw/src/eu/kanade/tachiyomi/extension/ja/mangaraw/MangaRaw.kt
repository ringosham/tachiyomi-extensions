package eu.kanade.tachiyomi.extension.ja.mangaraw

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Headers
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

abstract class MangaRaw(
    override val name: String,
    override val baseUrl: String,
    private val imageSelector: String = ".wp-block-image > img"
) : ParsedHttpSource() {

    override val lang = "ja"

    override val supportsLatest = true

    override fun headersBuilder(): Headers.Builder {
        return super.headersBuilder().add("Referer", baseUrl)
    }

    // comick.top doesn't have a popular manga page
    // redirect to latest manga request
    override fun popularMangaRequest(page: Int): Request {
        return if (name == "Comick")
            latestUpdatesRequest(page)
        else
            GET("$baseUrl/seachlist/page/$page/?cat=-1", headers)
    }

    override fun popularMangaSelector() = "article"

    override fun popularMangaFromElement(element: Element) = SManga.create().apply {
        setUrlWithoutDomain(element.select("a:has(img)").attr("href"))
        title = element.select("img").attr("alt").substringBefore("(RAW – Free)").trim()
        thumbnail_url = element.select("img").attr("abs:src")
    }

    override fun popularMangaNextPageSelector() = ".next.page-numbers"

    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/page/$page", headers)

    override fun latestUpdatesSelector() = popularMangaSelector()

    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)

    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList) =
        GET("$baseUrl/page/$page/?s=$query", headers)

    override fun searchMangaSelector() = popularMangaSelector()

    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)

    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    override fun mangaDetailsParse(document: Document) = SManga.create().apply {
        // All manga details are located in the same <p> tag
        // So here are some jank way of extracting them
        // comick.top doesn't have author or genre info
        if (name != "Comick") {
            // Extract the author, take out the colon and quotes
            author = document.select("#main > article > div > div > div > div > p").html()
                .substringAfter("</strong>").substringBefore("<br>").drop(1)
            genre = document.select("#main > article > div > div > div > div > p > a")
                .joinToString(separator = ", ", transform = { it.text() })
        }
        description = document.select("#main > article > div > div > div > div > p").html()
            .substringAfterLast("<br>")
        thumbnail_url = document.select(".wp-block-image img").attr("abs:src")
    }

    override fun chapterListSelector() = ".chapList a"

    override fun chapterFromElement(element: Element) = SChapter.create().apply {
        setUrlWithoutDomain(element.attr("href"))
        name = element.text().trim()
    }

    override fun pageListParse(document: Document): List<Page> {
        return document.select(imageSelector).mapIndexed { i, element ->
            val attribute = if (element.hasAttr("data-src")) "data-src" else "src"
            Page(i, "", element.attr(attribute))
        }
    }

    override fun imageUrlParse(document: Document): String =
        throw UnsupportedOperationException("Not Used")

    override fun getFilterList() = FilterList()
}
