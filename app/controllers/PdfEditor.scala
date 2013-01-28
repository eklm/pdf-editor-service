package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import models._
import play.api.Play.current
import java.io._

object PdfEditor extends Controller {

  val dataUrlRegexp = "data:(.*),(.*)".r
  
  def getPdf(formId: String) = Action {request=>
    val content = request.body.asJson.get

    val blocks = (content \ "blocks").as[List[JsValue]].map {blockData=>
      val blockType = (blockData \ "type").as[String]

      (if (blockType == "textblock") {
        new TextBlock(
          (blockData \ "text").as[String],
          (blockData \ "font").as[String],
          (blockData \ "fontSize").as[Int],
          (blockData \ "color").as[String],
          (blockData \ "page").as[Int],
          (blockData \ "x").as[Int],
          (blockData \ "y").as[Int]
        )
      } else if(blockType == "imageblock") {
        val imageDataUrl = (blockData \ "imageDataUrl").as[String]
        val dataUrlRegexp(format, imageData) = imageDataUrl
        new ImageBlock(
          imageData,
          (blockData \ "width").as[Int],
          (blockData \ "height").as[Int],
          (blockData \ "page").as[Int],
          (blockData \ "x").as[Int],
          (blockData \ "y").as[Int]
        )
      }).asInstanceOf[Block]

    }


    val pdfPath = new File(new File(Play.configuration.getString("original_pdfs_path").get), formId + ".pdf")
    val result = models.PdfEditor.addBlocks(pdfPath.getAbsolutePath, blocks)
    val outputPath = new File(new File(Play.configuration.getString("generated_pdfs_path").get), formId + ".pdf")
    val out = new FileOutputStream(outputPath)
    out.write(result)
    out.close
    val pdfUrl = "/generated_pdfs/" + formId + ".pdf"
    Ok(Json.toJson(Map("pdf_url" -> pdfUrl)))
  }   

}