package services

// Thrown when User is trying to access to content that is not attached to him/her
class ServiceNotAllowedException(msg: String) extends Exception(msg)

