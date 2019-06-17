import Main._
import junit.framework.TestCase
import org.junit.Assert.assertEquals

class ScraperTest extends TestCase {
  def testIfMultipleCompanyBatchesRetrieved: Unit = assertEquals(getCompanyURIs(30).length, 30)

  def testIfCompanyIsScraped: Unit =
    Array("tog-knives", "first-natural-brands", "joblab", "hoptroff-london-limited", "monzo")
      .map(scrapeCompany).filter(_ != null).forall(c =>
      !c.industryType.isEmpty && !c.location.isEmpty &&
        c.team.nonEmpty && c.campaign.forall(c =>
        c.investorCount > 0 && c.fundingReceivedPercentage.nonEmpty &&
          c.fundingSought.nonEmpty))

  def testIfAllScrape: Unit = assert(scrapeAll.length >= 733)

  def testIfCampaignScrap: Unit = {

  }
}
