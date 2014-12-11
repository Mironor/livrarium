package services

import globals.TestGlobal
import helpers.{BookFormatHelper, RandomIdGenerator, LivrariumSpecification}
import org.specs2.execute.AsResult
import org.specs2.matcher.ThrownMessages
import org.specs2.specification.AroundExample
import play.api.test.FakeApplication

class BookServiceSpec extends LivrariumSpecification with AroundExample with ThrownMessages {

  def randomIdGenerator = inject[RandomIdGenerator]

  /**
   * This automatically handles up and down evolutions at the beginning and at the end of a spec respectively
   */
  def around[T: AsResult](t: => T) = {
    val app = FakeApplication(withGlobal = Some(TestGlobal), additionalConfiguration = inMemoryDatabase())
    running(app) {
      val userService = inject[UserService]
      await(userService.saveWithLoginInfo(TestGlobal.testUser))
      val folder = prepareFolder()
      prepareTestBooks(folder)

      AsResult(t)
    }
  }

  def prepareFolder() = {
    val folderService = new FolderService

    await(folderService.createRootForUser(TestGlobal.testUser))
    await(folderService.appendToRoot(TestGlobal.testUser, "Sub1"))
  }

  def prepareTestBooks(folderToAddBooks: Folder) = {

    val bookService = new BookService

    val book = Book(
      None,
      randomIdGenerator.generateBookId(),
      "book",
      BookFormatHelper.PDF
    )

    bookService.save(TestGlobal.testUser, book)
    bookService.addToFolder(TestGlobal.testUser, book, folderToAddBooks)
  }

  "Book Service" should {
    "create new book" in {
      // Given
      val bookService = new BookService

      // When
      val books = await(bookService.retrieveAll(TestGlobal.testUser))

      // Then
      books must have size 1
    }
    /*

    "retrieve all books from a folder" in {
      // Given
      val bookService = new BookService
      val folder = getBookFolder()

      // When
      val books = await(bookService.retrieveAllFromFolder(TestGlobal.testUser, folder))

      // Then
      books must have size 1
    }


    def getBookFolder(): Folder = {
      val folderService = inject[FolderService]

      val rootFolderChildren = await(folderService.retrieveUserFolderTree(TestGlobal.testUser))
      rootFolderChildren(0)
    }
    */
  }


}
