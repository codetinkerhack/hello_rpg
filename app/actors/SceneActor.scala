package actors

import akka.actor.{Props, ActorRef, Actor}
import java.util.Random
import scala.collection.immutable.{HashSet, Queue}
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.libs.Akka
import play.libs.Json
import play.mvc.WebSocket
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.node.{ArrayNode,ObjectNode} 


/**
 * SceneActor(s) comprise the World.  
 * The SceneActor maintains a list of users subscribed to a Scene events.
 */

class SceneActor(scene: List[List[Int]]) extends Actor {

  protected[this] var users: HashSet[ActorRef] = HashSet.empty[ActorRef]

  def receive = {
    case UserMove(name, x , y) =>
      //val newPrice = stockQuote.newPrice(stockHistory.last.doubleValue())
      //stockHistory = stockHistory.drop(1) :+ newPrice
      // notify watchers
      println("name: "+name+"x: "+x+"y: "+y)
      users.foreach(_ ! UserMove(name, x, y))
    case Subscribe() =>
      // send the scene to the user
      sender ! Scene(scene)
      // add the watcher to the list
      users = users + sender
    case UnSubscribe() =>
      users = users - sender
    
  }
}

case class Scene(scene: List[List[Int]])

case class Subscribe()

case class UnSubscribe()

case class UserMove(name: String, x: Int, y: Int)

case class LoadWorld()

case class LoadScene(sceneName: String)

case class MoveFromSceneToScene(user: String, fromSceneX:Int, fromSceneY:Int, toSceneX:Int, toSceneY:Int)

case class ShutDown()

class WorldActor extends Actor {
  
  val sceneDefaultTiles : List[List[Int]] = List(
List(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
List(1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
List(1,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
List(1,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
List(1,2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1),
List(1,1,2,2,2,2,1,1,3,3,3,3,1,1,1,1,1,1,1,1),
List(1,1,1,2,2,1,1,3,2,3,2,3,3,1,1,1,1,1,1,1),
List(1,1,1,1,1,1,1,3,2,2,2,2,3,1,1,1,1,1,1,1),
List(1,1,1,1,1,1,1,1,3,3,3,3,1,1,1,1,1,1,1,1),
List(1,1,1,1,1,1,1,1,1,2,2,1,1,1,1,1,1,1,1,1),
List(1,1,1,1,1,1,2,2,2,2,2,2,2,2,1,1,1,1,1,1),
List(1,1,1,1,1,2,1,2,2,3,4,3,2,1,2,1,1,1,1,1),
List(1,1,1,1,1,2,1,2,2,3,4,3,2,1,2,1,1,1,1,1),
List(1,1,1,1,1,2,1,2,2,3,3,3,2,1,2,1,1,1,1,1),
List(1,1,1,1,1,2,1,2,2,2,2,2,2,1,2,1,1,1,1,1),
List(1,1,1,1,2,2,1,2,2,2,2,2,2,1,2,2,1,1,1,1),
List(1,1,1,1,1,1,1,1,2,1,1,2,1,1,1,1,1,1,1,1),
List(1,1,1,1,1,1,1,1,2,1,1,2,1,1,1,1,1,1,1,1),
List(1,1,1,1,1,1,1,1,2,1,1,2,1,1,1,1,1,1,1,1),
List(1,1,1,1,1,1,1,2,2,1,1,2,2,1,1,1,1,1,1,1)
)
  
  val defaultSceneActor = context.actorOf(Props(new SceneActor(sceneDefaultTiles)), "sceneDefault")
  
  var scenes : Map[(Int,Int),ActorRef] = Map((0,0) -> defaultSceneActor)
  
   def receive = {
     case LoadWorld() => {
    	 
     }
     
     case LoadScene(scene:String) => {
       println(scene)
       // Load and create SceneActors
     }
     
     case MoveFromSceneToScene(user:String, fx:Int, fy:Int, tx:Int, ty:Int) => {

       // TODO: lookup Scene to Scene mapping within World
       
       // get or pass default scene which is
       context.child("scene"+tx+ty).getOrElse {
         defaultSceneActor
       } forward Subscribe()
     }
  }
 }
 
 object WorldActor {
   lazy val worldActor: ActorRef = Akka.system.actorOf(Props(classOf[WorldActor]))
 }
 
 class UserActor(out: WebSocket.Out[JsonNode], name: String) extends Actor {

     WorldActor.worldActor ! MoveFromSceneToScene(name, 0, 0, 0, 0)
        

	def receive = {
	  case UserMove(name, x,y) => {
			val userMoveMessage: ObjectNode = Json.newObject();
			userMoveMessage.put("type", "userMove");
			userMoveMessage.put("name", name);
			userMoveMessage.put("x", x);
			userMoveMessage.put("y", y);
			out.write(userMoveMessage);
		} 
	  
	  
	  case Scene(scene) =>
	    {			// push the history to the client
			

			val sceneLoadMessage: ObjectNode = Json.newObject();
			sceneLoadMessage.put("type", "loadScene");

			for (row <- scene) {
				val sceneRowJson: ArrayNode  = sceneLoadMessage.putArray("sceneRow");
				for (tile <- row) {
					sceneRowJson.add(tile);
				}
			}
			
			out.write(sceneLoadMessage);
		}
	}
}

