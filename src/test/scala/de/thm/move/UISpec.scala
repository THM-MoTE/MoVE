package de.thm.move

import java.awt.MouseInfo

import com.athaydes.automaton.FXApp
import org.scalatest.BeforeAndAfterAll
import de.thm.move.implicits.ConcurrentImplicits._
import de.thm.move.types._
import org.scalactic.TolerantNumerics

abstract class UISpec
  extends MoveSpec
  with BeforeAndAfterAll {

  implicit val coordinatePrecision = TolerantNumerics.tolerantDoubleEquality(0.01)


  override def beforeAll: Unit = {
    FXApp.startApp( new MoveApp() )
    // let the window open and show before running tests
    Thread.sleep( 2000 )
  }
  override def afterAll: Unit = {
    FXApp.doInFXThreadBlocking( () => FXApp.getStage().close() )
  }

  def mousePosition:Point = {
    val p = MouseInfo.getPointerInfo.getLocation
    (p.x,p.y)
  }
}
