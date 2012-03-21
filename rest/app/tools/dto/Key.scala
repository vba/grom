package tools.dto

import tools.extractors.{OfficeToPdf, PdfToPng, Extractable}


class Key private[dto] (val id: String, val extractor: Option[Extractable])
case class PdfKey (override val id: String) extends Key (id, Some(PdfToPng))
case class OfficeKey (override val id: String) extends Key (id, Some(OfficeToPdf))