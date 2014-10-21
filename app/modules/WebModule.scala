package modules

import controllers.{Cloud, Application}
import models.FolderDAO
import scaldi.Module

/**
 * User: mironor
 * Date: 7/4/14
 */
class WebModule extends Module{
  bind[FolderDAO] to new FolderDAO
  //  bind[BookService] to new BookService
  //  bind[BookDAO] to new BookDAO
}
