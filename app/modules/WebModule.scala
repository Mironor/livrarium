package modules

import daos.{BookDAO, FolderDAO}
import helpers.{RandomIdGenerator, UUIDGenerator}
import scaldi.Module
import services.{BookService, FolderService}

/**
 * Application's dependency injections
 */
class WebModule extends Module {
  bind[RandomIdGenerator] to new UUIDGenerator

  bind[FolderDAO] to new FolderDAO
  bind[FolderService] to new FolderService

  bind[BookService] to new BookService
  bind[BookDAO] to new BookDAO
}
