package utils

import scala.util.parsing.json.JSON
import actors. Scene
import scala.io._
import java.io.File
import scala.collection.JavaConversions._



object Utils {

  def loadWorld(appAbsolutePath: String): Map[String, Scene] = {
   
    val sceneFilePath = appAbsolutePath + "/assets/javascripts/scenes/"
    
    (for(sceneFileName <- getFileTree(new File(sceneFilePath)).filter(_.getName.endsWith(".json")) ) 
      yield (sceneFileName.getName, loadScene(sceneFilePath+sceneFileName.getName))).toMap
 
  }
  
  def getFileTree(f: File): List[File] =
        f :: (if (f.isDirectory) f.listFiles().toList.flatMap(getFileTree) 
               else List.empty)

               
               
  def loadScene(sceneFile: String): Scene = {

    val sceneJson = Source.fromFile(sceneFile).getLines.mkString
    
    val sceneJsonParsed = JSON.parseFull(sceneJson).get
    
    val sceneTiles = sceneJsonParsed.asInstanceOf[Map[String,List[List[Double]]]].get("scene").get
    
    val sceneTransitions = sceneJsonParsed.asInstanceOf[Map[String,List[String]]].get("transitions").get

    Scene(sceneFile, sceneTiles.map( _.map (_.toInt)), sceneTransitions)
    
    
  }

}