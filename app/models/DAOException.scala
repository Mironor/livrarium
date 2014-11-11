package models

case class DAOException(message: String) extends Exception(message)
