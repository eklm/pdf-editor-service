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

    def addBlocks(pdfPath: String, blocks: List[Block]) = {
        val reader = new PdfReader(pdfPath)
        val output = new ByteArrayOutputStream()
        val stamper = new PdfStamper(reader, output)

        blocks.foreach {block =>
            val cb = stamper.getOverContent(block.page)
            val rec = reader.getCropBox(block.page)
            val width = rec.getWidth()
            val k = Play.configuration.getInt("page_width").get / width

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

                        cb.setFontAndSize(bf, round(textBlock.fontSize / k))
                        cb.setColorFill(color)
                        val xx = round(textBlock.x / k)
                        val yy = round(rec.getHeight() - textBlock.y / k - textBlock.fontSize / k)
                        cb.setTextMatrix(xx, yy - round(1.0*(textBlock.fontSize+4)*lineNumber/k))
                        lineNumber += 1
                        cb.showText(line)
                        cb.endText()
                    }
                }

                case imageBlock: ImageBlock => {
                    val imageBytes = Base64.decodeBase64(imageBlock.data)
                    val pdfImage = com.itextpdf.text.Image.getInstance(java.awt.Toolkit.getDefaultToolkit.createImage(imageBytes), null)

                    val xx = round(imageBlock.x / k)
                    val yy = round(rec.getHeight() - imageBlock.y / k - imageBlock.height / k)

                    cb.addImage(pdfImage, imageBlock.width / k, 0, 0, imageBlock.height/k, xx, yy)                    
                }
            }
        }

        stamper.close()
        output.toByteArray
    }

}