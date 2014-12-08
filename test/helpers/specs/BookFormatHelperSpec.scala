package helpers.specs

import helpers.{BookFormatHelper, LivrariumSpecification}

class BookFormatHelperSpec extends LivrariumSpecification {

  "Book Format Helper" should {
    "detect pdf type from pdf application type" in {
      // Given
      val pdfStandardContentType = "application/pdf"
      val pdfCompatibilityContentType = "application/x-pdf"

      // When
      val pdfStandard = BookFormatHelper.normalize(pdfStandardContentType)
      val pdfCompatibility = BookFormatHelper.normalize(pdfCompatibilityContentType)

      // Then
      pdfStandard must beEqualTo(BookFormatHelper.PDF)
      pdfCompatibility must beEqualTo(BookFormatHelper.PDF)
    }

    "detect 'None' type when content type is not handled" in {
      // Given
      val dummyType = "application/dummy"

      // When
      val dummy = BookFormatHelper.normalize(dummyType)

      // Then
      dummy must beEqualTo(BookFormatHelper.NONE)
    }
  }

}
