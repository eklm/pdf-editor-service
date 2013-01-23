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
import org.apache.commons.codec.binary.Base64

object PdfEditor {

    def addBlocks(pdfPath: String, textBlocks: List[TextBlock], imageBlocks: List[ImageBlock]) = {
        val reader = new PdfReader(pdfPath)
        val output = new ByteArrayOutputStream()
        val stamper = new PdfStamper(reader, output)

        textBlocks.foreach {block =>
            val cb = stamper.getOverContent(block.page)
            var lineNumber = 0
            block.text.lines.foreach {line => 
                cb.beginText()
                val bf = BaseFont.createFont(Play.configuration.getString("fonts_path").get + "/" + block.font + ".ttf", BaseFont.IDENTITY_H, true)
                val rec = reader.getCropBox(block.page)
                val width = rec.getWidth()
                val k = Play.configuration.getInt("page_width").get / width

                val color = new BaseColor(
                    Integer.parseInt(block.color.substring(0, 2), 16), 
                    Integer.parseInt(block.color.substring(2, 4), 16), 
                    Integer.parseInt(block.color.substring(4, 6), 16), 
                    (if (block.color.size > 6) {Integer.parseInt(block.color.substring(6, 8), 16)} else {255})
                )

                cb.setFontAndSize(bf, round(block.fontSize / k))
                cb.setColorFill(color)
                val xx = round(block.x / k)
                val yy = round(rec.getHeight() - block.y / k - block.fontSize / k)
                cb.setTextMatrix(xx, yy - round(1.0*(block.fontSize+4)*lineNumber/k))
                lineNumber += 1
                cb.showText(line)
                cb.endText()
            }
        }

        imageBlocks.foreach {block =>
            val cb = stamper.getOverContent(block.page)
            val imageBytes = Base64.decodeBase64(block.data)
            val pdfImage = com.itextpdf.text.Image.getInstance(java.awt.Toolkit.getDefaultToolkit.createImage(imageBytes), null)

            val rec = reader.getCropBox(block.page)
            val width = rec.getWidth()
            val scale = Play.configuration.getInt("page_width").get / width

            val xx = round(block.x / scale)
            val yy = round(rec.getHeight() - block.y / scale - block.height / scale)

            cb.addImage(pdfImage, block.width / scale, 0, 0, block.height/scale, xx, yy)
        }

        stamper.close()
        output.toByteArray
    }

}