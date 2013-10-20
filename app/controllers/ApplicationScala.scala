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
import play.libs.Akka
import akka.actor.Props
import scala.concurrent.Await
import akka.util.Timeout
import play.api.data._
import play.api.data.Forms._
import actors._

case class UserData(name : String, hero : String)


object ApplicationScala extends Controller {

    val userForm = Form(
        mapping (
            "name" -> text,
            "hero" -> text
        ) (UserData.apply) (UserData.unapply)
    
    )
    
     
    def index = Action { request => 
    	//TODO: Random user?
      //    val randomData = Map("name" -> "", "hero"->"")
  //    val userData = userForm.bind(anyData)
      Ok(views.html.index(userForm));
    }
    
   
    def login = Action { implicit request => 
   
        val userData = userForm.bindFromRequest.get;
        val uuid = java.util.UUID.randomUUID().toString();
        
        Ok(views.html.game()).withSession(
        		"name" -> userData.name, 
        		"hero" -> userData.hero,
        		"uuid" -> uuid);
    }


    def ws = WebSocket.using[String] { request => {
  
       	val userName =  request.session.get("name").getOrElse("")
       	val uuid =  request.session.get("uuid").getOrElse("")
        
        val out = Enumerator.imperative[String]()
        val userActor = Akka.system.actorOf(Props(new UserActor(out, uuid, userName)), uuid)
        
        userActor ! Subscribe()
        
        val in = Iteratee.foreach[String] {

       	  jsonMessage => {
       		  	val parsedMessage = Json.parse(jsonMessage)
       	    	val  userMove = new SendUserMove(uuid, userName, (parsedMessage \ "x").as[Int],(parsedMessage \ "y").as[Int])
                userActor ! userMove
       	  	}
        }
  
        (in, out)
        
        }
    }
        
    def getScene = Action {
    	
//        implicit val timeout = Timeout(5000)
//        
//        val userActor = Akka.system.actorSelection("Ev")
//
//        //TODO: Scene name? 
//        val future = userActor ? LoadScene("default")
//        
//        //TODO: Unlikely but timeout Error sent back?
//        val result = Await.result(future, timeout.duration).asInstanceOf[String]
      
    	
    
        Ok(WorldActor.getDefaultScene)  
    }       
        

}
