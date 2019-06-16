import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.{HtmlButton, HtmlPage}

object Main extends App {

  lazy val wc = new WebClient()
  lazy val BASE_URL = "https://www.crowdcube.com"
  lazy val COMPANY_URI_RGX = "href=\"/companies/(.*)\"".r
  lazy val COMPANY_DETAILS_RGX = ("<strong>[\\S\\s]*?Industry type[\\S\\s]*?</strong>[\\S\\s]*?" +
    "<p class=\"cc-companyDetails__itemContent\">([\\S\\s]*?)" +
    "</p>[\\S\\s]*?<address>([\\S\\s]*?)</address>").r
  lazy val TEAM_MEMBERS_RGX = ("<span class=\"cc-company__person\">([\\S\\s]*?)</span>" +
    "[\\S\\s]*?<td>([\\S\\s]*?)</td>").r
  lazy val CAMPAIGN_URI_RGX = "<a href=\"https://www.crowdcube.com/investment/(.*)\">".r
  lazy val SLEEP_TIME = 4500

  var scrapeCount = 0

  def scrapeAll =
    getCompanyURIs(Int.MaxValue).map(scrapeCompany).toList

  /** @param fetchCount Rounded up to nearest modulus of 15, i.e. 22 fetches 30 */
  def getCompanyURIs(fetchCount: Int) = {
    val page: HtmlPage = wc.getPage(s"$BASE_URL/companies/")
    val loadMoreBtn = page.getElementById("loadMoreCompanies").asInstanceOf[HtmlButton]

    var prevlinkCount = 0
    var currentLinkCount = -1
    var identicalCount = 0

    while (currentLinkCount <= fetchCount && identicalCount < 2) {
      currentLinkCount = COMPANY_URI_RGX.findAllIn(page.asXml).length
      if (prevlinkCount == currentLinkCount) identicalCount += 1
      prevlinkCount = currentLinkCount


      loadMoreBtn.click
      println(s"Fetched $currentLinkCount companies, sleeping for ${SLEEP_TIME / 1000.0} seconds")
      wc.waitForBackgroundJavaScript(SLEEP_TIME)
    }

    COMPANY_URI_RGX.findAllIn(page.asXml).matchData.map(_.group(1))
  }

  def scrapeCompany(companyUri: String): Company = {
    scrapeCount += 1
    println(s"$scrapeCount - Scraping $companyUri")
    val html = wc.getPage(s"$BASE_URL/companies/$companyUri").asInstanceOf[HtmlPage].asXml
    val matchList = COMPANY_DETAILS_RGX.findAllIn(html).matchData.toList

    if (matchList.isEmpty) {
      println(s"Information not available for $companyUri")
      return null
    }
    val matches = matchList.head

    new Company(frmtContent(matches.group(1)), frmtContent(matches.group(2)),
      scrapeTeam(html), scrapeCampaigns(html))
  }

  //Removes unnecessary/html elements from data
  def frmtContent(data: String) = data.replace("  ", "")
    .replace("<br/>", " , ").replace("\r\n", "").trim

  def scrapeTeam(html: String) =
    TEAM_MEMBERS_RGX.findAllIn(html).matchData.map(tm =>
      new TeamMember(
        frmtContent(tm.group(1)), Gender.Indeterminate,
        tm.group(2).toUpperCase.contains("CEO"))).toSet


  def scrapeCampaigns(html: String): Set[Campaign] = {
    val uris = CAMPAIGN_URI_RGX.findAllIn(html).matchData.toList
      .map(m => m.group(1)).toSet

    uris.map(uri => wc.getPage(s"$BASE_URL/investment/$uri").asInstanceOf[HtmlPage].asXml)

    null
  }
}
