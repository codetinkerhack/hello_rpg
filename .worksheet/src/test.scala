import utils.Utils
import scala.util.parsing.json.JSON


object test {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(168); 
    
    
    
    val appAbsolutePath = "C:/Users/evgeniyshatokhin/Desktop/GitHub/hello_rpg/app";System.out.println("""appAbsolutePath  : String = """ + $show(appAbsolutePath ));$skip(46); 
    
        val sceneFileName = "scene.json";System.out.println("""sceneFileName  : String = """ + $show(sceneFileName ));$skip(89); 
    val sceneFilePath = appAbsolutePath + "/assets/javascripts/scenes/" + sceneFileName;System.out.println("""sceneFilePath  : String = """ + $show(sceneFilePath ));$skip(78); ;
    val sceneJson = scala.io.Source.fromFile(sceneFilePath).getLines.mkString;System.out.println("""sceneJson  : String = """ + $show(sceneJson ));$skip(56); 
     
       val scene1 = JSON.parseFull(sceneJson).get;System.out.println("""scene1  : Any = """ + $show(scene1 ));$skip(121); 
       
    
    val scene = JSON.parseFull(sceneJson).get.asInstanceOf[Map[String,List[List[Double]]]].get("scene").get;System.out.println("""scene  : List[List[Double]] = """ + $show(scene ));$skip(151); 
                                         
    val scene2 = JSON.parseFull(sceneJson).get.asInstanceOf[Map[String,List[String]]].get("transitions").get;System.out.println("""scene2  : List[String] = """ + $show(scene2 ));$skip(28); 
    
    val b:Double = 1.0;System.out.println("""b  : Double = """ + $show(b ));$skip(20); 
    val c = b.toInt;System.out.println("""c  : Int = """ + $show(c ));$skip(39); 
    
    val a = scene.head.head.toInt;System.out.println("""a  : Int = """ + $show(a ));$skip(54); val res$0 = 
    
      
    
    Utils.loadWorld(appAbsolutePath);System.out.println("""res0: Map[String,actors.Scene] = """ + $show(res$0))}
  
}
