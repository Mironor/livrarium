package helpers

import java.util.UUID

class PseudoUUIDGenerator extends RandomIdGenerator {

  val generatedBookId = UUID.fromString("00000000-0000-4000-A000-000000000000")

  override def generateBookId() = generatedBookId
}
