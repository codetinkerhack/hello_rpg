package actors

import akka.actor.{ Props, ActorRef, Actor }
import java.util.Random
import scala.collection.immutable.{ HashSet, Queue }
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.libs.Akka
import play.libs.Json
import play.mvc.WebSocket
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.node.{ ArrayNode, ObjectNode }
import org.slf4j.LoggerFactory
import play.api.libs.iteratee.PushEnumerator
import play.api.libs.json.JsValue




case class LoadScene(sceneActorName: String, scene: List[List[Int]])

case class Subscribe()

case class UnSubscribe()

case class UnSubscribeScene(scene: String)

case class UserMove(id: String, name: String, x: Int, y: Int)

case class UserMoveScene(scene: String, id: String, name: String, x: Int, y: Int)

case class SendUserMove(id: String, name: String, x: Int, y: Int)

case class UserMessage(message: JsValue)

case class LoadWorld()

case class MoveFromSceneToScene(id: String, name: String, fromSceneX: Int, fromSceneY: Int, toSceneX: Int, toSceneY: Int)

case class ShutDown()





/**
 * SceneActor(s) comprise the World.
 * The SceneActor maintains a list of users subscribed to a Scene events.
 */

class SceneActor(sceneActorName: String, scene: List[List[Int]]) extends Actor {

  protected[this] var users: HashSet[ActorRef] = HashSet.empty[ActorRef]

  def receive = {
    case UserMove(id, name, x, y) =>
      //val newPrice = stockQuote.newPrice(stockHistory.last.doubleValue())
      //stockHistory = stockHistory.drop(1) :+ newPrice
      // notify watchers
      WorldActor.logger.info("SceneActor.UserMove: id: "+ id +" name: " + name + " x: " + x + " y: " + y)
      users.foreach(_ ! UserMove(id, name, x, y))
    case Subscribe() =>
      // send the scene to the user
      sender ! LoadScene(sceneActorName, scene)
      // add the watcher to the list
      users = users + sender
    case UnSubscribe() =>
      users = users - sender

  }
}



class WorldActor extends Actor {

  val defaultSceneActor = context.actorOf(Props(new SceneActor("defaultSceneActor", WorldActor.sceneDefaultTiles)), "defaultSceneActor")
  val sceneActor10 = context.actorOf(Props(new SceneActor("scene10", WorldActor.scene10)), "scene10")
  var scenes: Map[(Int, Int), ActorRef] = Map((0, 0) -> defaultSceneActor, (1, 0) -> sceneActor10)

  def receive = {
    case LoadWorld() => {

    }

    case UserMoveScene(scene, id, name, x, y) => {
      context.child(scene).getOrElse {
        defaultSceneActor
      } forward UserMove(id, name, x, y)
    }

    case UnSubscribeScene(scene) => {
      context.child(scene).getOrElse {
        defaultSceneActor
      } forward UnSubscribe()
    }

  
    case MoveFromSceneToScene(id, user, fx, fy, tx, ty) => {

      // TODO: lookup Scene to Scene mapping within World
      WorldActor.logger.info("MoveFromSceneToScene: User: " + user + " scene" + tx + ty)
      // get or pass default scene which is
      context.child("scene" + tx + ty).getOrElse {
        defaultSceneActor
      } forward Subscribe()
    }
  }
}

object WorldActor {
  val logger = LoggerFactory.getLogger("actors.WorldActor");
  lazy val worldActor: ActorRef = Akka.system.actorOf(Props(classOf[WorldActor]))

  //for(line <- Source.fromPath("myfile.txt").getLines())

  val sceneDefaultTiles: List[List[Int]] = List(
    List(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 1, 2, 2, 2, 2, 1, 1, 3, 3, 3, 3, 1, 1, 1, 1),
    List(1, 1, 1, 2, 2, 1, 1, 3, 2, 3, 2, 3, 3, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 3, 2, 2, 2, 2, 3, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 1, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1),
    List(1, 1, 1, 1, 1, 2, 1, 2, 2, 3, 4, 3, 2, 1, 2, 1),
    List(1, 1, 1, 1, 1, 2, 1, 2, 2, 3, 4, 3, 2, 1, 2, 1),
    List(1, 1, 1, 1, 1, 2, 1, 2, 2, 3, 3, 3, 2, 1, 2, 1),
    List(1, 1, 1, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 1, 2, 1),
    List(1, 1, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2),
    List(1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 1),
    List(1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 1))
    
