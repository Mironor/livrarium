package helpers

import java.io.File
import javax.imageio.ImageIO

import org.apache.pdfbox.io.RandomAccessFile
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}

import scala.util.control.Exception._

/**
 * Static object that contains helper methods for working with pdf files
 */
object PDFHelper {

  val imageFileType = "jpg"

  /**
   * Using PDFBox extract first page as an image of the supplied pdf
   * @param pdfPath input pdf
   * @param outputPath output image
   * @return
   */
  def extractImageFromPdf(pdfPath: String, outputPath: String) = {
    val output = new File(outputPath)

    withNonSeqLoadedPdf(pdfPath) {
      document =>
        val pages = document.getDocumentCatalog.getAllPages
        val titlePage = pages.get(0).asInstanceOf[PDPage]

        val titlePageImage = titlePage.convertToImage()
        ImageIO.write(titlePageImage, imageFileType, output)
    }
  }

  private def withNonSeqLoadedPdf[T](pdfPath: String)(f: PDDocument => T): T = {
    val input = new File(pdfPath)
    val inputRABuf = new RandomAccessFile(input, "rw")

    // Sequential load does not work on some PDFs, using non-sequential instead
    val document = PDDocument.loadNonSeq(input, inputRABuf)

    withCloseable(document)(f)
  }

  private def withCloseable[T <: {def close() : Unit}, K](document: T)(f: T => K) = {
    allCatch.andFinally {
      document.close()
    }.apply(f(document))
  }

  /**
   * Returns the number of pages in supplied pdf
   * @param pdfPath path to the pdf
   * @return
   */
  def getTotalPages(pdfPath: String): Int = withSeqLoadedPdf(pdfPath)(_.getNumberOfPages)

  private def withSeqLoadedPdf[T](pdfPath: String)(f: PDDocument => T): T = {
    val input = new File(pdfPath)

    // Sequential load does not work on some PDFs, using non-sequential instead
    val document = PDDocument.load(input)

    withCloseable(document)(f)
  }
}

