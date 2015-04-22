package controllers

import java.io.File
import javax.imageio.ImageIO

import com.mohiva.play.silhouette.impl.authenticators.SessionAuthenticator
import com.mohiva.play.silhouette.test._
import fixtures.{FolderFixture, UserFixture}
import helpers.{BookFormatHelper, LivrariumSpecification, PDFTestHelper, RandomIdGenerator}
import models.{Folder, FolderContents}
import org.apache.commons.io.FileUtils
import org.specs2.matcher.{FileMatchers, ThrownMessages}
import org.specs2.specification.AroundExample
import play.api.Play
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.FilePart
import play.api.test._
import services.{BookService, FolderService}

class CloudSpec extends LivrariumSpecification with FileMatchers with AroundExample with ThrownMessages {

  protected def bootstrapFixtures(): Unit = {
    await(UserFixture.initFixture())
    await(FolderFixture.initFixture())
    cleanUploadDirectories()
  }

  private def cleanUploadDirectories() = {

    val applicationPath = Play.current.path
    val uploadFolderPath = applicationPath + inject[String](identified by "folders.uploadPath")
    val generatedImageFolderPath = applicationPath + inject[String](identified by "folders.generatedImagePath")

    val uploadFolder = new File(uploadFolderPath)
    val generatedImageFolder = new File(generatedImageFolderPath)

    FileUtils.forceMkdir(uploadFolder)
    FileUtils.forceMkdir(generatedImageFolder)

    FileUtils.cleanDirectory(uploadFolder)
    FileUtils.cleanDirectory(generatedImageFolder)
  }

  "Cloud controller" should {

    "return folder tree" in {
      // Given
      val request = FakeRequest().withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val cloudController = new Cloud

      // When
      val result = cloudController.getFolderTree()(request)

      // Then
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")

      val rootChildren = contentAsJson(result).as[List[Folder]]
      rootChildren must have size 2

      val sub1 = rootChildren.head
      sub1.name must beEqualTo(FolderFixture.sub1Name)
      sub1.children.head.name must beEqualTo(FolderFixture.sub1sub1Name)
      sub1.children(1).name must beEqualTo(FolderFixture.sub1sub2Name)

      val sub2 = rootChildren(1)
      sub2.name must beEqualTo(FolderFixture.sub2Name)
    }

    "return root's content" in {
      // Given
      val request = FakeRequest().withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val cloudController = new Cloud

      // When
      val result = cloudController.getRootContent()(request)

      // Then
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      val folderContents = contentAsJson(result).as[FolderContents]
      folderContents.folders must have size 2
      folderContents.folders(0).name must beEqualTo(FolderFixture.sub1Name)
      folderContents.folders(1).name must beEqualTo(FolderFixture.sub2Name)
    }

    "return some folder's (other than root) content" in {
      // Given
      val request = FakeRequest().withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val cloudController = new Cloud

      // When
      val result = cloudController.getContent(FolderFixture.sub1Id)(request)

      // Then
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      contentAsJson(result).as[FolderContents].folders must have size 2
    }

    "create new folder" in {
      // Given
      val folderService = inject[FolderService]

      val newFolderName = "testCreateFolder"

      val requestJson = Json.obj(
        "idParent" -> JsNumber(FolderFixture.sub1Id),
        "name" -> JsString(newFolderName)
      )

      val request = FakeRequest(Helpers.POST, "", FakeHeaders(), requestJson)
        .withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)

      val cloudController = new Cloud

      // When
      await(cloudController.createFolder()(request))

      // Then
      val userFolderTree = await(folderService.retrieveChildren(UserFixture.testUser, FolderFixture.sub1Id))
      userFolderTree must have size 3

      val createdFolder = userFolderTree(2)
      createdFolder.name mustEqual newFolderName
    }

    "upload a pdf" in {
      // Given
      val userId = UserFixture.testUserId

      val randomIdGenerator = inject[RandomIdGenerator]
      val generatedBookIdentifier = randomIdGenerator.generateBookId() // it generates the same id each time due to the injection

      val applicationPath = Play.current.path
      val uploadPath = applicationPath + inject[String](identified by "folders.uploadPath")

      val request = generateRequestWithUploadedPdfFile()

      val cloudController = new Cloud

      // When
      await(cloudController.upload(FolderFixture.sub1Id)(request))

      // Then
      val uploadedPdf = new File(s"$uploadPath/$userId/$generatedBookIdentifier.pdf")
      uploadedPdf must exist
    }

    def generateRequestWithUploadedPdfFile() = {
      val file = generateTemporaryPdfFile()

      val tempFile = TemporaryFile(file)
      val uploadInputName = inject[String](identified by "books.uploadInputName")
      val part = FilePart[TemporaryFile](key = uploadInputName, filename = file.getName, contentType = Some("application/pdf"), ref = tempFile)
      val formData = MultipartFormData(dataParts = Map(), files = Seq(part), badParts = Seq(), missingFileParts = Seq())

      FakeRequest(Helpers.POST, "", FakeHeaders(), formData).withAuthenticator[SessionAuthenticator](UserFixture.testUserLoginInfo)
    }

    def generateTemporaryPdfFile() = {
      val fileName = "book.pdf"
      val filePath = s"/tmp/$fileName"
      PDFTestHelper.createDummyPdf(filePath)
      new File(filePath)
    }

    "generate image (with good width/height) from uploaded pdf" in {
      // Given
      val userId = UserFixture.testUserId

      val randomIdGenerator = inject[RandomIdGenerator]
      val generatedBookId = randomIdGenerator.generateBookId() // it generates the same id each time due to the injection

      val applicationPath = Play.current.path
      val generatedImagePath = applicationPath + inject[String](identified by "folders.generatedImagePath")

      val request = generateRequestWithUploadedPdfFile()

      val cloudController = new Cloud

      // When
      await(cloudController.upload(FolderFixture.sub1Id)(request))

      // Then
      val generatedPdfImage = new File(s"$generatedImagePath/$userId/$generatedBookId.jpg")
      generatedPdfImage must exist

      val bufferedGeneratedPdfImage = ImageIO.read(generatedPdfImage)
      bufferedGeneratedPdfImage.getWidth mustEqual inject[Int](identified by "books.thumbnailWidth")
      bufferedGeneratedPdfImage.getHeight mustEqual inject[Int](identified by "books.thumbnailHeight")

      val generatedPdfSmallImage = new File(s"$generatedImagePath/$userId/$generatedBookId-small.jpg")
      generatedPdfSmallImage must exist

      val bufferedGeneratedPdfSmallImage = ImageIO.read(generatedPdfSmallImage)
      bufferedGeneratedPdfSmallImage.getWidth mustEqual inject[Int](identified by "books.smallThumbnailWidth")
      bufferedGeneratedPdfSmallImage.getHeight mustEqual inject[Int](identified by "books.smallThumbnailHeight")
    }

    "generate corresponding model in the database from uploaded pdf" in {
      // Given
      val bookService = inject[BookService]

      val randomIdGenerator = inject[RandomIdGenerator]
      val generatedBookId = randomIdGenerator.generateBookId() // it generates the same id each time due to the injection

      val request = generateRequestWithUploadedPdfFile()

      val cloudController = new Cloud

      // When
      await(cloudController.upload(FolderFixture.sub1Id)(request))
      val books = await(bookService.retrieveAllFromFolder(UserFixture.testUser, FolderFixture.sub1Id))

      // Then
      books must have size 1

      val book = books.head
      book.identifier mustEqual generatedBookId
      book.format mustEqual BookFormatHelper.PDF
      book.name mustEqual "book"
    }
  }
}
