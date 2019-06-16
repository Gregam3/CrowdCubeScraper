import Main._
import junit.framework.TestCase
import org.junit.Assert.assertEquals

class ScraperTest extends TestCase {
  def testIfMultipleCompanyBatchesRetrieved: Unit = assertEquals(getCompanyURIs(30).length, 30)

  def testIfCompanyIsScraped: Unit =
    Array("first-natural-brands", "tog-knives", "joblab", "hoptroff-london-limited", "monzo")
      .map(scrapeCompany).filter(_ != null).forall(c => !c.industryType.isEmpty && !c.location.isEmpty && c.team.nonEmpty)

  def testIfAllScrape: Unit = assert(scrapeAll.length >= 733)


}
