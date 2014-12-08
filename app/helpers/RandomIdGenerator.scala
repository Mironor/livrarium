package helpers

import java.util.UUID

trait RandomIdGenerator {
  def generateBookId(): UUID
}

class UUIDGenerator extends RandomIdGenerator{
  def generateBookId(): UUID = UUID.randomUUID()
}
