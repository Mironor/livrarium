package helpers

import java.util.UUID

class PseudoUUIDGenerator extends RandomIdGenerator {

  val generatedBookId = UUID.fromString("0ec7ea5a-12d2-11e5-b60b-1697f925ec7b").toString

  override def generateBookId() = generatedBookId
}
