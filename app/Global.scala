package appglobal

import play.api._
import play.api.Play.current
import actors._

object Global extends GlobalSettings {
    var applicationPath= ""
    
  override def onStart(app: Application) {
    Logger.info("Application has started")
    applicationPath = Play.application.configuration.getString("application.path").get
    Logger.info("Application path: "+ applicationPath)
 
    
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}