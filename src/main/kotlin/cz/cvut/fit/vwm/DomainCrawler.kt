package cz.cvut.fit.vwm

import cz.cvut.fit.vwm.service.PageService
import edu.uci.ics.crawler4j.crawler.WebCrawler
import edu.uci.ics.crawler4j.parser.HtmlParseData
import edu.uci.ics.crawler4j.url.WebURL
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject
import java.util.regex.Pattern
import edu.uci.ics.crawler4j.crawler.Page as CrawledPage


class DomainCrawler : WebCrawler() {


    val service by inject<PageService>(PageService::class.java)

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "https://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    override fun shouldVisit(referringPage: CrawledPage?, url: WebURL): Boolean {
        val href: String = url.url.toLowerCase()
        return (!FILTERS.matcher(href).matches()
                && href.startsWith("https://cs.wikipedia.org/"))
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    override fun visit(page: CrawledPage) {
        val url: String = page.webURL.url
        println("URL: $url")
        if (page.parseData is HtmlParseData) {
            val htmlParseData: HtmlParseData = page.parseData as HtmlParseData

            val text: String = htmlParseData.text
            val html: String = htmlParseData.html

            val outlinks: Set<WebURL> = htmlParseData.outgoingUrls

            println("Text length: " + text.length)
            println("Html length: " + html.length)
            println("Number of outgoing links: " + outlinks.size)

            runBlocking {
                service.updatePage(url, outlinks.size, htmlParseData.title, text)
                service.updateInlinks(outlinks)
            }
        }
    }


    companion object {
        private val FILTERS: Pattern = Pattern.compile(
            ".*(\\.(css|js|gif|jpg"
                    + "|png|mp3|mp4|zip|gz))$"
        )
    }
}
