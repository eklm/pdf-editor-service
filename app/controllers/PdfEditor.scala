package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import models._
import play.api.Play.current
import java.io._

object PdfEditor extends Controller {
  
  def generatePdf = Action {request=>
    val content = request.body.asJson.get

    val pdfUrl = (content \ "pdfUrl").as[String]
    val referenceWidth = (content \ "referenceWidth").as[Int]
    val print = (content \ "print").asOpt[Boolean].getOrElse(false)

    val blocks = (content \ "blocks").as[List[JsValue]].map {blockData=>
      val blockType = (blockData \ "type").as[String]

      (if (blockType == "text") {
        new TextBlock(
          (blockData \ "text").as[String],
          (blockData \ "font").as[String],
          (blockData \ "fontSize").as[Int],
          (blockData \ "color").as[String],
          (blockData \ "page").as[Int],
          (blockData \ "x").as[Int],
          (blockData \ "y").as[Int]
        )
      } else if(blockType == "image") {
        new ImageBlock(
          (blockData \ "url").as[String],
          (blockData \ "width").as[Int],
          (blockData \ "height").as[Int],
          (blockData \ "page").as[Int],
          (blockData \ "x").as[Int],
          (blockData \ "y").as[Int]
        )
      }).asInstanceOf[Block]
    }

    val result = models.PdfEditor.addBlocks(pdfUrl, blocks, referenceWidth, print)
    val filename = java.security.MessageDigest.getInstance("SHA-1").digest(result).map("%02x" format _).mkString
    val outputPath = new File(new File(Play.configuration.getString("generated_pdfs_path").get), filename + ".pdf")
    val out = new FileOutputStream(outputPath)
    out.write(result)
    out.close
    val newPdfUrl = "/pdfeditor/generated_pdfs/" + filename + ".pdf"
    Ok(Json.toJson(Map("pdf_url" -> newPdfUrl)))
  }

  def getPdf(filename: String) = Action{request=>
    Ok.sendFile(new java.io.File(Play.configuration.getString("generated_pdfs_path").get, filename), inline=true)
  }

}