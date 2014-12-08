package helpers

import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}

object PDFTestHelper {
  def createDummyPdf(output: String) = {
    // Create a document and add a page to it
    val document = new PDDocument()
    val page = new PDPage()
    document.addPage(page)

    // Create a new font object selecting one of the PDF base fonts
    val font = PDType1Font.HELVETICA_BOLD

    // Start a new content stream which will "hold" the to be created content
    val contentStream = new PDPageContentStream(document, page)

    // Define a text content stream using the selected font, moving the cursor and drawing the text "Hello World"
    contentStream.beginText()
    contentStream.setFont(font, 12)
    contentStream.moveTextPositionByAmount(100, 700)
    contentStream.drawString("Hello World")
    contentStream.endText()

    // Make sure that the content stream is closed:
    contentStream.close()

    // Save the results and ensure that the document is properly closed:
    document.save(output)
    document.close()
  }
}
