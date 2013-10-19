package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import play.api.libs.ws.WS
import scala.concurrent.Future
import play.api.libs.json.{Json, JsValue}
import play.api.Play
import play.api.libs.ws.Response
import play.api.libs.json.JsString
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import akka.pattern.ask
import akka.actor.Actor
import actors.UserActor
import play.libs.Akka
import akka.actor.Props
import actors.LoadScene
import scala.concurrent.Await
import akka.util.Timeout
import actors.UserMove

object ApplicationScala extends Controller {

    //val logger = LoggerFactory.getLogger(controllers.Application.class);
     
    def index = Action { request => 
         
       Ok(views.html.index());
    }
    
    def login = Action { implicit request => 
   
//        Form<User> user = userForm.bindFromRequest();
//        logger.info(user.name());
   //     session().put("userName", "Ev");
        
        Ok(views.html.game());
    }


    def ws = WebSocket.using[String] { request => {
  
       	val name = "Ev"//session().get("playerName")
        
        
        
        //Akka.system().actorOf(Props.create(UserActor.class, out, name));
        
        // Log events to the console
        
        val out = Enumerator.imperative[String]()
        val userActor = Akka.system.actorOf(Props(new UserActor(out, name)), name)
        
        val in = Iteratee.foreach[String] {

       	  jsonMessage => {
       		  	val parsedMessage = Json.parse(jsonMessage)
       	    	val  userMove = new UserMove((parsedMessage \ "name").as[String], (parsedMessage \ "x").as[Int],(parsedMessage \ "y").as[Int])
                userActor ! userMove
       	  	}
        }
  
        (in, out)
        
        }
    }
        
    def getScene = Action {
    	
        implicit val timeout = Timeout(5000)
        
        val userActor = Akka.system.actorSelection("Ev");

        //TODO: Scene name? 
        val future = userActor ? LoadScene("default")
        
        //TODO: Unlikely but timeout Error sent back?
        val result = Await.result(future, timeout.duration).asInstanceOf[String]
    
        Ok(result)  
    }       
        

}
