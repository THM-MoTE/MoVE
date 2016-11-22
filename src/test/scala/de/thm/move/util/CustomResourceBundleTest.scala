package de.thm.move.util

import java.util.{MissingResourceException, ResourceBundle}

import de.thm.move.MoveSpec

class CustomResourceBundleTest extends MoveSpec {
  val locale = java.util.Locale.ENGLISH
  val bundle = new CustomResourceBundle(List("i18n/messages"), locale)

  "A CustomResourceBundle" should "retrieve keys based on the locale" in {
    bundle.getString("file.new") shouldBe "New"
    bundle.getString("edit.undo") shouldBe "Undo"
  }
  it should "not retrieve unknown keys" in {
    an [MissingResourceException] should be thrownBy bundle.getString("pane")
    an [MissingResourceException] should be thrownBy bundle.getString("window")
    an [MissingResourceException] should be thrownBy bundle.getString("tiger")
  }
  it should "retrieve resources from multiple bundles" in {
    val locale = java.util.Locale.ENGLISH
    val bundle = new CustomResourceBundle(List("i18n/messages", "fonts/fontawesome"), locale)
    bundle.getString("fa.square") shouldBe "\uf0c8"
    bundle.getString("fa.file.exit") shouldBe "\uf00d"
  }

  it should "not retrieve unknown keys from resources with multiple bundles" in {
    val locale = java.util.Locale.ENGLISH
    val bundle = new CustomResourceBundle(List("i18n/messages", "fonts/fontawesome"), locale)
    an [MissingResourceException] should be thrownBy bundle.getString("fa.test")
  }
}
