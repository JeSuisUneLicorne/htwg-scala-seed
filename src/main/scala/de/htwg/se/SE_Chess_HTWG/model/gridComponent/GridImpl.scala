package de.htwg.se.SE_Chess_HTWG.model.gridComponent

import com.google.inject.{Guice, Inject}
import de.htwg.se.SE_Chess_HTWG.ChessModule
import de.htwg.se.SE_Chess_HTWG.model.fileIOComponent.FileIOInterface
import de.htwg.se.SE_Chess_HTWG.model.movement.Move
import de.htwg.se.SE_Chess_HTWG.model.pieceComponent._
import de.htwg.se.SE_Chess_HTWG.util.ColumnMatcher._
import de.htwg.se.SE_Chess_HTWG.util.MovementResult
import de.htwg.se.SE_Chess_HTWG.util.MovementResult.MovementResult

import scala.math.abs

class GridImpl @Inject()(var cells: Matrix) extends GridInterface {
  val BOARD_SIZE: Int = 8
  var enPassantSquare: Option[Cell] = None
  var promotionSquare: Option[Cell] = None

  val injector = Guice.createInjector(new ChessModule)
  val pieceFactory: PieceFactory = injector.getInstance(classOf[PieceFactory])

  def getCell(row: Int, col: Int): Cell = cells.cell(row, col)

  def setCells(cells: Matrix): Unit = this.cells = cells

  def replaceColor(row: Int, col: Int, isWhite: Boolean): Matrix = cells.replaceCell(row, col, Cell(getCell(row, col).value, isWhite))

  def replaceValue(row: Int, col: Int, value: Option[PieceInterface]): Matrix = cells.replaceCell(row, col, Cell(value, getCell(row, col).isWhite))

  def movePiece(move: Move): MovementResult = move.executeMove

  def getSetCells(): List[Cell] = cells.getSetCells

  override def createNewGridWithPieces: GridInterface = {
    createNewGrid
    setPieces
    this
  }

  override def createNewGridWithoutPieces: GridInterface = {
    createNewGrid
    this
  }

  def setPieces: Unit = {
    for (col <- 0 until BOARD_SIZE) {
      setCells(replaceValue(1, col, Some(pieceFactory.getPiece(Piece.PAWN, true, 1, col))))
      cells = replaceValue(0, col, Some(matchColToPiece(0, col, true)))

      cells = replaceValue(6, col, Some(pieceFactory.getPiece(Piece.PAWN,false, 6, col)))
      cells = replaceValue(7, col, Some(matchColToPiece(7, col, false)))
    }
  }

  def createNewGrid: Unit = {
    for {
      row <- 0 until BOARD_SIZE
      col <- 0 until BOARD_SIZE
    } if ((row + col) % 2 != 0) cells = replaceColor(row, col, true)
  }

  override def promotePiece(row: Int, col: Int, pieceShortcut: String): MovementResult = {
    if (promotionSquare.isDefined && promotionSquare.get.value.get.row == row && promotionSquare.get.value.get.col == col) {
      val promotionPiece: Option[PieceInterface] = getPromotionPieceFromPieceShortcut(row, col, pieceShortcut, getCell(row, col).value.get.isWhite)
      if (promotionPiece.isDefined) {
        replaceValue(row, col, promotionPiece)
        MovementResult.SUCCESS
      } else {
        MovementResult.ERROR
      }
    } else {
      MovementResult.ERROR
    }
  }

  def getPromotionPieceFromPieceShortcut(row: Int, col: Int, pieceShortcut: String, isWhite: Boolean): Option[PieceInterface] = {
    pieceShortcut match {
      case "Q" => Some(pieceFactory.getPiece(Piece.QUEEN, isWhite, row, col))
      case "R" => Some(pieceFactory.getPiece(Piece.ROOK, isWhite, row, col))
      case "N" => Some(pieceFactory.getPiece(Piece.KNIGHT, isWhite, row, col))
      case "B" => Some(pieceFactory.getPiece(Piece.BISHOP, isWhite, row, col))
      case _ => None
    }
  }

  def matchColToPiece(row: Int, col: Int, isWhite: Boolean): PieceInterface = {
    col match {
      case 0 => pieceFactory.getPiece(Piece.ROOK, isWhite, row, col)
      case 1 => pieceFactory.getPiece(Piece.KNIGHT, isWhite, row, col)
      case 2 => pieceFactory.getPiece(Piece.BISHOP, isWhite, row, col)
      case 3 => if (isWhite) pieceFactory.getPiece(Piece.QUEEN, isWhite, row, col) else pieceFactory.getPiece(Piece.KING, isWhite, row, col)
      case 4 => if (isWhite) pieceFactory.getPiece(Piece.KING, isWhite, row, col) else pieceFactory.getPiece(Piece.QUEEN, isWhite, row, col)
      case 5 => pieceFactory.getPiece(Piece.BISHOP, isWhite, row, col)
      case 6 => pieceFactory.getPiece(Piece.KNIGHT, isWhite, row, col)
      case 7 => pieceFactory.getPiece(Piece.ROOK, isWhite, row, col)
    }
  }

  override def toString: String = {
    val upperBorder = ("+--" + "--" * BOARD_SIZE) + "--------+\n"
    val fillerLine = "|  " + "  " * BOARD_SIZE + "        |\n"
    val lowerBorder = ("+  " + "/" * (BOARD_SIZE)) + "+\n"  //"+--" "/--" u0336
    val line = ("_  " + ("x " * BOARD_SIZE)) + "  |\n"
    var box = "\n" + (upperBorder + fillerLine + (line * BOARD_SIZE)) + fillerLine + lowerBorder

    for {
      row <- (0 until BOARD_SIZE).reverse
      col <- 0 until BOARD_SIZE
    } box = box.replaceFirst("x", getCell(row, col).toString)
      .replaceFirst("_", abs(col - 8).toString).replaceFirst("/", matchColToLetter(col))
    box
  }
}
