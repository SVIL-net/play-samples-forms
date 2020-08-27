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
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import profile.api._

    /**
      *  Here we define the table. It will have a name of people
      */
  private class WidgetTable(tag: Tag) extends Table[Widget](tag, "widgets") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The age column */
    def price = column[Int]("price")

        /**
          *  This is the tables default "projection".
          *
          *  It defines how the columns are converted to and from the Person object.
          *
          *  In this case, we are simply passing the id, name and page parameters to the Person case classes
          *  apply and unapply methods.
          */
    def * = (id, name, price) <> ((Widget.apply _).tupled, Widget.unapply)
  }

  private val widgets = TableQuery[WidgetTable]

  def create(name:String, price:Int): Future[Widget] = db.run {
    (widgets.map( w => (w.name, w.price))
       returning(widgets.map(_.id))
       into((na, id) => Widget(id, na._1, na._2))
    ) += (name, price)
  }

  def list(): Future[Seq[Widget]] = db.run {
    widgets.result
  }
}
