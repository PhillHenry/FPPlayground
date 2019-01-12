package uk.co.odinconsultants.fp.tagless


/**
  * "usually OOP makes it hard add interface methods but easy to add implementations. In FP, on the other hand, it’s easy to add new methods, but harder to add implementations."
  * "Tagless final approach brings us 3 “moving parts”: Language, Bridge, Interpreter"
  * see https://blog.scalac.io/exploring-tagless-final.html
  */
object TaglessFinal extends App {

  trait Language[Wrapper[_]] {
    def number(v: Int): Wrapper[Int]

    def increment(a: Wrapper[Int]): Wrapper[Int]
    def add(a: Wrapper[Int], b: Wrapper[Int]): Wrapper[Int]
    def text(v: String): Wrapper[String]
    def toUpper(a: Wrapper[String]): Wrapper[String]
    def concat(a: Wrapper[String], b: Wrapper[String]): Wrapper[String]
    def toString(v: Wrapper[Int]): Wrapper[String]
  }

  trait ScalaToLanguageBridge[ScalaValue] {
    def apply[Wrapper[_]](implicit L: Language[Wrapper]): Wrapper[ScalaValue]
  }

  def buildNumber(number: Int) = new ScalaToLanguageBridge[Int] {
    override def apply[Wrapper[_]](implicit L: Language[Wrapper]): Wrapper[Int] = L.number(number)
  }

  def buildIncrementNumber(number: Int) = new ScalaToLanguageBridge[Int] {
    override def apply[Wrapper[_]](implicit L: Language[Wrapper]): Wrapper[Int] = L.increment(L.number(number))
  }

  def buildIncrementExpression(expression: ScalaToLanguageBridge[Int]) = new ScalaToLanguageBridge[Int] {
    override def apply[Wrapper[_]](implicit L: Language[Wrapper]): Wrapper[Int] = L.increment(expression.apply)
  }

  def buildComplexExpression(text: String, a: Int, b: Int) = new ScalaToLanguageBridge[String] {
    override def apply[Wrapper[_]](implicit F: Language[Wrapper]): Wrapper[String] = {
      val addition = F.add(F.number(a), F.increment(F.number(b)))
      F.concat(F.text(text), F.toString(addition))
    }
  }

  val fullExpression = buildComplexExpression("Result is ", 10, 1)

  type NoWrap[ScalaValue] = ScalaValue

  val interpret = new Language[NoWrap] {
    override def number(v: Int): NoWrap[Int] = v
    override def increment(a: NoWrap[Int]): NoWrap[Int] = a + 1
    override def add(a: NoWrap[Int], b: NoWrap[Int]): NoWrap[Int] = a + b

    override def text(v: String): NoWrap[String] = v
    override def toUpper(a: NoWrap[String]): NoWrap[String] = a.toUpperCase
    override def concat(a: NoWrap[String], b: NoWrap[String]): NoWrap[String] = a + " " + b

    override def toString(v: NoWrap[Int]): NoWrap[String] = v.toString
  }

  type PrettyPrint[ScalaValue] = String

  val interpretAsPrettyPrint = new Language[PrettyPrint] {
    override def number(v: Int): PrettyPrint[Int] = s"($v)"
    override def increment(a: PrettyPrint[Int]): PrettyPrint[Int] = s"(inc $a)"
    override def add(a: PrettyPrint[Int], b: PrettyPrint[Int]): PrettyPrint[Int] = s"(+ $a $b)"

    override def text(v: String): PrettyPrint[String] = s"[$v]"
    override def toUpper(a: PrettyPrint[String]): PrettyPrint[String] = s"(toUpper $a)"
    override def concat(a: PrettyPrint[String], b: PrettyPrint[String]): PrettyPrint[String] = s"(concat $a $b)"

    override def toString(v: PrettyPrint[Int]): PrettyPrint[String] = s"(toString $v)"
  }

  println(fullExpression(interpret))

  println(s"interpreted full (as pretty print): ${fullExpression(interpretAsPrettyPrint)}")

  trait LanguageWithMul[Wrapper[_]] extends Language[Wrapper] {
    def multiply(a: Wrapper[Int], b: Wrapper[Int]): Wrapper[Int]
  }
  trait ScalaToLanguageWithMulBridge[ScalaValue] {
    def apply[Wrapper[_]](implicit L: LanguageWithMul[Wrapper]): Wrapper[ScalaValue]
  }
  def multiply(a: Int, b: Int) = new ScalaToLanguageWithMulBridge[Int] {
    override def apply[Wrapper[_]](implicit L: LanguageWithMul[Wrapper]): Wrapper[Int] = {
      L.multiply(L.number(a), L.number(b))
    }
  }

  val interpretWithMul = new LanguageWithMul[NoWrap] {
    override def multiply(a: NoWrap[Int], b: NoWrap[Int]): NoWrap[Int] = a * b

    override def number(v: Int): NoWrap[Int] = v
    override def increment(a: NoWrap[Int]): NoWrap[Int] = a + 1
    override def add(a: NoWrap[Int], b: NoWrap[Int]): NoWrap[Int] = a + b

    override def text(v: String): NoWrap[String] = v
    override def toUpper(a: NoWrap[String]): NoWrap[String] = a.toUpperCase
    override def concat(a: NoWrap[String], b: NoWrap[String]): NoWrap[String] = a + " " + b

    override def toString(v: NoWrap[Int]): NoWrap[String] = v.toString
  }

  // PH
  trait SemiImplemented extends Language[NoWrap] {
    // copied from interpretAsPrettyPrint above
    override def number(v: Int): NoWrap[Int] = v
    override def increment(a: NoWrap[Int]): NoWrap[Int] = a + 1
    override def add(a: NoWrap[Int], b: NoWrap[Int]): NoWrap[Int] = a + b

    override def text(v: String): NoWrap[String] = v
    override def toUpper(a: NoWrap[String]): NoWrap[String] = a.toUpperCase
    override def concat(a: NoWrap[String], b: NoWrap[String]): NoWrap[String] = a + " " + b

    override def toString(v: NoWrap[Int]): NoWrap[String] = v.toString
  }
  val interpretWithMulPH = new SemiImplemented with LanguageWithMul[NoWrap] {
    override def multiply(a: NoWrap[Int], b: NoWrap[Int]): NoWrap[Int] = a * b
  }

}
