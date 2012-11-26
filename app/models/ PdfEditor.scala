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

object PdfEditor {

    def writeTextBlocks(pdfPath: String, textBlocks: List[TextBlock]) = {
        val reader = new PdfReader(pdfPath)
        val output = new ByteArrayOutputStream()
        val stamper = new PdfStamper(reader, output)
        textBlocks.foreach {block =>
            val cb = stamper.getOverContent(block.page)
            cb.beginText()
            val bf = BaseFont.createFont(Play.configuration.getString("fonts_path").get + "/" + block.font + ".ttf", BaseFont.IDENTITY_H, true)
            val rec = reader.getCropBox(block.page)
            val width = rec.getWidth()
            val k = Play.configuration.getInt("page_width").get / width

            val color = new BaseColor(
                Integer.parseInt(block.color.substring(0, 2), 16), 
                Integer.parseInt(block.color.substring(2, 4), 16), 
                Integer.parseInt(block.color.substring(4, 6), 16), 
                Integer.parseInt(block.color.substring(6, 8), 16))

            cb.setFontAndSize(bf, round(block.fontSize / k))
            cb.setColorFill(color)
            val xx = round(block.x / k)
            val yy = round(rec.getHeight() - block.y / k - block.fontSize / k)
            cb.setTextMatrix(xx, yy)
            cb.showText(block.text)
            cb.endText()
        }
        stamper.close()
        output.toByteArray
    }

}