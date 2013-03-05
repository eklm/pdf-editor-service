package models;

import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.PdfContentByte
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.BaseColor
import java.io._
import play.api.Play.current
import play.api._
import scala.math._
import java.net.URL

object PdfEditor {

  def addBlocks(pdfUrl: String, blocks: List[Block], referenceWidth: Int, print: Boolean) = {
    val reader = new PdfReader(new URL(pdfUrl))
    val output = new ByteArrayOutputStream()
    val stamper = new PdfStamper(reader, output)

    blocks.foreach {block =>
      val cb = stamper.getOverContent(block.page)
      val rec = reader.getCropBox(block.page)
      val width = rec.getWidth()
      val scale = referenceWidth * 1./ width

      block match {
        case textBlock: TextBlock => {
          var lineNumber = 0
          textBlock.text.lines.foreach {line => 
              cb.beginText()
              val bf = BaseFont.createFont(Play.configuration.getString("fonts_path").get + "/" + textBlock.font + ".ttf", BaseFont.IDENTITY_H, true)

              val color = new BaseColor(
                  Integer.parseInt(textBlock.color.substring(0, 2), 16), 
                  Integer.parseInt(textBlock.color.substring(2, 4), 16), 
                  Integer.parseInt(textBlock.color.substring(4, 6), 16), 
                  (if (textBlock.color.size > 6) {Integer.parseInt(textBlock.color.substring(6, 8), 16)} else {255})
              )

              cb.setFontAndSize(bf, round(textBlock.fontSize / scale))
              cb.setColorFill(color)
              val x = round(textBlock.x / scale)
              val y = round(rec.getHeight() - textBlock.y / scale - (textBlock.fontSize) / scale - (textBlock.fontSize+4)*lineNumber/scale)
              cb.setTextMatrix(x, y )
              lineNumber += 1
              cb.showText(line)
              cb.endText()
          }
        }

        case imageBlock: ImageBlock => {
          val pdfImage = com.itextpdf.text.Image.getInstance(new URL(imageBlock.url))

          val x = round(imageBlock.x / scale)
          val y = round(rec.getHeight() - imageBlock.y / scale - imageBlock.height / scale)

          cb.addImage(pdfImage, round(imageBlock.width /scale), 0, 0, round(imageBlock.height/scale), x, y)
        }
      }
    }

    if (print) {
      stamper.addJavaScript("this.print();");
    }

    stamper.close()
    output.toByteArray
  }

}