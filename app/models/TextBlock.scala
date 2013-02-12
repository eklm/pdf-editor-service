package models

class TextBlock(
  val text: String,
  val font: String,
  val fontSize: Int,
  val color: String,
  val page: Int,
  val x: Int,
  val y: Int
) extends Block