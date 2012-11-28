package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import models._
import play.api.Play.current
import java.io._

object PdfEditor extends Controller {

  def getPdf(formId: String) = Action {request=>
    val textBlocksJson = request.body.asJson.get \ "textblocks"
    val textBlocks = textBlocksJson.as[List[JsValue]].map {tb=>
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
    val pdfPath = new File(new File(Play.configuration.getString("original_pdfs_path").get), formId + ".pdf")
    val result = models.PdfEditor.writeTextBlocks(pdfPath.getAbsolutePath, textBlocks)
    val outputPath = new File(new File(Play.configuration.getString("generated_pdfs_path").get), formId + ".pdf")
    val out = new FileOutputStream(outputPath)
    out.write(result)
    out.close
    val pdfUrl = "/generated_pdfs/" + formId + ".pdf"
    Ok(Json.toJson(Map("pdf_url" -> pdfUrl)))
  }   

}