    val scene10: List[List[Int]] = List(
    List(1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1),
    List(1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
    List(1, 1, 2, 2, 2, 2, 1, 1, 3, 3, 3, 3, 1, 1, 1, 2),
    List(2, 1, 1, 2, 2, 1, 1, 3, 2, 3, 2, 3, 3, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 3, 2, 2, 2, 2, 3, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 1, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 1, 2),
    List(2, 1, 1, 1, 1, 2, 1, 2, 2, 3, 4, 3, 2, 1, 2, 2),
    List(2, 1, 1, 1, 1, 2, 1, 2, 2, 3, 4, 3, 2, 1, 2, 2),
    List(2, 1, 1, 1, 1, 2, 1, 2, 2, 3, 3, 3, 2, 1, 2, 2),
    List(2, 1, 1, 1, 1, 2, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2),
    List(2, 1, 1, 1, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2),
    List(2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 2),
    List(2, 1, 1, 1, 1, 1, 1, 2, 2, 1, 1, 2, 2, 1, 1, 2))

  def getDefaultScene = {
    val sceneLoadMessage: ObjectNode = Json.newObject();
    sceneLoadMessage.put("type", "loadScene");

    val sceneJson = sceneLoadMessage.putArray("scene");

    for (row <- sceneDefaultTiles) {

      val sceneRowJson = sceneJson.addArray()

      for (tile <- row) {
        sceneRowJson.add(tile);
      }
    }
    WorldActor.logger.info(sceneLoadMessage.toString());

    sceneLoadMessage.toString()
  }

}

class UserActor(out: PushEnumerator[String], id: String, name: String) extends Actor {

  var currentSceneActorName:String=_
    
  def receive = {

    // Selector
    case UserMessage(message) => {
    	WorldActor.logger.info("UserMessage: "+message.toString())
    	if((message \ "type").as[String] == "userMove") { self ! SendUserMove(id, name, (message \ "x").as[Int], (message \ "y").as[Int]) }
    	if((message \ "type").as[String] == "subscribe") { self ! Subscribe() }
    }
    
    case SendUserMove(id, name, x, y) => {
      if(y < 255 && y > 0) {
    	  	WorldActor.logger.info("SendUserMove: sceneActor: " + currentSceneActorName + " id: " +id +" name: " + name + " x: " + x + " y: " + y)
    	  
    	  	
    	  WorldActor.worldActor ! UserMoveScene(currentSceneActorName, id, name, x, y)
      } else if (y > 255) {
      
    	  WorldActor.worldActor ! UnSubscribeScene(currentSceneActorName)
    	  
          WorldActor.worldActor ! MoveFromSceneToScene(id, name, 0, 0, 1, 0)
      } else if (y < 0) {
      
    	  WorldActor.worldActor ! UnSubscribeScene(currentSceneActorName)
    	  
          WorldActor.worldActor ! MoveFromSceneToScene(id, name, 0, 0, 0, 0)
      }
    }

    case UserMove(id, name, x, y) => {
      if(this.id != id) {
    	val userMoveMessage: ObjectNode = Json.newObject();
      	userMoveMessage.put("type", "userMove");
      	userMoveMessage.put("id", id);
      	userMoveMessage.put("name", name);
      	userMoveMessage.put("x", x);
      	userMoveMessage.put("y", y);
      	out.push(userMoveMessage.toString());
      }
    }

    case Subscribe() => {
      WorldActor.worldActor ! MoveFromSceneToScene(id, name, 0, 0, 0, 0)
    }

    case LoadScene(sceneActorName, scene) =>
      {
    	this.currentSceneActorName = sceneActorName
    	
        val sceneLoadMessage: ObjectNode = Json.newObject();
        sceneLoadMessage.put("type", "loadScene");

        val sceneJson = sceneLoadMessage.putArray("scene");

        for (row <- scene) {

          val sceneRowJson = sceneJson.addArray()

          for (tile <- row) {
            sceneRowJson.add(tile);
          }
        }
        //WorldActor.logger.info(sceneLoadMessage.toString());
        out.push(sceneLoadMessage.toString());
      }
  }
}

