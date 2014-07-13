package modules

import controllers.{Cloud, Application}
import scaldi.Module

/**
 * User: mironor
 * Date: 7/4/14
 */
class WebModule extends Module{
  binding to new Application
  binding to new Cloud
}
