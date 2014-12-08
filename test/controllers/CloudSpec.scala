package controllers

import java.io.File
import javax.imageio.ImageIO

import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test._
import globals.TestGlobal
import helpers.{PDFTestHelper, LivrariumSpecification, RandomIdGenerator}
import org.apache.commons.io.FileUtils
import org.specs2.execute.AsResult
import org.specs2.matcher.{FileMatchers, ThrownMessages}
import org.specs2.specification.AroundExample
import play.api.Play
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test._
import services.{FolderContents, FolderService, UserService}

class CloudSpec extends LivrariumSpecification with FileMatchers with AroundExample with ThrownMessages {

  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      val userService = inject[UserService]
      await(userService.saveWithLoginInfo(TestGlobal.testUser))

      cleanUploadDirectories()

      AsResult(t)
    }
  }

  private def cleanUploadDirectories() = {

    val applicationPath = Play.current.path
    val uploadFolderPath = applicationPath + inject[String](identified by "folders.uploadFolder")
    val generatedImageFolderPath = applicationPath + inject[String](identified by "folders.uploadFolder")

    val uploadFolder = new File(uploadFolderPath)
    val generatedImageFolder = new File(generatedImageFolderPath)

    FileUtils.forceMkdir(uploadFolder)
    FileUtils.forceMkdir(generatedImageFolder)

    FileUtils.cleanDirectory(uploadFolder)
    FileUtils.cleanDirectory(generatedImageFolder)
  }

  "Cloud controller" should {

    "return root's content" in {
      // Given
      val folderService = inject[FolderService]
      await(folderService.createRootForUser(TestGlobal.testUser))
      await(folderService.appendToRoot(TestGlobal.testUser, "sub1"))
      await(folderService.appendToRoot(TestGlobal.testUser, "sub2"))


      val request = FakeRequest().withAuthenticator[SessionAuthenticator](TestGlobal.testUser.loginInfo)

      val cloudController = new Cloud

      // When
      val result = cloudController.getRootContent()(request)

      // Then
      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result).as[FolderContents].folders must have size 2
    }

    "return some folder's (other than root) content" in {
      // Given
      val folderService = inject[FolderService]
      await(folderService.createRootForUser(TestGlobal.testUser))
      await(folderService.appendToRoot(TestGlobal.testUser, "sub1"))

      val sub2Folder = await(folderService.appendToRoot(TestGlobal.testUser, "sub2"))
      val sub2FolderId = sub2Folder.id.getOrElse(fail("sub-folder's id is not defined"))
      await(folderService.appendTo(TestGlobal.testUser, sub2FolderId, "subSub1"))


      val request = FakeRequest().withAuthenticator[SessionAuthenticator](TestGlobal.testUser.loginInfo)

      val cloudController = new Cloud

      // When
      val result = cloudController.getContent(sub2FolderId)(request)

      // Then
      status(result) must equalTo(OK)
      contentType(result) must beSome("application/json")
      contentAsJson(result).as[FolderContents].folders must have size 1
    }

    "create new folder" in {
      // Given
      val folderService = inject[FolderService]
      await(folderService.createRootForUser(TestGlobal.testUser))

      val requestJson = Json.obj(
        "idParent" -> JsNumber(1),
        "name" -> JsString("testCreateFolder")
      )
      val request = FakeRequest(Helpers.POST, "", FakeHeaders(), requestJson)
        .withAuthenticator[SessionAuthenticator](TestGlobal.testUser.loginInfo)

      val cloudController = new Cloud

      // When
      await(cloudController.createFolder()(request))

      // Then
      val userFolderTree = await(folderService.retrieveUserFolderTree(TestGlobal.testUser))
      userFolderTree must have size 1
    }

    "upload a pdf" in {
      // Given
      val userId = TestGlobal.testUser.id.getOrElse(fail("User's id is not defined"))

      val randomIdGenerator = inject[RandomIdGenerator]
      val generatedBookId = randomIdGenerator.generateBookId() // it generates the same id each time due to the injection

      val applicationPath = Play.current.path
      val uploadFolder = applicationPath + inject[String](identified by "folders.uploadFolder")

      val file = generateTemporaryPdfFile()

      val request = generateRequestFromPdfFile(file)

      val cloudController = new Cloud

      // When
      await(cloudController.upload()(request))

      // Then
      val uploadedPdf = new File(s"$uploadFolder/$userId/$generatedBookId.pdf")
      uploadedPdf must exist
    }

    def generateTemporaryPdfFile() = {
      val fileName = "book.pdf"
      val filePath = s"/tmp/$fileName"
      PDFTestHelper.createDummyPdf(filePath)
      new File(filePath)
    }

    def generateRequestFromPdfFile(file: File) = {
      val tempFile = TemporaryFile(file)
      val uploadInputName = inject[String](identified by "books.uploadInputName")
      val part = FilePart[TemporaryFile](key = uploadInputName, filename = file.getName, contentType = Some("application/pdf"), ref = tempFile)
      val formData = MultipartFormData(dataParts = Map(), files = Seq(part), badParts = Seq(), missingFileParts = Seq())
      FakeRequest(Helpers.POST, "", FakeHeaders(), formData).withAuthenticator[SessionAuthenticator](TestGlobal.testUser.loginInfo)
    }

    "generate image from uploaded pdf" in {
      // Given
      val userId = TestGlobal.testUser.id.getOrElse(fail("User's id is not defined"))

      val randomIdGenerator = inject[RandomIdGenerator]
      val generatedBookId = randomIdGenerator.generateBookId() // it generates the same id each time due to the injection

      val applicationPath = Play.current.path
      val generatedImageFolder = applicationPath + inject[String](identified by "folders.generatedImageFolder")

      val file = generateTemporaryPdfFile()

      val request = generateRequestFromPdfFile(file)

      val cloudController = new Cloud

      // When
      await(cloudController.upload()(request))

      // Then
      val generatedPdfImage = new File(s"$generatedImageFolder/$userId/$generatedBookId.jpg")
      generatedPdfImage must exist

      val bufferedGeneratedPdfImage = ImageIO.read(generatedPdfImage)
      bufferedGeneratedPdfImage.getWidth must beEqualTo(inject[Int](identified by "books.thumbnailWidth"))
      bufferedGeneratedPdfImage.getHeight must beEqualTo(inject[Int](identified by "books.thumbnailHeight"))

      val generatedPdfSmallImage = new File(s"$generatedImageFolder/$userId/$generatedBookId-small.jpg")
      generatedPdfSmallImage must exist

      val bufferedGeneratedPdfSmallImage = ImageIO.read(generatedPdfSmallImage)
      bufferedGeneratedPdfSmallImage.getWidth must beEqualTo(inject[Int](identified by "books.smallThumbnailWidth"))
      bufferedGeneratedPdfSmallImage.getHeight must beEqualTo(inject[Int](identified by "books.smallThumbnailHeight"))
    }

    /*
    "generate corresponding model in the database from uploaded pdf" in {
      // Given
      val bookService = inject[BookService]

      val userId = TestGlobal.testUser.id.getOrElse(fail("User's id is not defined"))

      val randomIdGenerator = inject[RandomIdGenerator]
      val generatedBookId = randomIdGenerator.generateBookId() // it generates the same id each time due to the injection

      val file = generateTemporaryPdfFile()

      val request = generateRequestFromPdfFile(file)

      val cloudController = new Cloud

      // When
      await(cloudController.upload()(request))

      // Then


    }
    */
  }
}
