package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import models._
import play.api.Play.current
import java.io._

object PdfEditor extends Controller {

  def getPdf(formId: String) = Action {request=>
    val content = request.body.asJson.get

    val textBlocks = (content \ "textblocks").as[List[JsValue]].map {tb=>
      new TextBlock(
        (tb \ "text").as[String],
        (tb \ "font").as[String],
        (tb \ "fontSize").as[Int],
        (tb \ "color").as[String],
        (tb \ "page").as[Int],
        (tb \ "x").as[Int],
        (tb \ "y").as[Int]
      )
    }

    val imageBlocks = (content \ "imageblocks").as[List[JsValue]].map {ib=>
      new ImageBlock(
        (ib \ "data").as[String],
        (ib \ "width").as[Int],
        (ib \ "height").as[Int],
        (ib \ "page").as[Int],
        (ib \ "x").as[Int],
        (ib \ "y").as[Int]
      )
    }

    val pdfPath = new File(new File(Play.configuration.getString("original_pdfs_path").get), formId + ".pdf")
    val result = models.PdfEditor.addBlocks(pdfPath.getAbsolutePath, textBlocks, imageBlocks)
    val outputPath = new File(new File(Play.configuration.getString("generated_pdfs_path").get), formId + ".pdf")
    val out = new FileOutputStream(outputPath)
    out.write(result)
    out.close
    val pdfUrl = "/generated_pdfs/" + formId + ".pdf"
    Ok(Json.toJson(Map("pdf_url" -> pdfUrl)))
  }   

}