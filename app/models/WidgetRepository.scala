package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
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
   * Here we define the table. It will have a name of people
   */
  private class WidgetTable(tag: Tag) extends Table[Widget](tag, "widget") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")

    /** The age column */
    def price = column[Int]("price")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, name, price) <> ((Widget.apply _).tupled, Widget.unapply)
  }

  /**
   * The starting point for all queries on the people table.
   */
  private val widget = TableQuery[WidgetTable]

  /**
   * Create a person with the given name and age.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def create(name: String, price: Int): Future[Widget] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (widget.map(p => (p.name, p.price))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning widget.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((namePrice, id) => Widget(id, namePrice._1, namePrice._2))
    // And finally, insert the person into the database
    ) += (name, price)
  }

  /**
   * List all the people in the database.
   */
  def list(): Future[Seq[Widget]] = db.run {
    widget.result
  }
}