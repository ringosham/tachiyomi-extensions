package eu.kanade.tachiyomi.extension.ja.mangaraw

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class MangaRawFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        Comick(),
        MangaPro()
    )
}

// Comick has a slightly different layout in html, even though it looks exactly the same to MangaRaw visually
class Comick : MangaRaw(
    "Comick",
    "https://comick.top",
    "#main > article > div > div > div.entry-content > center > p > img"
)

class MangaPro : MangaRaw("MangaPro", "https://mangapro.top")
