package helpers

class PseudoUUIDGenerator extends RandomIdGenerator {

  val generatedBookId = "00000000-0000-0000-000000000000"

  override def generateBookId() = generatedBookId
}
