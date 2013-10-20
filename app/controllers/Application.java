package controllers;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import play.data.Form;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import scala.Option;
import actors.ShutDown;
import actors.UserActor;
import actors.UserMove;
import actors.WorldActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import models.*;
import play.api.data.*;

/**
 * The main web controller that handles returning the index page, setting up a WebSocket, and watching a stock.
 */
public class Application extends Controller {

	 static Logger logger = LoggerFactory.getLogger(controllers.Application.class);
//	 static Form<User> userForm = form(User.class);
    //  
    // public static Result index() {
    //      
    //     return ok(views.html.index.render());
    // }
    
//     public static Result login() {
//    
// //        Form<User> user = userForm.bindFromRequest();
//         
// //        logger.info(user.name());
//         session().put("userName", "Ev");
//         
//         return ok(views.html.game());
//     }

    public static WebSocket<JsonNode> ws() {
        
        
    	final String name = session().get("playerName");    
        
        return new WebSocket<JsonNode>() {
            

        	public void onReady(final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {

                // create a new UserActor and give it the default stocks to watch
                final ActorRef userActor = Akka.system().actorOf(Props.create(UserActor.class, out, name));
            
                // send all WebSocket message to the UserActor
                in.onMessage(new F.Callback<JsonNode>() {
                    @Override
                    public void invoke(JsonNode jsonNode) throws Throwable {
                        // parse the JSON into WatchStock
                        UserMove userMove = new UserMove("", jsonNode.get("name").getTextValue(),jsonNode.get("x").getIntValue(),jsonNode.get("y").getIntValue());
                        // send the watchStock message to the StocksActor
                        logger.info("Received: "+jsonNode.toString());
                        userActor.tell(userMove, userActor);
                        // SceneActor.sceneActor().tell(watchStock, userActor);
                    }
                });

                // on close, tell the userActor to shutdown
                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        final Option<String> none = Option.empty();
                        userActor.tell(new ShutDown(), userActor);
                        Akka.system().stop(userActor);
                    }
                });
            }
        };
    }

}
