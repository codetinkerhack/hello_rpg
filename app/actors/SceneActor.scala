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
import utils.Utils
import play.api.Play
import appglobal._

case class Scene(name: String, sceneTiles: List[List[Int]], sceneTransitions: List[String])

//class World(scenes: List[Scene])

case class LoadScene(sceneActorName: String, scene: Scene)

case class Subscribe()

case class SubscribeScene(scene: String)

case class UnSubscribe()

case class UnSubscribeScene(scene: String)

case class UserMove(id: String, name: String, x: Int, y: Int)

case class UserMoveScene(scene: String, id: String, name: String, x: Int, y: Int)

case class SendUserMove(id: String, name: String, x: Int, y: Int)

case class UserMessage(message: JsValue)

case class LoadWorld()

case class MoveFromSceneToScene(id: String, name: String, fromScene: String, transitionDirection: Int)

case class ShutDown()

/**
 * SceneActor(s) comprise the World.
 * The SceneActor maintains a list of users subscribed to a Scene events.
 */

class SceneActor(sceneActorName: String, scene: Scene) extends Actor {

  protected[this] var users: HashSet[ActorRef] = HashSet.empty[ActorRef]

  def receive = {
    case UserMove(id, name, x, y) =>
      //val newPrice = stockQuote.newPrice(stockHistory.last.doubleValue())
      //stockHistory = stockHistory.drop(1) :+ newPrice
      // notify watchers
      WorldActor.logger.info("SceneActor.UserMove: id: " + id + " name: " + name + " x: " + x + " y: " + y)
      users.foreach(_ ! UserMove(id, name, x, y))
    case Subscribe() =>
      WorldActor.logger.info("SceneActor.Subscribe: " + sceneActorName)
      // send the scene to the user
      sender ! LoadScene(sceneActorName, scene)
      // add the watcher to the list
      users = users + sender
    case UnSubscribe() => {
      WorldActor.logger.info("SceneActor.UnSubscribe: " + sceneActorName)
      users = users - sender
    }
  }
}

class WorldActor extends Actor {
 
  val world: Map[String, Scene] = Utils.loadWorld(WorldActor.applicationPath)

  world.foreach(scene => context.actorOf(Props(new SceneActor(scene._1, scene._2)), scene._1))

  // default scene
  val defaultSceneActor = context.child(WorldActor.defaultScene).get

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

    case SubscribeScene(scene) => {
      context.child(scene).getOrElse {
        defaultSceneActor
      } forward Subscribe()

    }

    case MoveFromSceneToScene(id, user, sf, sceneTo) => {

      WorldActor.logger.info("MoveFromSceneToScene: User: " + user + " scene from:  " + sf + "to: " + sceneTo)

      // get or pass default scene which is
      val scene = world.get(sf).get

      context.child(scene.sceneTransitions(sceneTo)).getOrElse {
        defaultSceneActor
      } forward Subscribe()
    }
  }
}

object WorldActor {
  val  applicationPath = Global.applicationPath
 
    
  val logger = LoggerFactory.getLogger("actors.WorldActor");
  val defaultScene = "scene.json"
  lazy val worldActor: ActorRef = Akka.system.actorOf(Props(classOf[WorldActor]))
  
  logger.info("World Actor started...")

}

class UserActor(out: PushEnumerator[String], id: String, name: String) extends Actor {

  var currentSceneActorName: String = WorldActor.defaultScene

  def receive = {

    // Selector
    case UserMessage(message) => {
      WorldActor.logger.info("UserMessage: " + message.toString())
      if ((message \ "type").as[String] == "userMove") { self ! SendUserMove(id, name, (message \ "x").as[Int], (message \ "y").as[Int]) }
      if ((message \ "type").as[String] == "subscribe") { self ! Subscribe() }
    }

    case SendUserMove(id, name, x, y) => {
      if (y >= 0 && y <= 255 && x >= 0 && x <= 384) {
        WorldActor.logger.info("SendUserMove: sceneActor: " + currentSceneActorName + " id: " + id + " name: " + name + " x: " + x + " y: " + y)

        WorldActor.worldActor ! UserMoveScene(currentSceneActorName, id, name, x, y)
      } else if (y > 255) {

        WorldActor.worldActor ! UnSubscribeScene(currentSceneActorName)
        WorldActor.worldActor ! MoveFromSceneToScene(id, name, currentSceneActorName, 1)
      } else if (y < 0) {
        
        WorldActor.worldActor ! UnSubscribeScene(currentSceneActorName)
        WorldActor.worldActor ! MoveFromSceneToScene(id, name, currentSceneActorName, 0)
      } else if (x > 384) {
        
        WorldActor.worldActor ! UnSubscribeScene(currentSceneActorName)
        WorldActor.worldActor ! MoveFromSceneToScene(id, name, currentSceneActorName, 3)
      } else if (x < 0) {
        
        WorldActor.worldActor ! UnSubscribeScene(currentSceneActorName)
        WorldActor.worldActor ! MoveFromSceneToScene(id, name, currentSceneActorName, 2)
      }
    }

    case UserMove(id, name, x, y) => {
      if (this.id != id) {
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
      WorldActor.worldActor ! SubscribeScene(currentSceneActorName)
    }

    case LoadScene(sceneActorName, scene) =>
      {
        this.currentSceneActorName = sceneActorName

        val sceneLoadMessage: ObjectNode = Json.newObject();
        sceneLoadMessage.put("type", "loadScene");

        val sceneJson = sceneLoadMessage.putArray("scene");

        for (row <- scene.sceneTiles) {

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

