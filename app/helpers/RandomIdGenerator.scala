package helpers

import java.util.UUID

trait RandomIdGenerator {
  def generateBookId(): String
}

class UUIDGenerator extends RandomIdGenerator{
  def generateBookId(): String = UUID.randomUUID().toString
}
