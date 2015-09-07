package modules

import daos.{BookDAO, FolderDAO}
import helpers.{FileHelper, RandomIdGenerator, UUIDGenerator}
import scaldi.Module
import services.{BookService, FolderService}

/**
 * Application's dependency injections
 */
class WebModule extends Module {
  bind[RandomIdGenerator] to new UUIDGenerator
  bind[FileHelper] to new FileHelper

  bind[FolderDAO] to new FolderDAO
  bind[FolderService] to new FolderService

  bind[BookService] to new BookService
  bind[BookDAO] to new BookDAO
}
