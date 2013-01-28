package models

class ImageBlock(
  val url: String,
  val width: Int,
  val height: Int,
  val page: Int,
  val x: Int,
  val y: Int
) extends Block