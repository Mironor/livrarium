package helpers

object BookFormatHelper {
  val NONE = "none"
  val PDF = "pdf"
  val EPUB = "epub"
  val MOBI = "mobi"

  val supportedTypes = List(PDF)

  def normalize(rawType: String) = {
    rawType match {
      // should be identity function if type is already normalized
      case NONE => NONE
      case PDF => PDF
      case EPUB => EPUB
      case MOBI => MOBI

      // standard types (http://www.iana.org/assignments/media-types/media-types.xhtml)
      case "application/pdf" => PDF

      // compatibility types
      case "application/x-pdf" => PDF
    }
  }

  def isSupportedType(fileType: String) = supportedTypes.contains(fileType)
}
