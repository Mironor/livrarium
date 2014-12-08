package modules

import helpers.{UUIDGenerator, RandomIdGenerator}
import models.FolderDAO
import scaldi.Module
import services.FolderService

/**
 * Application's dependency injections
 */
class WebModule extends Module{
  bind[FolderDAO] to new FolderDAO
  bind[FolderService] to new FolderService

  bind[RandomIdGenerator] to new UUIDGenerator

  //  bind[BookService] to new BookService
  //  bind[BookDAO] to new BookDAO
}
