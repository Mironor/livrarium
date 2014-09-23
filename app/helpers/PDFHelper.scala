package helpers

import java.io.File
import javax.imageio.ImageIO
import org.apache.pdfbox.io.RandomAccessFile
import org.apache.pdfbox.pdmodel.{PDPage, PDDocument}

object PDFHelper {
  def extractImageFromPdf(pdfPath: String, outputPath: String) = {
    val input = new File(pdfPath)
    val inputRABuf = new RandomAccessFile(input, "rw")
    val output = new File(outputPath)

    // Sequential load does not work on some pdfs
    val document = PDDocument.loadNonSeq(input, inputRABuf)
    val pages = document.getDocumentCatalog.getAllPages
    val titlePage = pages.get(0).asInstanceOf[PDPage]

    val titlePageImage = titlePage.convertToImage()
    ImageIO.write(titlePageImage, "jpg", output)
  }

  def getTotalPages(pdfPath: String) = {
    val input = new File(pdfPath)

    val document = PDDocument.load(input)
    document.getNumberOfPages
  }
}
