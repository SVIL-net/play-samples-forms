package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ Future, ExecutionContext }

/**
  *  A repository for widget items.
  *
  *  @param dbConfigProvider The Play db config provider. Play will inject this for you.
  */

@Singleton
class WidgetRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
 
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._


  private class WidgetTable(tag: Tag) extends Table[Widget](tag, "widgets") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def price = column[Int]("price")
    def * = (id, name, price) <> ((Widget.apply _).tupled, Widget.unapply)
  }

  private val widgets = TableQuery[WidgetTable]

  def create(name:String, price:Int): Future[Widget] = db.run {
    (widgets.map( w => (w.name, w.price))
      returning widgets.map(_.id)
      into ((namePrice, id) => Widget(id, namePrice._1, namePrice._2))
    ) += (name, price)
  }

  def list(): Future[Seq[Widget]] = db.run {
    widgets.result
  }
}